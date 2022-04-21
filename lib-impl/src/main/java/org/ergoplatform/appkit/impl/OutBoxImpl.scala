package org.ergoplatform.appkit.impl

import org.ergoplatform.ErgoBoxCandidate
import org.ergoplatform.appkit.{ErgoToken, ErgoValue, InputBox, Iso, JavaHelpers, OutBox}
import scorex.util.ModifierId
import sigmastate.Values

import java.util

class OutBoxImpl(_ergoBoxCandidate: ErgoBoxCandidate) extends OutBox {
  override def getValue: Long = _ergoBoxCandidate.value

  override def getCreationHeight: Int = _ergoBoxCandidate.creationHeight

  override def getTokens: util.List[ErgoToken] = Iso.isoTokensListToPairsColl.from(_ergoBoxCandidate.additionalTokens)

  override def getRegisters: util.List[ErgoValue[_]] = JavaHelpers.getBoxRegisters(_ergoBoxCandidate)

  override def getBytesWithNoRef: Array[Byte] = _ergoBoxCandidate.bytesWithNoRef

  override def getErgoTree: Values.ErgoTree = _ergoBoxCandidate.ergoTree

  private[impl] def getErgoBoxCandidate: ErgoBoxCandidate = _ergoBoxCandidate

  override def convertToInputWith(txId: String, boxIndex: Short): InputBox = {
    val box = _ergoBoxCandidate.toBox(ModifierId @@ txId, boxIndex)
    new InputBoxImpl(box)
  }
}

