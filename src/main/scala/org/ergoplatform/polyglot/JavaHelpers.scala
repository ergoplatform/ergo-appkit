package org.ergoplatform.polyglot

import java.math.BigInteger
import java.util

import org.bouncycastle.util.BigIntegers
import org.ergoplatform.ErgoAddressEncoder.NetworkPrefix
import org.ergoplatform.{ErgoBox, UnsignedInput, ErgoScriptPredef}
import org.ergoplatform.ErgoBox.{NonMandatoryRegisterId, TokenId}
import org.ergoplatform.settings.ErgoAlgos
import scalan.RType
import scorex.crypto.authds.ADKey
import scorex.crypto.hash.{Digest32, Blake2b256}
import scorex.util.ModifierId
import scorex.util.encode.Base16
import sigmastate.Values.{ErgoTree, Constant, SValue, EvaluatedValue}
import sigmastate.basics.DLogProtocol.DLogProverInput
import sigmastate.{Values, TrivialProp, SType}
import sigmastate.lang.Terms.ValueOps
import special.collection.Coll
import sigmastate.eval.{CompiletimeIRContext, Evaluation, Colls, CostingSigmaDslBuilder, CPreHeader}
import sigmastate.interpreter.CryptoConstants
import sigmastate.interpreter.Interpreter.ScriptEnv
import sigmastate.serialization.ValueSerializer
import special.sigma.{AnyValue, PreHeader, Header}

import scala.collection.JavaConverters

object JavaHelpers {
  val HeaderRType: RType[Header] = special.sigma.HeaderRType
  val PreHeaderRType: RType[PreHeader] = special.sigma.PreHeaderRType

  def Algos: ErgoAlgos =  org.ergoplatform.settings.ErgoAlgos

  def deserializeValue[T <: SValue](bytes: Array[Byte]): T = {
    ValueSerializer.deserialize(bytes).asInstanceOf[T]
  }

  def toHeaders(): Coll[Header] = Colls.emptyColl
  def toPreHeader(): PreHeader = {
    CPreHeader(9, Colls.emptyColl, 0, 0, 0,
      sigmastate.eval.SigmaDsl.groupGenerator, Colls.emptyColl)
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

  def proverInputFromSeed(seedStr: String): DLogProverInput = {
      val secret = BigIntegers.fromUnsignedByteArray(Blake2b256.hash(1 + seedStr))
      DLogProverInput(secret)
  }

  def decodeBase16(base16: String): Array[Byte] = Base16.decode(base16).get

  def createUnsignedInput(boxId: String): UnsignedInput = {
    val idBytes = decodeBase16(boxId)
    createUnsignedInput(idBytes)
  }

  def createUnsignedInput(boxIdBytes: Array[Byte]): UnsignedInput = {
    new UnsignedInput(ADKey @@ boxIdBytes)
  }

}