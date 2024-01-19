package org.ergoplatform.appkit

import org.ergoplatform.ErgoBoxAssets
import org.ergoplatform.appkit.InputBoxesSelectionException.{NotEnoughErgsException, NotEnoughTokensException}
import org.ergoplatform.sdk.ErgoToken
import org.ergoplatform.sdk.JavaHelpers._
import org.ergoplatform.sdk.wallet.AssetUtils
import org.ergoplatform.wallet.boxes.DefaultBoxSelector.{NotEnoughCoinsForChangeBoxesError, NotEnoughErgsError, NotEnoughTokensError}
import scorex.util.{ModifierId, bytesToId}

import java.util
import java.util.{List => JList}
import scala.collection.mutable


object InputBoxesValidatorJavaHelper {
  import org.ergoplatform.sdk.SdkIsos._
  final case class InputBoxWrapper(inputBox: InputBox) extends ErgoBoxAssets {
    override def value: Long = inputBox.getValue

    override def tokens: Map[ModifierId, Long] = {
      val tokens = mutable.Map[ModifierId, Long]()
      inputBox.getTokens.convertTo[IndexedSeq[ErgoToken]].foreach { token: ErgoToken =>
        AssetUtils.mergeAssetsMut(tokens, Map.apply(bytesToId(token.getId.getBytes) -> token.getValue))
      }
      tokens.toMap
    }
  }

  def validateBoxes(unspentBoxes: JList[InputBox],
                    amountToSpend: Long,
                    tokensToSpend: JList[ErgoToken]): Unit = {
    val inputBoxes = unspentBoxes.convertTo[IndexedSeq[InputBox]]
      .map(InputBoxWrapper.apply)
    val targetAssets =  isoErgoTokenSeqToLinkedMap.to(tokensToSpend.convertTo[IndexedSeq[ErgoToken]]).toMap
    new InputBoxesValidator().select(inputBoxes.toIterator, amountToSpend, targetAssets) match {
      case Left(err: NotEnoughCoinsForChangeBoxesError) =>
        throw new InputBoxesSelectionException.NotEnoughCoinsForChangeException(err.message)
      case Left(err: NotEnoughErgsError) => {
        // we might have a ChangeBox error here as well, so let's report it correctly
        if (err.balanceFound >= amountToSpend) {
          throw new InputBoxesSelectionException.NotEnoughCoinsForChangeException(err.message)
        } else {
          throw new NotEnoughErgsException(err.message, err.balanceFound)
        }
      }
      case Left(err: NotEnoughTokensError) => {
        val tokensHm = err.tokensFound.foldLeft(new util.HashMap[String, java.lang.Long])((hm, elem) => {
          hm.put(elem._1.base16, elem._2)
          hm
        })
        throw new NotEnoughTokensException(err.message, tokensHm)
      }
      case Left(err) =>
        throw new InputBoxesSelectionException(
          s"Not enough funds in boxes to pay $amountToSpend nanoERGs, \ntokens: $tokensToSpend, \nreason: $err")
      case Right(v) => // do nothing, everything alright
    }
  }

}
