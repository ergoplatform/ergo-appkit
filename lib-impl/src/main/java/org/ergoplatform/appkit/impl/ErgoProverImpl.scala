package org.ergoplatform.appkit.impl

import java.util

import org.ergoplatform.P2PKAddress
import org.ergoplatform.appkit._
import org.ergoplatform.sdk.wallet.secrets.ExtendedSecretKey
import sigmastate.eval.CostingSigmaDslBuilder
import sigmastate.interpreter.HintsBag
import special.sigma.BigInt
import sigmastate.utils.Helpers._  // don't remove, required for Scala 2.11
import JavaHelpers._

class ErgoProverImpl(_ctx: BlockchainContextBase,
                     _prover: AppkitProvingInterpreter) extends ErgoProver {
  private def networkPrefix = _ctx.getNetworkType.networkPrefix

  override def getP2PKAddress: P2PKAddress = {
    val pk = _prover.pubKeys(0)
    JavaHelpers.createP2PKAddress(pk, networkPrefix)
  }

  override def getAddress = new Address(getP2PKAddress)

  override def getSecretKey: BigInt =
    CostingSigmaDslBuilder.BigInt(_prover.secretKeys.get(0).privateInput.w)

  override def getEip3Addresses: util.List[Address] = {
    val addresses = _prover.secretKeys
      .convertTo[IndexedSeq[ExtendedSecretKey]]
      .drop(1)
      .map { k =>
        val p2pkAddress = JavaHelpers.createP2PKAddress(k.publicImage, networkPrefix)
        new Address(p2pkAddress)
      }
    addresses.convertTo[util.List[Address]]
  }

  override def sign(tx: UnsignedTransaction): SignedTransaction =
    sign(tx, baseCost = 0)

  override def sign(tx: UnsignedTransaction, baseCost: Int): SignedTransaction = {
    val txImpl = tx.asInstanceOf[UnsignedTransactionImpl]
    val boxesToSpend = JavaHelpers.toIndexedSeq(txImpl.getBoxesToSpend)
    val dataBoxes = JavaHelpers.toIndexedSeq(txImpl.getDataBoxes)
    val (signed, cost) = _prover.sign(
      unsignedTx = txImpl.getTx,
      boxesToSpend = boxesToSpend,
      dataBoxes = dataBoxes,
      stateContext = txImpl.getStateContext,
      baseCost = baseCost, txImpl.getTokensToBurn).getOrThrow
    new SignedTransactionImpl(_ctx, signed, cost)
  }

  override def signMessage(sigmaProp: SigmaProp, message:  Array[Byte], hintsBag: HintsBag): Array[Byte] = {
    _prover.signMessage(sigmaProp.getSigmaBoolean, message, hintsBag).getOrThrow
  }

  override def reduce(tx: UnsignedTransaction, baseCost: Int): ReducedTransaction = {
    val txImpl = tx.asInstanceOf[UnsignedTransactionImpl]
    val boxesToSpend = JavaHelpers.toIndexedSeq(txImpl.getBoxesToSpend)
    val dataBoxes = JavaHelpers.toIndexedSeq(txImpl.getDataBoxes)
    val reduced = _prover.reduceTransaction(
      unsignedTx = txImpl.getTx,
      boxesToSpend = boxesToSpend,
      dataBoxes = dataBoxes,
      stateContext = txImpl.getStateContext,
      baseCost = baseCost,
      tokensToBurn = txImpl.getTokensToBurn)
    new ReducedTransactionImpl(_ctx, reduced)
  }

  override def signReduced(tx: ReducedTransaction, baseCost: Int): SignedTransaction = {
    val (signed, cost) = _prover.signReduced(tx.getTx, baseCost)
    new SignedTransactionImpl(_ctx, signed, cost)
  }

}

