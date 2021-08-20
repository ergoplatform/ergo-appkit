package org.ergoplatform.appkit.impl

import java.util

import org.ergoplatform.appkit.{InputBox, CoveringBoxes, ErgoProverBuilder, UnsignedTransactionBuilder, Address, ErgoWallet, Constants, ErgoToken, PreHeaderBuilder, SignedTransaction, NetworkType, ErgoContract, BlockchainContext}
import org.ergoplatform.restapi.client.{ApiClient, NodeInfo, Parameters}
import sigmastate.Values

class ColdBlockchainContext(networkType: NetworkType, params: Parameters) extends BlockchainContextBase(networkType) {
  override def getApiClient: ApiClient = ???

  private val _nodeInfo = new NodeInfo()
    .parameters(params)

  override def getNodeInfo: NodeInfo = _nodeInfo

  override def createPreHeader(): PreHeaderBuilder = ???

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
