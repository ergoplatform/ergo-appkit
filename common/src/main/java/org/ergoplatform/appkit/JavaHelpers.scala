package org.ergoplatform.appkit

import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import scalan.RType
import special.collection.Coll
import com.google.common.base.{Preconditions, Strings}

import scala.collection.JavaConversions
import org.ergoplatform._
import org.ergoplatform.ErgoBox.TokenId
import sigmastate.SType
import sigmastate.Values.{ErgoTree, Constant, SValue, EvaluatedValue}
import sigmastate.serialization.{ValueSerializer, ErgoTreeSerializer, SigmaSerializer, GroupElementSerializer}
import scorex.crypto.authds.ADKey
import scorex.crypto.hash.Digest32
import org.ergoplatform.wallet.mnemonic.{Mnemonic => WMnemonic}
import org.ergoplatform.settings.ErgoAlgos
import sigmastate.lang.Terms.ValueOps
import sigmastate.eval.{CompiletimeIRContext, Evaluation, Colls, CostingSigmaDslBuilder, CPreHeader}
import special.sigma.{Header, GroupElement, AnyValue, AvlTree, PreHeader}
import java.util
import java.lang.{Long => JLong, String => JString}
import java.util.{List => JList}

import sigmastate.utils.Helpers._  // don't remove, required for Scala 2.11
import org.ergoplatform.ErgoAddressEncoder.NetworkPrefix
import sigmastate.basics.DLogProtocol.ProveDlog

/** Type-class of isomorphisms between types.
  * Isomorphism between two types `A` and `B` essentially say that both types
  * represents the same information (entity) but in a different way.
  * <p>
  * The information is not lost so that both are true:
  * 1) a == from(to(a))
  * 2) b == to(from(b))
  * <p>
  * It is used to define type-full conversions:
  * - different conversions between Java and Scala data types.
  * - conversion between Ergo representations and generated API representations
  */
abstract class Iso[A, B] {
  def to(a: A): B

  def from(b: B): A
}
final case class InverseIso[A,B](iso: Iso[A,B]) extends Iso[B,A] {
  override def to(a: B): A = iso.from(a)
  override def from(b: A): B = iso.to(b)
}
trait LowPriorityIsos {



}

object Iso extends LowPriorityIsos {
  implicit def identityIso[A]: Iso[A, A] = new Iso[A, A] {
    override def to(a: A): A = a

    override def from(b: A): A = b
  }

  implicit def inverseIso[A,B](implicit iso: Iso[A,B]): Iso[B,A] = InverseIso[A,B](iso)

  implicit val jlongToLong: Iso[JLong, Long] = new Iso[JLong, Long] {
    override def to(b: JLong): Long = b
    override def from(a: Long): JLong = a
  }

  implicit val isoErgoTokenToPair: Iso[ErgoToken, (TokenId, Long)] = new Iso[ErgoToken, (TokenId, Long)] {
    override def to(a: ErgoToken) = (Digest32 @@ a.getId.getBytes, a.getValue)
    override def from(t: (TokenId, Long)): ErgoToken = new ErgoToken(t._1, t._2)
  }

