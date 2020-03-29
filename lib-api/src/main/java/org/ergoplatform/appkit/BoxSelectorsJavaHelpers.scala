package org.ergoplatform.appkit

import org.ergoplatform.wallet.boxes.DefaultBoxSelector
import java.util.{List => JList, Map => JMap}
import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.Iso._
import org.ergoplatform.ErgoBox.TokenId
import org.ergoplatform.ErgoBoxAssets
import org.ergoplatform.ErgoBoxAssetsHolder
import scorex.util.{bytesToId, ModifierId}


object BoxSelectorsJavaHelpers {

  final case class InputBoxWrapper(val inputBox: InputBox) extends ErgoBoxAssets {
    override def value: Long = inputBox.getValue
    override def tokens: Map[ModifierId, Long] = inputBox.getTokens.convertTo[Map[ModifierId, Long]]
  }

  def selectBoxes(unspentBoxes: JList[InputBox],
                  amountToSpend: Long, 
                  tokensToSpend: JList[ErgoToken]): JList[InputBox] = {
    val inputBoxes = unspentBoxes.convertTo[IndexedSeq[InputBox]]
      .map(InputBoxWrapper.apply).toIterator
    val targetAssets = tokensToSpend.convertTo[Map[ModifierId, Long]]
    val foundBoxes: IndexedSeq[InputBox] = DefaultBoxSelector.select(inputBoxes, amountToSpend, targetAssets)
      .toSeq
      .flatMap { _.boxes.map(_.inputBox) }
      .toIndexedSeq
    foundBoxes.convertTo[JList[InputBox]]
  }

}
