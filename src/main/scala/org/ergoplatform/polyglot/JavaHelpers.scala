package org.ergoplatform.polyglot

import java.math.BigInteger
import java.util

import com.google.common.base.Strings
import org.bouncycastle.util.BigIntegers
import org.ergoplatform.ErgoAddressEncoder.NetworkPrefix
import org.ergoplatform.{ErgoBox, UnsignedInput, ErgoScriptPredef}
import org.ergoplatform.ErgoBox.{NonMandatoryRegisterId, TokenId}
import org.ergoplatform.api.client.BlockHeader
import org.ergoplatform.settings.ErgoAlgos
import org.ergoplatform.wallet.interpreter.ErgoInterpreter
import org.ergoplatform.wallet.mnemonic.Mnemonic
import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import org.ergoplatform.wallet.serialization.JsonCodecsWrapper
import scalan.RType
import scorex.crypto.authds.{ADDigest, ADKey}
import scorex.crypto.hash.{Digest32, Blake2b256}
import scorex.util.ModifierId
import scorex.util.encode.Base16
import sigmastate.Values.{ErgoTree, Constant, SValue, EvaluatedValue}
import sigmastate.basics.DLogProtocol.DLogProverInput
import sigmastate.{Values, TrivialProp, SType}
import sigmastate.lang.Terms.ValueOps
import special.collection.Coll
import sigmastate.eval.{CompiletimeIRContext, Evaluation, SigmaDsl, CAvlTree, Colls, CostingSigmaDslBuilder, CPreHeader, CHeader}
import sigmastate.interpreter.CryptoConstants
import sigmastate.interpreter.Interpreter.ScriptEnv
import sigmastate.serialization.{ValueSerializer, SigmaSerializer, GroupElementSerializer}
import special.sigma.{Header, GroupElement, AnyValue, AvlTree, PreHeader}

import scala.collection.JavaConverters


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

  def toHeaders(headers: util.List[BlockHeader]): Coll[Header] = {
    val hs = headers.toArray(new Array[BlockHeader](0)).map { h =>
      CHeader(
        id = h.getId().toColl,
        version = h.getVersion().toByte,
        parentId = h.getParentId().toColl,
        ADProofsRoot = h.getAdProofsRoot.toColl,
        stateRoot = CAvlTree(ErgoInterpreter.avlTreeFromDigest(ADDigest @@ h.getStateRoot().toBytes)),
        transactionsRoot = h.getTransactionsRoot().toColl,
        timestamp = h.getTimestamp(),
        nBits = h.getNBits(),
        height = h.getHeight,
        extensionRoot = h.getExtensionHash().toColl,
        minerPk = h.getPowSolutions.getPk().toGroupElement,
        powOnetimePk = h.getPowSolutions().getW().toGroupElement,
        powNonce = h.getPowSolutions().getN().toColl,
        powDistance = SigmaDsl.BigInt(h.getPowSolutions().getD().toBigIntegerExact),
        votes = h.getVotes().toColl
      ): Header
    }
    Colls.fromArray(hs)
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

  def compile(constants: util.Dictionary[String, Object], contractText: String, networkPrefix: NetworkPrefix): ErgoTree = {
    val env = JavaConverters.dictionaryAsScalaMap(constants).toMap
    implicit val IR = new CompiletimeIRContext
    val prop = ErgoScriptPredef.compileWithCosting(env, contractText, networkPrefix).asSigmaProp
    ErgoTree.fromProposition(prop)
  }

  private def anyValueToConstant(v: AnyValue): Constant[_ <: SType] = {
    val tpe = Evaluation.rtypeToSType(v.tVal)
    Constant(v.value.asInstanceOf[SType#WrappedType], tpe)
  }

  def createBox(value: Long, tree: ErgoTree, tokens: util.List[ErgoToken], registers: util.List[scala.Tuple2[String, Object]], txId: String, index: Short, creationHeight: Int): ErgoBox = {
    val ts = toTokensColl(tokens)
    val rs = toIndexedSeq(registers).map { r =>
      val id = ErgoBox.registerByName(r._1).asInstanceOf[NonMandatoryRegisterId]
      val value = r._2.asInstanceOf[EvaluatedValue[_ <: SType]]
      id -> value
    }.toMap
    new ErgoBox(value, tree, ts.asInstanceOf[Coll[(TokenId, Long)]], rs, ModifierId @@ txId, index, creationHeight)
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

}