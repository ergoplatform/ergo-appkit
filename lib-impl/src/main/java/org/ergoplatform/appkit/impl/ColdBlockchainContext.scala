package org.ergoplatform.appkit.impl

import java.util
import org.ergoplatform.appkit.{Address, BlockchainContext, BlockchainDataSource, BlockchainParameters, Constants, CoveringBoxes, ErgoContract, ErgoProverBuilder, ErgoToken, ErgoWallet, InputBox, NetworkType, PreHeaderBuilder, SignedTransaction, UnsignedTransactionBuilder}
import org.ergoplatform.restapi.client.{ApiClient, NodeInfo, Parameters}
import sigmastate.Values

class ColdBlockchainContext(networkType: NetworkType, params: Parameters) extends BlockchainContextBase(networkType) {
  override def getDataSource: BlockchainDataSource = throw new UnsupportedOperationException("Cold blockchain context has no data source.")

  private val _params = new NodeInfoParameters(new NodeInfo()
    .parameters(params))

  override def getParameters: BlockchainParameters = _params

  override def createPreHeader(): PreHeaderBuilder = throw new UnsupportedOperationException("Cold blockchain context has no pre header builder.")

  override def signedTxFromJson(json: String): SignedTransaction = ???

  override def newTxBuilder(): UnsignedTransactionBuilder = ???

  override def getBoxesById(boxIds: String*): Array[InputBox] = ???

  override def newProverBuilder(): ErgoProverBuilder = new ErgoProverBuilderImpl(this)

  override def getHeight: Int = ???

  override def sendTransaction(tx: SignedTransaction): String = ???

  override def getWallet: ErgoWallet = ???

  override def getUnspentBoxesFor(address: Address,
                                  offset: Int,
                                  limit: Int): util.List[InputBox] = ???

  override def getCoveringBoxesFor(address: Address,
                                   amountToSpend: Long,
                                   tokensToSpend: util.List[ErgoToken]): CoveringBoxes = ???
}
