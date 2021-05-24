package org.ergoplatform.appkit.impl

import org.ergoplatform.ErgoBoxCandidate
import org.ergoplatform.appkit.{ErgoId, ErgoToken, OutBox}
import scorex.util.ModifierId

class OutBoxImpl(_ctx: BlockchainContextImpl,
                 _ergoBoxCandidate: ErgoBoxCandidate) extends OutBox {
  override def getValue: Long = _ergoBoxCandidate.value

  override def getCreationHeight: Int = _ergoBoxCandidate.creationHeight

  override def token(id: ErgoId): ErgoToken = null

  private[impl] def getErgoBoxCandidate: ErgoBoxCandidate = _ergoBoxCandidate

  override def convertToInputWith(txId: String, boxIndex: Short) = {
    val box = _ergoBoxCandidate.toBox(ModifierId @@ txId, boxIndex)
    new InputBoxImpl(_ctx, box)
  }
}

