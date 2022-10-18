package org.ergoplatform.appkit

import scala.collection.mutable
import org.ergoplatform.wallet.boxes.DefaultBoxSelector
import org.ergoplatform.wallet.boxes.DefaultBoxSelector.NotEnoughCoinsForChangeBoxesError
import org.ergoplatform.wallet.boxes.DefaultBoxSelector.NotEnoughErgsError
import org.ergoplatform.wallet.boxes.DefaultBoxSelector.NotEnoughTokensError

import java.util.{List => JList, Map => JMap}
import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.Iso._
import org.ergoplatform.ErgoBox.TokenId
import org.ergoplatform.ErgoBoxAssets
import org.ergoplatform.ErgoBoxAssetsHolder
import org.ergoplatform.appkit.InputBoxesSelectionException.{NotEnoughErgsException, NotEnoughTokensException}
import scorex.util.{ModifierId, bytesToId}

import java.util


object BoxSelectorsJavaHelpers {

  final case class InputBoxWrapper(val inputBox: InputBox) extends ErgoBoxAssets {
    override def value: Long = inputBox.getValue
    override def tokens: Map[ModifierId, Long] = 
      inputBox.getTokens.convertTo[mutable.LinkedHashMap[ModifierId, Long]].toMap
  }

  def selectBoxes(unspentBoxes: JList[InputBox],
                  amountToSpend: Long,
                  tokensToSpend: JList[ErgoToken]): JList[InputBox] = {
    val inputBoxes = unspentBoxes.convertTo[IndexedSeq[InputBox]]
      .map(InputBoxWrapper.apply).toIterator
    val targetAssets = tokensToSpend.convertTo[mutable.LinkedHashMap[ModifierId, Long]].toMap
    val foundBoxes: IndexedSeq[InputBox] = new DefaultBoxSelector(None).select(inputBoxes, amountToSpend, targetAssets) match {
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
      case Right(v) => v.boxes.map(_.inputBox).toIndexedSeq
    }
    foundBoxes.convertTo[JList[InputBox]]
  }

}
