package org.ergoplatform.appkit

import org.ergoplatform.wallet.boxes.BoxSelectors.SelectionResult
import org.ergoplatform.wallet.boxes.BoxSelectors
import org.ergoplatform.wallet.boxes.Box
import java.util.{List => JList, Map => JMap}
import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.Iso._
import org.ergoplatform.ErgoBox.TokenId
import scorex.util.{bytesToId, ModifierId}
import org.ergoplatform.wallet.boxes.GenericBox


object BoxSelectorsJavaHelpers {

  final case class InputBoxWrapper(val inputBox: InputBox) extends Box {
    override def value: Long = inputBox.getValue
    override def assets: Map[ModifierId, Long] = inputBox.getTokens.convertTo[Map[ModifierId, Long]]
  }

  def selectBoxes(unspentBoxes: JList[InputBox],
                  amountToSpend: Long, 
                  tokensToSpend: JList[ErgoToken]): JList[InputBox] = {
    val inputBoxes = unspentBoxes.convertTo[IndexedSeq[InputBox]]
      .map(InputBoxWrapper.apply).toIterator
    val targetAssets = tokensToSpend.convertTo[Map[ModifierId, Long]]
    val noFilter = { b: Box => true}
    val foundBoxes: IndexedSeq[InputBox] = BoxSelectors.select(inputBoxes, noFilter, amountToSpend, targetAssets)
      .toSeq
      .flatMap { _.boxes.map(_.inputBox) }
      .toIndexedSeq
    foundBoxes.convertTo[JList[InputBox]]
  }

}
