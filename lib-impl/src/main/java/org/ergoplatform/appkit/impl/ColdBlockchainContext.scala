package org.ergoplatform.appkit.impl

import org.ergoplatform.appkit._

import java.util

class ColdBlockchainContext(networkType: NetworkType, params: BlockchainParameters) extends BlockchainContextBase(networkType) {
  override def getDataSource: BlockchainDataSource = throw new UnsupportedOperationException("Cold blockchain context has no data source.")

  override def getParameters: BlockchainParameters = params

  override def createPreHeader(): PreHeaderBuilder = throw new UnsupportedOperationException("Cold blockchain context has no pre header builder.")

  override def signedTxFromJson(json: String): SignedTransaction = ???

  override def newTxBuilder(): UnsignedTransactionBuilder = ???

  override def getBoxesById(boxIds: String*): Array[InputBox] = ???

  override def newProverBuilder(): ErgoProverBuilder = new ErgoProverBuilderImpl(this)

  override def getHeight: Int = ???

  override def sendTransaction(tx: SignedTransaction): String = ???

  override def getUnspentBoxesFor(address: Address,
                                  offset: Int,
                                  limit: Int): util.List[InputBox] = ???

  override def getCoveringBoxesFor(address: Address,
                                   amountToSpend: Long,
                                   tokensToSpend: util.List[ErgoToken]): CoveringBoxes = ???
}
