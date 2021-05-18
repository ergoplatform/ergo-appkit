package org.ergoplatform.appkit.testing

import org.ergoplatform.appkit.{AppkitTestingCommon, BlockchainContext, ConstantsBuilder, ErgoContract}

trait AppkitTesting extends AppkitTestingCommon {

  /** Creates a contract with True proposition, which is always valid. */
  def truePropContract(ctx: BlockchainContext): ErgoContract = {
    ctx.compileContract(ConstantsBuilder.empty(),"{sigmaProp(true)}")
  }

}
