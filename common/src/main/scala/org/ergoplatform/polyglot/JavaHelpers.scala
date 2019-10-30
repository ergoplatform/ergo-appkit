package org.ergoplatform.polyglot

import java.math.BigInteger

import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import org.ergoplatform.ErgoAddressEncoder.NetworkPrefix
import scalan.RType
import special.collection.Coll
import com.google.common.base.Strings

import scala.collection.JavaConverters
import org.ergoplatform.{ErgoBox, ErgoBoxCandidate, UnsignedInput, ErgoScriptPredef}
import org.ergoplatform.ErgoBox.{NonMandatoryRegisterId, TokenId}
import sigmastate.SType
import scorex.util.ModifierId
import sigmastate.Values.{ShortConstant, LongConstant, Constant, EvaluatedValue, SValue, BigIntConstant, IntConstant, ErgoTree, ByteConstant, GroupElementConstant}
import sigmastate.serialization.{ValueSerializer, SigmaSerializer, GroupElementSerializer}
import scorex.crypto.authds.ADKey
import scorex.crypto.hash.Digest32
import org.ergoplatform.wallet.mnemonic.Mnemonic
import org.ergoplatform.settings.ErgoAlgos
import sigmastate.lang.Terms.ValueOps
import sigmastate.eval.{CompiletimeIRContext, Evaluation, SigmaDsl, CAvlTree, Colls, CostingSigmaDslBuilder, CPreHeader, CHeader}
import special.sigma.{Header, GroupElement, AnyValue, AvlTree, PreHeader}
import java.util

import org.bouncycastle.math.ec.ECPoint

object JavaHelpers {
  implicit class StringExtensions(val source: String) extends AnyVal {
    def toBytes: Array[Byte] = decodeStringToBytes(source)
    def toColl: Coll[Byte] = decodeStringToColl(source)
    def toGroupElement: GroupElement = decodeStringToGE(source)
  }

  val HeaderRType: RType[Header] = special.sigma.HeaderRType
  val PreHeaderRType: RType[PreHeader] = special.sigma.PreHeaderRType

  def Algos: ErgoAlgos =  org.ergoplatform.settings.ErgoAlgos

  def deserializeValue[T <: SValue](bytes: Array[Byte]): T = {
    ValueSerializer.deserialize(bytes).asInstanceOf[T]
  }

  def decodeStringToBytes(str: String): Array[Byte] = {
    val bytes = ErgoAlgos.decode(str).fold(t => throw t, identity)
    bytes
  }

  def decodeStringToColl(str: String): Coll[Byte] = {
    val bytes = ErgoAlgos.decode(str).fold(t => throw t, identity)
    Colls.fromArray(bytes)
  }

  def decodeStringToGE(str: String): GroupElement = {
    val bytes = ErgoAlgos.decode(str).fold(t => throw t, identity)
    val pe = GroupElementSerializer.parse(SigmaSerializer.startReader(bytes))
    SigmaDsl.GroupElement(pe)
  }

  def hash(s: String): String = {
    ErgoAlgos.encode(ErgoAlgos.hash(s))
  }

  def toPreHeader(h: Header): PreHeader = {
    CPreHeader(h.version, h.parentId, h.timestamp, h.nBits, h.height, h.minerPk, h.votes)
  }

  def getStateDigest(tree: AvlTree): Array[Byte] = {
    tree.digest.toArray
  }

  def toTokensColl[T](tokens: util.List[ErgoToken]): Coll[(Array[Byte], Long)] = {
    val ts = JavaConverters.asScalaIterator(tokens.iterator()).map(t => (t.getId().getBytes, t.getValue())).toArray
    Colls.fromArray(ts)
  }

  def toTokensSeq[T](tokens: util.List[ErgoToken]): Seq[(TokenId, Long)] = {
    val ts = JavaConverters.asScalaIterator(tokens.iterator()).map(t => (Digest32 @@ t.getId().getBytes(), t.getValue())).toSeq
    ts
  }

  def toIndexedSeq[T](xs: util.List[T]): IndexedSeq[T] = {
    JavaConverters.asScalaIterator(xs.iterator()).toIndexedSeq
  }