  implicit val isoErgoValueToSValue: Iso[ErgoValue[_], EvaluatedValue[SType]] = new Iso[ErgoValue[_], EvaluatedValue[SType]] {
    override def to(x: ErgoValue[_]): EvaluatedValue[SType] =
      Constant(x.getValue.asInstanceOf[SType#WrappedType], Evaluation.rtypeToSType(x.getType.getRType))
      
    override def from(x: EvaluatedValue[SType]): ErgoValue[_] = {
      new ErgoValue(x.value, new ErgoType(Evaluation.stypeToRType(x.tpe)))
    }
  }

  val isoTokensListToPairsColl: Iso[JList[ErgoToken], Coll[(TokenId, Long)]] = {
    implicit val TokenIdRType: RType[TokenId] = RType.arrayRType[Byte].asInstanceOf[RType[TokenId]]
    JListToColl(isoErgoTokenToPair, RType[(TokenId, Long)])
  }


  implicit val jstringToOptionString: Iso[JString, Option[String]] = new Iso[JString, Option[String]] {
    override def to(a: JString): Option[String] = if (Strings.isNullOrEmpty(a)) None else Some(a)
    override def from(b: Option[String]): JString = if (b.isEmpty) "" else b.get
  }

  implicit def JListToIndexedSeq[A, B](implicit itemIso: Iso[A, B]): Iso[JList[A], IndexedSeq[B]] =
    new Iso[JList[A], IndexedSeq[B]] {
      override def to(as: JList[A]): IndexedSeq[B] = {
        JavaConversions.asScalaIterator(as.iterator()).map(itemIso.to).toIndexedSeq
      }

      override def from(bs: IndexedSeq[B]): JList[A] = {
        val res = new util.ArrayList[A](bs.length)
        for ( a <- bs.map(itemIso.from) ) res.add(a)
        res
      }
    }

  implicit def JListToColl[A, B](implicit itemIso: Iso[A, B], tB: RType[B]): Iso[JList[A], Coll[B]] =
    new Iso[JList[A], Coll[B]] {
      override def to(as: JList[A]): Coll[B] = {
        val bsIter = JavaConversions.asScalaIterator(as.iterator).map { a =>
          itemIso.to(a)
        }
        Colls.fromArray(bsIter.toArray(tB.classTag))
      }

      override def from(bs: Coll[B]): JList[A] = {
        val res = new util.ArrayList[A](bs.length)
        bs.toArray.foreach { b =>
          res.add(itemIso.from(b))
        }
        res
      }
    }

}

object JavaHelpers {
  implicit class UniversalConverter[A](val x: A) extends AnyVal {
    def convertTo[B](implicit iso: Iso[A,B]): B = iso.to(x)
  }

  implicit class StringExtensions(val source: String) extends AnyVal {
    def toBytes: Array[Byte] = decodeStringToBytes(source)

    def toColl: Coll[Byte] = decodeStringToColl(source)

    def toGroupElement: GroupElement = decodeStringToGE(source)
  }

  implicit val TokenIdRType: RType[TokenId] = RType.arrayRType[Byte].asInstanceOf[RType[TokenId]]

  val HeaderRType: RType[Header] = special.sigma.HeaderRType
  val PreHeaderRType: RType[PreHeader] = special.sigma.PreHeaderRType

  def Algos: ErgoAlgos = org.ergoplatform.settings.ErgoAlgos

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

  def createP2PKAddress(pk: ProveDlog, networkPrefix: NetworkPrefix): P2PKAddress = {
    implicit val ergoAddressEncoder: ErgoAddressEncoder = ErgoAddressEncoder(networkPrefix)
    P2PKAddress(pk)
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

  def toIndexedSeq[T](xs: util.List[T]): IndexedSeq[T] = {
    JavaConversions.asScalaIterator(xs.iterator()).toIndexedSeq
  }

  def compile(constants: util.Map[String, Object], contractText: String, networkPrefix: NetworkPrefix): ErgoTree = {
    val env = JavaConversions.mapAsScalaMap(constants).toMap
    implicit val IR = new CompiletimeIRContext
    val prop = ErgoScriptPredef.compileWithCosting(env, contractText, networkPrefix).asSigmaProp
    ErgoTree.fromProposition(prop)
  }

  private def anyValueToConstant(v: AnyValue): Constant[_ <: SType] = {
    val tpe = Evaluation.rtypeToSType(v.tVal)
    Constant(v.value.asInstanceOf[SType#WrappedType], tpe)
  }

  def createBoxCandidate(
        value: Long, tree: ErgoTree,
        tokens: util.List[ErgoToken],
        registers: util.List[ErgoValue[_]], creationHeight: Int): ErgoBoxCandidate = {
    import ErgoBox.nonMandatoryRegisters
    val nRegs = registers.size()
    Preconditions.checkArgument(nRegs <= nonMandatoryRegisters.length,
       "Too many additional registers %d. Max allowed %d", nRegs, nonMandatoryRegisters.length)
    val ts = tokens.convertTo[Coll[(TokenId, Long)]]
    val rs = toIndexedSeq(registers).zipWithIndex.map { case (ergoValue, i) =>
      val id = ErgoBox.nonMandatoryRegisters(i)
      val value = Iso.isoErgoValueToSValue.to(ergoValue)
      id -> value
    }.toMap

    new ErgoBoxCandidate(value, tree, creationHeight, ts, rs)
  }

  def seedToMasterKey(seedPhrase: String, pass: String = ""): ExtendedSecretKey = {
    val passOpt = if (Strings.isNullOrEmpty(pass)) None else Some(pass)
    val seed = WMnemonic.toSeed(seedPhrase, passOpt)
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

  def collRType[T](tItem: RType[T]): RType[Coll[T]] = special.collection.collRType(tItem)

  def BigIntRType: RType[special.sigma.BigInt] = special.sigma.BigIntRType

  def GroupElementRType: RType[special.sigma.GroupElement] = special.sigma.GroupElementRType

  def SigmaPropRType: RType[special.sigma.SigmaProp] = special.sigma.SigmaPropRType

  def AvlTreeRType: RType[special.sigma.AvlTree] = special.sigma.AvlTreeRType

  def SigmaDsl: CostingSigmaDslBuilder = sigmastate.eval.SigmaDsl

  def collFrom(arr: Array[Byte]): Coll[Byte] = {
    Colls.fromArray(arr)
  }

  def ergoTreeTemplateBytes(ergoTree: ErgoTree): Array[Byte] = {
    (new ErgoTreeSerializer).deserializeHeaderWithTreeBytes(SigmaSerializer.startReader(ergoTree.bytes))._4
  }
}
