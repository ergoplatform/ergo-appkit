package org.ergoplatform.appkit

import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import scalan.RType
import special.collection.Coll
import com.google.common.base.Strings
import scala.collection.JavaConverters
import org.ergoplatform._
import org.ergoplatform.ErgoBox.{NonMandatoryRegisterId, TokenId}
import sigmastate.SType
import scorex.util.ModifierId
import sigmastate.Values.{ErgoTree, Constant, SValue, EvaluatedValue}
import sigmastate.serialization.{ValueSerializer, SigmaSerializer, GroupElementSerializer}
import scorex.crypto.authds.ADKey
import scorex.crypto.hash.Digest32
import org.ergoplatform.wallet.mnemonic.Mnemonic
import org.ergoplatform.settings.ErgoAlgos
import sigmastate.lang.Terms.ValueOps
import sigmastate.eval.{CompiletimeIRContext, Evaluation, Colls, CostingSigmaDslBuilder, CPreHeader}
import special.sigma.{Header, GroupElement, AnyValue, AvlTree, PreHeader}
import java.util
import java.lang.{Long => JLong}
import java.util.{Collections, List => JList}
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

  implicit def JListToIndexedSeq[A, B](implicit itemIso: Iso[A, B]): Iso[JList[A], IndexedSeq[B]] =
    new Iso[JList[A], IndexedSeq[B]] {
      override def to(as: JList[A]): IndexedSeq[B] = {
        JavaConverters.asScalaIterator(as.iterator()).map(itemIso.to).toIndexedSeq
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
        val bsIter = JavaConverters.asScalaIterator(as.iterator).map { a =>
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

  def createBoxCandidate(
                            value: Long, tree: ErgoTree,
                            tokens: util.List[ErgoToken],
                            registers: util.List[Tuple2[String, Object]], creationHeight: Int): ErgoBoxCandidate = {
    val ts = tokens.convertTo[Coll[(TokenId, Long)]]
    val rs = toIndexedSeq(registers).map { r =>
      val id = ErgoBox.registerByName(r._1).asInstanceOf[NonMandatoryRegisterId]
      val value = r._2.asInstanceOf[EvaluatedValue[_ <: SType]]
      id -> value
    }.toMap
    new ErgoBoxCandidate(value, tree, creationHeight, ts, rs)
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
