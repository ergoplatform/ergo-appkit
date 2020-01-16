package org.ergoplatform.appkit

import org.ergoplatform.validation.ValidationRules
import org.ergoplatform.wallet.interpreter.ErgoInterpreter
import sigmastate.basics.DLogProtocol.{ProveDlog, DLogProverInput}
import java.util
import java.util.{List => JList}

import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import sigmastate.basics.{SigmaProtocol, SigmaProtocolPrivateInput, SigmaProtocolCommonInput, DiffieHellmanTupleProverInput}
import org.ergoplatform._
import org.ergoplatform.wallet.protocol.context.{ErgoLikeParameters, ErgoLikeStateContext, TransactionContext}

import scala.util.{Success, Failure, Try}
import sigmastate.eval.CompiletimeIRContext
import sigmastate.interpreter.{ContextExtension, ProverInterpreter}

/**
 * A class which holds secrets and can sign transactions (aka generate proofs).
 *
 * @param secretKeys secrets in extended form to be used by prover
 * @param dhtInputs  prover inputs containing secrets for generating proofs for ProveDHTuple nodes.
 * @param params     ergo blockchain parameters
 */
class AppkitProvingInterpreter(
      val secretKeys: JList[ExtendedSecretKey],
      val dhtInputs: JList[DiffieHellmanTupleProverInput],
      params: ErgoLikeParameters)
  extends ErgoLikeInterpreter()(new CompiletimeIRContext) with ProverInterpreter {

  override type CTX = ErgoLikeContext
  import Iso._

  val secrets: Seq[SigmaProtocolPrivateInput[_ <: SigmaProtocol[_], _ <: SigmaProtocolCommonInput[_]]] = {
    val dlogs = JListToIndexedSeq(identityIso[ExtendedSecretKey]).to(secretKeys).map(_.key)
    val dhts = JListToIndexedSeq(identityIso[DiffieHellmanTupleProverInput]).to(dhtInputs)
    dlogs ++ dhts
  }

  val pubKeys: Seq[ProveDlog] = secrets
    .filter { case _: DLogProverInput => true case _ => false}
    .map(_.asInstanceOf[DLogProverInput].publicImage)

  /**
   * @note requires `unsignedTx` and `boxesToSpend` have the same boxIds in the same order.
   */
  def sign(unsignedTx: UnsignedErgoLikeTransaction,
           boxesToSpend: IndexedSeq[ErgoBox],
           dataBoxes: IndexedSeq[ErgoBox],
           stateContext: ErgoLikeStateContext): Try[ErgoLikeTransaction] = {
    if (unsignedTx.inputs.length != boxesToSpend.length) Failure(new Exception("Not enough boxes to spend"))
    else
    if (unsignedTx.dataInputs.length != dataBoxes.length) Failure(new Exception("Not enough data boxes"))
    else {
      // Cost of transaction initialization: we should read and parse all inputs and data inputs,
      // and also iterate through all outputs to check rules
      val inputsCost = boxesToSpend.size * params.inputCost
      val dataInputsCost = dataBoxes.size * params.dataInputCost
      val outputsCost = unsignedTx.outputCandidates.size * params.outputCost
      val initialCost: Long = inputsCost + dataInputsCost + outputsCost

      val provedInputsTry = boxesToSpend
        .zipWithIndex
        .foldLeft(Try(IndexedSeq[Input]() -> initialCost)) { case (inputsCostTry, (inputBox, boxIdx)) =>
          val unsignedInput = unsignedTx.inputs(boxIdx)
          require(util.Arrays.equals(unsignedInput.boxId, inputBox.id))

          val transactionContext = TransactionContext(boxesToSpend, dataBoxes, unsignedTx, boxIdx.toShort)

          inputsCostTry.flatMap { case (inputsAcc, totalCost) =>

            val context = new ErgoLikeContext(ErgoInterpreter.avlTreeFromDigest(stateContext.previousStateDigest),
              stateContext.sigmaLastHeaders,
              stateContext.sigmaPreHeader,
              transactionContext.dataBoxes,
              transactionContext.boxesToSpend,
              transactionContext.spendingTransaction,
              transactionContext.selfIndex,
              ContextExtension.empty,
              ValidationRules.currentSettings,
              params.maxBlockCost,
              totalCost
            )

            prove(inputBox.ergoTree, context, unsignedTx.messageToSign).flatMap { proverResult =>
              val newTC = totalCost + proverResult.cost
              if (newTC > context.costLimit)
                Failure(new Exception(s"Cost of transaction $unsignedTx exceeds limit ${context.costLimit}"))
              else
                Success((inputsAcc :+ Input(unsignedInput.boxId, proverResult)) -> newTC)
            }
          }
        }
      provedInputsTry.map { case (inputs, _) =>
        new ErgoLikeTransaction(inputs, unsignedTx.dataInputs, unsignedTx.outputCandidates)
      }
    }
  }

}
