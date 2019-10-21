package org.ergoplatform.polyglot

import java.math.BigInteger
import java.util

import org.bouncycastle.util.BigIntegers
import org.ergoplatform.ErgoAddressEncoder.NetworkPrefix
import org.ergoplatform.{ErgoBox, UnsignedInput, ErgoScriptPredef}
import org.ergoplatform.ErgoBox.TokenId
import org.ergoplatform.settings.ErgoAlgos
import scalan.RType
import scorex.crypto.authds.ADKey
import scorex.crypto.hash.{Digest32, Blake2b256}
import scorex.util.encode.Base16
import sigmastate.Values.ErgoTree
import sigmastate.basics.DLogProtocol.DLogProverInput
import sigmastate.{Values, TrivialProp}
import sigmastate.lang.Terms.ValueOps
import special.collection.Coll
import sigmastate.eval.{CostingSigmaDslBuilder, CPreHeader, CompiletimeIRContext, Colls}
import sigmastate.interpreter.Interpreter.ScriptEnv
import special.sigma.{PreHeader, Header}

import scala.collection.JavaConverters

object JavaHelpers {
  val HeaderRType: RType[Header] = special.sigma.HeaderRType
  val PreHeaderRType: RType[PreHeader] = special.sigma.PreHeaderRType

  def Algos: ErgoAlgos =  org.ergoplatform.settings.ErgoAlgos
  def toHeaders(): Coll[Header] = Colls.emptyColl
  def toPreHeader(): PreHeader = CPreHeader(9, Colls.emptyColl, 0, 0, 0, null, Colls.emptyColl)

  def toTokensColl[T](tokens: util.ArrayList[ErgoToken]): Coll[(Array[Byte], Long)] = {
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

  def createBox(value: Long, tree: ErgoTree, tokens: util.List[ErgoToken], creationHeight: Int): ErgoBox = {
    val ts = toTokensSeq(tokens)
    ErgoBox(value, tree, creationHeight, ts)
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