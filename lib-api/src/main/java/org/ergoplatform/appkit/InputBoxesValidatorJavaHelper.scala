package org.ergoplatform.appkit

import org.ergoplatform.ErgoBoxAssets
import org.ergoplatform.appkit.InputBoxesSelectionException.{NotEnoughErgsException, NotEnoughTokensException}
import org.ergoplatform.sdk.ErgoToken
import org.ergoplatform.sdk.wallet.AssetUtils
import org.ergoplatform.wallet.boxes.DefaultBoxSelector.{NotEnoughCoinsForChangeBoxesError, NotEnoughErgsError, NotEnoughTokensError}
import scorex.util.{ModifierId, bytesToId}

import java.util
import java.util.{List => JList}
import scala.collection.JavaConverters._
import scala.collection.mutable


object InputBoxesValidatorJavaHelper {

  final case class InputBoxWrapper(val inputBox: InputBox) extends ErgoBoxAssets {
    override def value: Long = inputBox.getValue

    override def tokens: Map[ModifierId, Long] = {
      val tokens = mutable.Map[ModifierId, Long]()
      inputBox.getTokens.asScala.foreach { token: ErgoToken =>
        AssetUtils.mergeAssetsMut(tokens, Map.apply(bytesToId(token.getId.getBytes) -> token.getValue))
      }
      tokens.toMap
    }
  }

  def validateBoxes(unspentBoxes: JList[InputBox],
                    amountToSpend: Long,
                    tokensToSpend: JList[ErgoToken]): Unit = {
    val inputBoxes = unspentBoxes.asScala.toIndexedSeq
      .map(InputBoxWrapper(_))
    val targetAssets = tokensToSpend.asScala.map { t =>
      bytesToId(t.getId.getBytes) -> t.getValue
    }.toMap
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
          hm.put(elem._1.toString, elem._2)
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
