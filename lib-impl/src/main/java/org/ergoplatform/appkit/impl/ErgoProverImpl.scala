package org.ergoplatform.appkit.impl

import java.util
import org.ergoplatform.{sdk, P2PKAddress}
import org.ergoplatform.appkit._
import org.ergoplatform.sdk.wallet.secrets.ExtendedSecretKey
import sigmastate.eval.CostingSigmaDslBuilder
import sigmastate.interpreter.HintsBag
import special.sigma.BigInt
import sigmastate.utils.Helpers._
import org.ergoplatform.sdk.JavaHelpers.UniversalConverter
import org.ergoplatform.sdk.{AppkitProvingInterpreter, UnreducedTransaction}

class ErgoProverImpl(_ctx: BlockchainContextBase,
                     _prover: AppkitProvingInterpreter) extends ErgoProver {
  private def networkPrefix = _ctx.getNetworkType.networkPrefix

  override def getP2PKAddress: P2PKAddress = {
    val pk = _prover.pubKeys(0)
    sdk.JavaHelpers.createP2PKAddress(pk, networkPrefix)
  }

  override def getAddress = new Address(getP2PKAddress)

  override def getSecretKey: BigInt =
    CostingSigmaDslBuilder.BigInt(_prover.secretKeys(0).privateInput.w)

  override def getEip3Addresses: util.List[Address] = {
    val addresses = _prover.secretKeys
      .convertTo[IndexedSeq[ExtendedSecretKey]]
      .drop(1)
      .map { k =>
        val p2pkAddress = sdk.JavaHelpers.createP2PKAddress(k.publicImage, networkPrefix)
        new Address(p2pkAddress)
      }
    addresses.convertTo[util.List[Address]]
  }

  override def sign(tx: UnsignedTransaction): SignedTransaction =
    sign(tx, baseCost = 0)

  override def sign(tx: UnsignedTransaction, baseCost: Int): SignedTransaction = {
    val txImpl = tx.asInstanceOf[UnsignedTransactionImpl]
    val boxesToSpend = sdk.JavaHelpers.toIndexedSeq(txImpl.getBoxesToSpend)
    val dataBoxes = sdk.JavaHelpers.toIndexedSeq(txImpl.getDataBoxes)
    val tokensToBurn = sdk.JavaHelpers.toIndexedSeq(txImpl.getTokensToBurn)
    val unreduced = UnreducedTransaction(txImpl.getTx, boxesToSpend, dataBoxes, tokensToBurn)
    val signed = _prover.sign(
      unreducedTx = unreduced,
      stateContext = txImpl.getStateContext,
      baseCost = baseCost).getOrThrow
    new SignedTransactionImpl(_ctx, signed.ergoTx, signed.cost)
  }

  override def signMessage(sigmaProp: SigmaProp, message:  Array[Byte], hintsBag: HintsBag): Array[Byte] = {
    _prover.signMessage(sigmaProp.getSigmaBoolean, message, hintsBag).getOrThrow
  }

  override def reduce(tx: UnsignedTransaction, baseCost: Int): ReducedTransaction = {
    val txImpl = tx.asInstanceOf[UnsignedTransactionImpl]
    val boxesToSpend = sdk.JavaHelpers.toIndexedSeq(txImpl.getBoxesToSpend)
    val dataBoxes = sdk.JavaHelpers.toIndexedSeq(txImpl.getDataBoxes)
    val tokensToBurn = sdk.JavaHelpers.toIndexedSeq(txImpl.getTokensToBurn)
    val reduced = _prover.reduceTransaction(
      unsignedTx = txImpl.getTx,
      boxesToSpend = boxesToSpend,
      dataBoxes = dataBoxes,
      stateContext = txImpl.getStateContext,
      baseCost = baseCost,
      tokensToBurn = tokensToBurn)
    new ReducedTransactionImpl(_ctx, reduced)
  }

  override def signReduced(tx: ReducedTransaction, baseCost: Int): SignedTransaction = {
    val signed = _prover.signReduced(sdk.ReducedTransaction(tx.getTx), baseCost)
    new SignedTransactionImpl(_ctx, signed.ergoTx, signed.cost)
  }

}

