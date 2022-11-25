package org.ergoplatform.appkit

import org.ergoplatform.sdk.wallet.secrets.{ExtendedSecretKey, DerivationPath}
import scalan.{RType, ExactIntegral}
import special.collection.Coll
import com.google.common.base.{Preconditions, Strings}

import scala.collection.{JavaConversions, mutable}
import org.ergoplatform._
import org.ergoplatform.ErgoBox.TokenId
import sigmastate.SType
import sigmastate.Values.{SValue, SigmaPropConstant, ErgoTree, SigmaBoolean, Constant, EvaluatedValue}
import sigmastate.serialization.{ErgoTreeSerializer, GroupElementSerializer, SigmaSerializer, ValueSerializer}
import scorex.crypto.authds.ADKey
import scorex.crypto.hash.Digest32
import org.ergoplatform.settings.ErgoAlgos
import sigmastate.lang.Terms.ValueOps
import sigmastate.eval.{CompiletimeIRContext, Evaluation, Colls, CostingSigmaDslBuilder, CPreHeader}
import special.sigma.{Header, GroupElement, AnyValue, AvlTree}

import java.util
import java.lang.{Boolean => JBoolean, Short => JShort, Integer => JInt, Long => JLong, Byte => JByte, String => JString}
import java.math.BigInteger
import java.text.Normalizer.Form.NFKD
import java.text.Normalizer.normalize
import java.util.{List => JList, Map => JMap}
import org.ergoplatform.ErgoAddressEncoder.NetworkPrefix
import org.ergoplatform.sdk.wallet.TokensMap
import org.ergoplatform.wallet.mnemonic.Mnemonic.{Pbkdf2Iterations, Pbkdf2KeyLength}
import scorex.util.encode.Base16
import sigmastate.basics.DLogProtocol.ProveDlog
import sigmastate.basics.{DiffieHellmanTupleProverInput, ProveDHTuple}
import sigmastate.basics.CryptoConstants.EcPointType
import scorex.util.{idToBytes, bytesToId, ModifierId}
import sigmastate.interpreter.ContextExtension
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.KeyParameter
import org.ergoplatform.sdk.JavaHelpers.{TokenIdRType, TokenColl}
import org.ergoplatform.sdk.Extensions.{CollBuilderOps, PairCollOps}
import org.ergoplatform.sdk.JavaHelpers.UniversalConverter
import org.ergoplatform.sdk.{LowPriorityIsos, ErgoToken, SecretString, Iso, InverseIso}
import scalan.ExactIntegral.LongIsExactIntegral
import sigmastate.eval.CostingSigmaDslBuilder.validationSettings


object Iso extends LowPriorityIsos {

  implicit val isoErgoTypeToSType: Iso[ErgoType[_], SType] = new Iso[ErgoType[_], SType] {
    override def to(et: ErgoType[_]): SType = Evaluation.rtypeToSType(et.getRType)
    override def from(st: SType): ErgoType[_] = new ErgoType(Evaluation.stypeToRType(st))
  }