  def compile(constants: util.Map[String, Object], contractText: String, networkPrefix: NetworkPrefix): ErgoTree = {
    val env = JavaConverters.mapAsScalaMap(constants).toMap
    implicit val IR = new CompiletimeIRContext
    val prop = ErgoScriptPredef.compileWithCosting(env, contractText, networkPrefix).asSigmaProp
    ErgoTree.fromProposition(prop)
  }

  private def anyValueToConstant(v: AnyValue): Constant[_ <: SType] = {
    val tpe = Evaluation.rtypeToSType(v.tVal)
    Constant(v.value.asInstanceOf[SType#WrappedType], tpe)
  }

  def createBox(
       value: Long, tree: ErgoTree,
       tokens: util.List[ErgoToken],
       registers: util.List[Tuple2[String, Object]], creationHeight: Int, txId: String, index: Short): ErgoBox = {
    val ts = toTokensColl(tokens)
    val rs = toIndexedSeq(registers).map { r =>
      val id = ErgoBox.registerByName(r._1).asInstanceOf[NonMandatoryRegisterId]
      val value = r._2.asInstanceOf[EvaluatedValue[_ <: SType]]
      id -> value
    }.toMap
    new ErgoBox(value, tree, ts.asInstanceOf[Coll[(TokenId, Long)]], rs, ModifierId @@ txId, index, creationHeight)
  }

  def createBoxCandidate(
       value: Long, tree: ErgoTree,
       tokens: util.List[ErgoToken],
       registers: util.List[Tuple2[String, Object]], creationHeight: Int): ErgoBoxCandidate = {
    val ts = toTokensColl(tokens)
    val rs = toIndexedSeq(registers).map { r =>
      val id = ErgoBox.registerByName(r._1).asInstanceOf[NonMandatoryRegisterId]
      val value = r._2.asInstanceOf[EvaluatedValue[_ <: SType]]
      id -> value
    }.toMap
    new ErgoBoxCandidate(value, tree, creationHeight, ts.asInstanceOf[Coll[(TokenId, Long)]], rs)
  }

  def seedToMasterKey(seedPhrase: String, pass: String = ""): ExtendedSecretKey = {
    val passOpt = if (Strings.isNullOrEmpty(pass)) None else Some(pass)
    val seed = Mnemonic.toSeed(seedPhrase, passOpt)
    val masterKey = ExtendedSecretKey.deriveMasterKey(seed)
    masterKey
  }

  def createUnsignedInput(boxId: String): UnsignedInput = {
    val idBytes = decodeStringToBytes(boxId)
    createUnsignedInput(idBytes)
  }

  def createUnsignedInput(boxIdBytes: Array[Byte]): UnsignedInput = {
    new UnsignedInput(ADKey @@ boxIdBytes)
  }

  def valueOf(n: Byte): EvaluatedValue[_] = { ByteConstant(n) }
  def valueOf(n: Short): EvaluatedValue[_] = { ShortConstant(n) }
  def valueOf(n: Int): EvaluatedValue[_] = { IntConstant(n) }
  def valueOf(n: Long): EvaluatedValue[_] = { LongConstant(n) }
  def valueOf(n: BigInteger): EvaluatedValue[_] = { BigIntConstant(n) }
  def valueOf(n: ECPoint): EvaluatedValue[_] = { GroupElementConstant(SigmaDsl.GroupElement(n)) }

  def collRType[T](tItem: RType[T]): RType[Coll[T]] = special.collection.collRType(tItem)
  def BigIntRType: RType[special.sigma.BigInt] = special.sigma.BigIntRType
  def GroupElementRType: RType[special.sigma.GroupElement] = special.sigma.GroupElementRType
  def SigmaPropRType: RType[special.sigma.SigmaProp] = special.sigma.SigmaPropRType
  def AvlTreeRType: RType[special.sigma.AvlTree] = special.sigma.AvlTreeRType

  def SigmaDsl: CostingSigmaDslBuilder = sigmastate.eval.SigmaDsl

  def collFrom(arr: Array[Byte]): Coll[Byte] = {
    Colls.fromArray(arr)
  }

}
