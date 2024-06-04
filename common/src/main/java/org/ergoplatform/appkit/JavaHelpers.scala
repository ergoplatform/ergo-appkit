package org.ergoplatform.appkit

import org.ergoplatform.ErgoAddressEncoder.NetworkPrefix
import org.ergoplatform.ErgoBox.TokenId
import org.ergoplatform.ErgoScriptPredef.compileWithCosting
import org.ergoplatform._
import org.ergoplatform.sdk.JavaHelpers.{collRType, UniversalConverter}
import org.ergoplatform.sdk.{ErgoToken, SdkIsos, LowPriorityIsos}
import sigma.{Evaluation, Colls}
import sigma.data.{Iso, RType}
import sigma.ast.SType
import sigma.ast.syntax.ValueOps
import sigma.ast.{EvaluatedValue, Constant, ErgoTree}
import sigma.data.CSigmaDslBuilder.validationSettings
import sigma.interpreter.ContextExtension
import sigma.serialization.ErgoTreeSerializer
import sigma.compiler.ir.CompiletimeIRContext
import org.ergoplatform.sdk.SdkIsos._
import java.util
import java.util.{List => JList}
import scala.collection.JavaConverters
import scala.collection.compat.immutable.ArraySeq

object AppkitIso extends LowPriorityIsos {
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
    import org.ergoplatform.sdk.JavaHelpers._
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
      val iso = SdkIsos.JListToIndexedSeq[ContextVar, ContextVar]
      val vars = iso.from(b.values
        .map { case (id, v) => new ContextVar(id, isoErgoValueToSValue.from(v)) }
        .toIndexedSeq)
      vars
    }
  }

}

object AppkitHelpers {

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
      ergoTreeBytes, positions, newValues.map(v => AppkitIso.isoErgoValueToSValue.to(v).asInstanceOf[Constant[SType]]))
    ErgoTreeSerializer.DefaultSerializer.deserializeErgoTree(newBytes)
  }

  def toJavaList[T](xs: Seq[T]): util.List[T] = {
    import JavaConverters._
    xs.asJava
  }

  /** Wraps an array into sequence.
    * This method in necessary because ArraySeq is not available from Java (for all Scala versions).
    */
  def arraySeq[T](arr: Array[T]): Seq[T] =
    ArraySeq.unsafeWrapArray(arr)

  def compile(constants: util.Map[String, Object], contractText: String, networkPrefix: NetworkPrefix): ErgoTree = {
    import JavaConverters._
    val env = constants.asScala.toMap
    implicit val IR = new CompiletimeIRContext
    val prop = compileWithCosting(env, contractText, networkPrefix).asSigmaProp
    ErgoTree.fromProposition(prop)
  }

  /** Extracts registers as a list of ErgoValue instances (containing type descriptors). */
  def getBoxRegisters(ergoBox: ErgoBoxCandidate): JList[ErgoValue[_]] = {
    val size = ergoBox.additionalRegisters.size
    val res = new util.ArrayList[ErgoValue[_]](size)
    for ((r, v: EvaluatedValue[_]) <- ergoBox.additionalRegisters.toIndexedSeq.sortBy(_._1.number)) {
      val i = r.number - ErgoBox.mandatoryRegistersCount
      require(i == res.size(), s"registers are not densely packed in a box: ${ergoBox.additionalRegisters}")
      res.add(AppkitIso.isoErgoValueToSValue.from(v))
    }
    res
  }

  //TODO refactor: when similar method is avaialble in Sigma
  def createBoxCandidate(
        value: Long, tree: ErgoTree,
        tokens: Seq[ErgoToken],
        registers: Seq[ErgoValue[_]], creationHeight: Int): ErgoBoxCandidate = {
    import ErgoBox.nonMandatoryRegisters
    val nRegs = registers.length
    require(nRegs <= nonMandatoryRegisters.length,
       s"Too many additional registers $nRegs. Max allowed ${nonMandatoryRegisters.length}")
    implicit val TokenIdRType: RType[TokenId] = collRType(sigma.ByteType).asInstanceOf[RType[TokenId]]
    val ts = Colls.fromItems(tokens.map(isoErgoTokenToPair.to(_)):_*)
    val rs = registers.zipWithIndex.map { case (ergoValue, i) =>
      val id = ErgoBox.nonMandatoryRegisters(i)
      val value = AppkitIso.isoErgoValueToSValue.to(ergoValue)
      id -> value
    }.toMap

    new ErgoBoxCandidate(value, tree, creationHeight, ts, rs)
  }

  def secretStringToOption(secretString: org.ergoplatform.wallet.interface4j.SecretString): Option[org.ergoplatform.wallet.interface4j.SecretString] = {
    if (secretString == null || secretString.isEmpty) None else Some(secretString)
  }

}