  implicit val isoErgoValueToSValue: Iso[ErgoValue[_], EvaluatedValue[SType]] = new Iso[ErgoValue[_], EvaluatedValue[SType]] {
    override def to(x: ErgoValue[_]): EvaluatedValue[SType] =
      Constant(x.getValue.asInstanceOf[SType#WrappedType], Evaluation.rtypeToSType(x.getType.getRType))
      
    override def from(x: EvaluatedValue[SType]): ErgoValue[_] = {
      new ErgoValue(x.value, new ErgoType(Evaluation.stypeToRType(x.tpe)))
    }
  }

  implicit val isoContextVarsToContextExtension: Iso[JList[ContextVar], ContextExtension] = new Iso[JList[ContextVar], ContextExtension] {
    import JavaHelpers._
    override def to(vars: JList[ContextVar]): ContextExtension = {
      var values: Map[Byte, EvaluatedValue[SType]] = Map.empty
      vars.convertTo[IndexedSeq[ContextVar]].foreach { v =>
        val id = v.getId
        val value = v.getValue
        if (values.contains(id)) sys.error(s"Duplicate variable id: ($id -> $value")
        values += (v.getId() -> isoErgoValueToSValue.to(v.getValue))
      }
      ContextExtension(values)
    }
    override def from(b: ContextExtension): JList[ContextVar] = {
      val iso = sdk.Iso.JListToIndexedSeq[ContextVar, ContextVar]
      val vars = iso.from(b.values
        .map { case (id, v) => new ContextVar(id, isoErgoValueToSValue.from(v)) }
        .toIndexedSeq)
      vars
    }
  }

}

object JavaHelpers {
  import sdk.Iso._
  import appkit.Iso._

  implicit class ListOps[A](val xs: JList[A]) extends AnyVal {
    def map[B](f: A => B): JList[B] = {
      xs.convertTo[IndexedSeq[A]].map(f).convertTo[JList[B]]
    }
  }

  /** This value must be lazy to prevent early access to uninitialized unitType value. */
  lazy val UnitErgoVal = new ErgoValue[Unit]((), ErgoType.unitType)

  /** Transforms serialized bytes of ErgoTree with segregated constants by
    * replacing constants at given positions with new values. This operation
    * allow to use serialized scripts as pre-defined templates.
    * See [[sigmastate.SubstConstants]] for details.
    *
    * @param ergoTreeBytes serialized ErgoTree with ConstantSegregationFlag set to 1.
    * @param positions     zero based indexes in ErgoTree.constants array which
    *                      should be replaced with new values
    * @param newValues     new values to be injected into the corresponding
    *                      positions in ErgoTree.constants array
    * @return a new ErgoTree such that only specified constants
    *         are replaced and all other remain exactly the same
    */
  def substituteErgoTreeConstants(ergoTreeBytes: Array[Byte], positions: Array[Int], newValues: Array[ErgoValue[_]]): ErgoTree = {
    val (newBytes, _) = ErgoTreeSerializer.DefaultSerializer.substituteConstants(
      ergoTreeBytes, positions, newValues.map(v => Iso.isoErgoValueToSValue.to(v).asInstanceOf[Constant[SType]]))
    ErgoTreeSerializer.DefaultSerializer.deserializeErgoTree(newBytes)
  }

  def compile(constants: util.Map[String, Object], contractText: String, networkPrefix: NetworkPrefix): ErgoTree = {
    val env = JavaConversions.mapAsScalaMap(constants).toMap
    implicit val IR = new CompiletimeIRContext
    val prop = ErgoScriptPredef.compileWithCosting(env, contractText, networkPrefix).asSigmaProp
    ErgoTree.fromProposition(prop)
  }

  /** Extracts registers as a list of ErgoValue instances (containing type descriptors). */
  def getBoxRegisters(ergoBox: ErgoBoxCandidate): JList[ErgoValue[_]] = {
    val size = ergoBox.additionalRegisters.size
    val res = new util.ArrayList[ErgoValue[_]](size)
    for ((r, v: EvaluatedValue[_]) <- ergoBox.additionalRegisters.toIndexedSeq.sortBy(_._1.number)) {
      val i = r.number - ErgoBox.mandatoryRegistersCount
      require(i == res.size(), s"registers are not densely packed in a box: ${ergoBox.additionalRegisters}")
      res.add(Iso.isoErgoValueToSValue.from(v))
    }
    res
  }

  def createBoxCandidate(
        value: Long, tree: ErgoTree,
        tokens: Seq[ErgoToken],
        registers: Seq[ErgoValue[_]], creationHeight: Int): ErgoBoxCandidate = {
    import ErgoBox.nonMandatoryRegisters
    val nRegs = registers.length
    Preconditions.checkArgument(nRegs <= nonMandatoryRegisters.length,
       "Too many additional registers %d. Max allowed %d", nRegs, nonMandatoryRegisters.length)
    implicit val TokenIdRType: RType[TokenId] = RType.arrayRType[Byte].asInstanceOf[RType[TokenId]]
    val ts = Colls.fromItems(tokens.map(isoErgoTokenToPair.to(_)):_*)
    val rs = registers.zipWithIndex.map { case (ergoValue, i) =>
      val id = ErgoBox.nonMandatoryRegisters(i)
      val value = Iso.isoErgoValueToSValue.to(ergoValue)
      id -> value
    }.toMap

    new ErgoBoxCandidate(value, tree, creationHeight, ts, rs)
  }

  def secretStringToOption(secretString: SecretString): Option[SecretString] = {
    if (secretString == null || secretString.isEmpty) None else Some(secretString)
  }

  def createDiffieHellmanTupleProverInput(g: GroupElement,
                                          h: GroupElement,
                                          u: GroupElement,
                                          v: GroupElement,
                                          x: BigInteger): DiffieHellmanTupleProverInput = {
    createDiffieHellmanTupleProverInput(
      g = sdk.JavaHelpers.SigmaDsl.toECPoint(g),
      h = sdk.JavaHelpers.SigmaDsl.toECPoint(h),
      u = sdk.JavaHelpers.SigmaDsl.toECPoint(u),
      v = sdk.JavaHelpers.SigmaDsl.toECPoint(v),
      x
    )
  }

  def createDiffieHellmanTupleProverInput(g: EcPointType,
                                          h: EcPointType,
                                          u: EcPointType,
                                          v: EcPointType,
                                          x: BigInteger): DiffieHellmanTupleProverInput = {
    val dht = ProveDHTuple(g, h, u, v)
    DiffieHellmanTupleProverInput(x, dht)
  }

}



