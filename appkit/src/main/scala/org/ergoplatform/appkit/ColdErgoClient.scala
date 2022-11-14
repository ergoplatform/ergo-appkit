package org.ergoplatform.appkit

import java.util.function
import org.ergoplatform.restapi.client
import org.ergoplatform.appkit.impl.{ColdBlockchainContext, NodeInfoParameters}
import org.ergoplatform.restapi.client.NodeInfo

class ColdErgoClient(networkType: NetworkType, params: BlockchainParameters) extends ErgoClient {

  /**
    * Convenience constructor for giving maxBlockCost
    */
  def this(networkType: NetworkType, maxBlockCost: Int, blockVersion: Byte) {
    this(networkType, new NodeInfoParameters(
      new NodeInfo().parameters(new client.Parameters()
        .maxBlockCost(Integer.valueOf(maxBlockCost))
        .blockVersion(Integer.valueOf(blockVersion)))))
  }

  override def execute[T](action: function.Function[BlockchainContext, T]): T = {
    val ctx = new ColdBlockchainContext(networkType, params)
    val res = action.apply(ctx)
    res
  }
  override def getDataSource: BlockchainDataSource = ???
}
