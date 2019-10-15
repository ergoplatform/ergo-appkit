package org.ergoplatform.polyglot

import java.math.BigInteger
import java.util

import org.bouncycastle.util.BigIntegers
import org.ergoplatform.ErgoBox
import org.ergoplatform.ErgoBox.TokenId
import scorex.crypto.hash.{Digest32, Blake2b256}
import sigmastate.Values.ErgoTree
import sigmastate.basics.DLogProtocol.DLogProverInput
import sigmastate.{Values, TrivialProp}
import special.collection.Coll
import sigmastate.eval.Colls

import scala.collection.JavaConverters

object JavaHelpers {
  def toTokensColl[T](tokens: util.ArrayList[ErgoToken]): Coll[(Array[Byte], Long)] = {
    val ts = JavaConverters.asScalaIterator(tokens.iterator()).map(t => (t.getId(), t.getValue())).toArray
    Colls.fromArray(ts)
  }
  def toTokensSeq[T](tokens: util.ArrayList[ErgoToken]): Seq[(TokenId, Long)] = {
    val ts = JavaConverters.asScalaIterator(tokens.iterator()).map(t => (Digest32 @@ t.getId(), t.getValue())).toSeq
    ts
  }

  def toIndexedSeq[T](xs: util.ArrayList[T]): IndexedSeq[T] = {
    JavaConverters.asScalaIterator(xs.iterator()).toIndexedSeq
  }

  def createBox(value: Long, tokens: util.ArrayList[ErgoToken], creationHeight: Int): ErgoBox = {
    val ts = toTokensSeq(tokens)
    val tree = ErgoTree.fromSigmaBoolean(TrivialProp.TrueProp)
    ErgoBox(value, tree, creationHeight, ts)
  }

  def proverInputFromSeed(seedStr: String): DLogProverInput = {
      val secret = BigIntegers.fromUnsignedByteArray(Blake2b256.hash(1 + seedStr))
      DLogProverInput(secret)
  }

}