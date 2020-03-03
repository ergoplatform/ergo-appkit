package org.ergoplatform.appkit.impl

import java.util

import org.ergoplatform._
import org.ergoplatform.appkit.Parameters.{MinChangeValue, MinFee}
import org.ergoplatform.appkit._

import scala.collection.IndexedSeq
import scala.language.postfixOps

object TransactionOperations {

  def buildUnsignedErgoTx(inputs: IndexedSeq[ErgoBox],
                          dataInputs: IndexedSeq[DataInput],
                          outputCandidates: Seq[ErgoBoxCandidate],
                          feeAmount: Long,
                          changeAddress: ErgoAddress,
                          currentHeight: Int): UnsignedErgoLikeTransaction = {

    require(feeAmount > 0, "Fee amount should be defined")
    val inputTotal = inputs.map(_.value).sum
    val outputSum = outputCandidates.map(_.value).sum
    val outputTotal = outputSum + feeAmount
    val changeAmt = inputTotal - outputTotal
    val noChange = changeAmt < MinChangeValue
    // if computed changeAmt is too small give it to miner as tips
    val actualFee = if (noChange) feeAmount + changeAmt else feeAmount
    require(
      actualFee >= MinFee,
      s"Fee must be greater then minimum amount ($MinFee NanoErg)"
    )
    val feeOut = new ErgoBoxCandidate(
      actualFee,
      ErgoScriptPredef.feeProposition(Parameters.MinerRewardDelay),
      currentHeight
    )
    val changeOut =
      new ErgoBoxCandidate(changeAmt, changeAddress.script, currentHeight)

    val addedChangeOut = if (!noChange) Seq(changeOut) else Seq()

    val finalOutputCandidates = outputCandidates ++ Seq(feeOut) ++ addedChangeOut

    val mintedTokensNum = finalOutputCandidates
      .flatMap(_.additionalTokens.toArray)
      .count(t => util.Arrays.equals(t._1, inputs.head.id))

    require(
      mintedTokensNum <= 1,
      s"Only one token can be minted, but found $mintedTokensNum"
    )

    val tx = new UnsignedErgoLikeTransaction(
      inputs.map(b => new UnsignedInput(b.id)),
      dataInputs,
      finalOutputCandidates.toIndexedSeq
    )
    tx
  }
}
