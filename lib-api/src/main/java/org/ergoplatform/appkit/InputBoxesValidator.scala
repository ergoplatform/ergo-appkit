package org.ergoplatform.appkit

import org.ergoplatform.wallet.Constants.MaxAssetsPerBox
import org.ergoplatform.wallet.boxes.BoxSelector._
import org.ergoplatform.wallet.boxes.DefaultBoxSelector._
import org.ergoplatform.wallet.boxes.{BoxSelector, ReemissionData}
import org.ergoplatform.wallet.{AssetUtils, TokensMap}
import org.ergoplatform.{ErgoBoxAssets, ErgoBoxAssetsHolder}
import scorex.util.ModifierId
import sigmastate.utils.Helpers.EitherOps

import scala.collection.mutable

/**
  * Pass through implementation of the box selector. Unlike DefaultBoxSelector from ergo-wallet,
  * it does not select input boxes. We do this in appkit ourselves and do not need the selector
  * to interfere with how we built our transaction. Instead, this selector performs validation
  * and calculates the necessary change box
  */
class InputBoxesValidator extends BoxSelector {

  override def reemissionDataOpt: Option[ReemissionData] = None

  override def select[T <: ErgoBoxAssets](inputBoxes: Iterator[T],
                                          externalFilter: T => Boolean,
                                          targetBalance: Long,
                                          targetAssets: TokensMap): Either[BoxSelectionError, BoxSelectionResult[T]] = {
    //mutable structures to collect results
    val res = mutable.Buffer[T]()
    var currentBalance = 0L
    val currentAssets = mutable.Map[ModifierId, Long]()

    // select all input boxes - we only validate here
    while (inputBoxes.hasNext) {
      val box = inputBoxes.next()
      currentBalance = currentBalance + box.value
      AssetUtils.mergeAssetsMut(currentAssets, box.tokens)
      res += box
    }

    if (currentBalance - targetBalance >= 0) {
      //now check if we found all tokens
      if (targetAssets.forall {
        case (id, targetAmt) => currentAssets.getOrElse(id, 0L) >= targetAmt
      }) {
        formChangeBoxes(currentBalance, targetBalance, currentAssets, targetAssets).mapRight { changeBoxes =>
          BoxSelectionResult(res, changeBoxes)
        }
      } else {
        Left(NotEnoughTokensError(
          s"Not enough tokens in input boxes to send $targetAssets (found only $currentAssets)", currentAssets.toMap)
        )
      }
    } else {
      Left(NotEnoughErgsError(
        s"not enough boxes to meet ERG needs $targetBalance (found only $currentBalance)", currentBalance)
      )
    }
  }

  /**
    * Helper method to construct change outputs
    *
    * @param foundBalance    - ERG balance of boxes collected
    *                        (spendable only, so after possibly deducting re-emission tokens)
    * @param targetBalance   - ERG amount to be transferred to recipients
    * @param foundBoxAssets  - assets balances of boxes
    * @param targetBoxAssets - assets amounts to be transferred to recipients
    * @return
    */
  def formChangeBoxes(foundBalance: Long,
                      targetBalance: Long,
                      foundBoxAssets: mutable.Map[ModifierId, Long],
                      targetBoxAssets: TokensMap): Either[BoxSelectionError, Seq[ErgoBoxAssets]] = {
    AssetUtils.subtractAssetsMut(foundBoxAssets, targetBoxAssets)
    val changeBoxesAssets: Seq[mutable.Map[ModifierId, Long]] = foundBoxAssets.grouped(MaxAssetsPerBox).toSeq
    val changeBalance = foundBalance - targetBalance
    //at least a minimum amount of ERG should be assigned per a created box
    if (changeBoxesAssets.size * MinBoxValue > changeBalance) {
      Left(NotEnoughCoinsForChangeBoxesError(
        s"Not enough nanoERGs ($changeBalance nanoERG) to create ${changeBoxesAssets.size} change boxes, \nfor $changeBoxesAssets"
      ))
    } else {
      val changeBoxes = if (changeBoxesAssets.nonEmpty) {
        val baseChangeBalance = changeBalance / changeBoxesAssets.size

        val changeBoxesNoBalanceAdjusted = changeBoxesAssets.map { a =>
          ErgoBoxAssetsHolder(baseChangeBalance, a.toMap)
        }

        val modifiedBoxOpt = changeBoxesNoBalanceAdjusted.headOption.map { firstBox =>
          ErgoBoxAssetsHolder(
            changeBalance - baseChangeBalance * (changeBoxesAssets.size - 1),
            firstBox.tokens
          )
        }

        modifiedBoxOpt.toSeq ++ changeBoxesNoBalanceAdjusted.tail
      } else if (changeBalance > 0) {
        Seq(ErgoBoxAssetsHolder(changeBalance))
      } else {
        Seq.empty
      }

      Right(changeBoxes)
    }
  }

}
