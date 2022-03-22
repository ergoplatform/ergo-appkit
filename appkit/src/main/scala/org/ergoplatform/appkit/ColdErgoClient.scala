package org.ergoplatform.appkit

import java.util.function
import org.ergoplatform.restapi.client
import org.ergoplatform.appkit.impl.{BlockchainContextBuilderImpl, ColdBlockchainContext}

class ColdErgoClient(networkType: NetworkType, params: client.Parameters) extends ErgoClient {
  override def execute[T](action: function.Function[BlockchainContext, T]): T = {
    val ctx = new ColdBlockchainContext(networkType, params)
    val res = action.apply(ctx)
    res
  }
  override def getDataSource: BlockchainDataSource = ???
}
