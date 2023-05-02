package org.ergoplatform.appkit

import org.ergoplatform.appkit.babelfee.{BabelFeeOperations, BabelFeeBoxContract, BabelFeeBoxState}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.util
import java.util.Arrays

class BabelFeeSpec extends AnyPropSpec with Matchers with ScalaCheckDrivenPropertyChecks
  with HttpClientTesting
  with AppkitTestingCommon {

  val mockTokenId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"

  property("babel fee box creation and revoke") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val creator = address

      val amountToSend = Parameters.OneErg * 100

      val input1 = ctx.newTxBuilder.outBoxBuilder
        .value(amountToSend + Parameters.MinFee)
        .contract(creator.toErgoContract)
        .build().convertToInputWith(mockTokenId, 0)

      val txCreate = BabelFeeOperations.createNewBabelContractTx(
        BoxOperations.createForSender(creator, ctx)
          .withAmountToSpend(amountToSend)
          .withInputBoxesLoader(new MockedBoxesLoader(Arrays.asList(input1))),
        ErgoId.create(mockTokenId),
        Parameters.OneErg
      )

      ctx.newProverBuilder().build().reduce(txCreate, 0)

      val babelFeeErgoBox = txCreate.getOutputs.get(0).convertToInputWith(mockTokenId, 0)

      // now we cancel the babel box
      val txCancel = BabelFeeOperations.cancelBabelFeeContract(
        BoxOperations.createForSender(creator, ctx)
          .withInputBoxesLoader(new MockedBoxesLoader(Arrays.asList(input1))),
        babelFeeErgoBox)

      ctx.newProverBuilder()
        .withMnemonic(mnemonic, SecretString.empty(), false)
        .build()
        .sign(txCancel)

      // check if the contract really is for the token
      new BabelFeeBoxContract(babelFeeErgoBox.getErgoTree).getTokenId.toString shouldBe mockTokenId
      // check template hash
      ErgoTreeTemplate.fromErgoTree(babelFeeErgoBox.getErgoTree).getTemplateHashHex shouldBe BabelFeeBoxContract.templateHash
    }
  }

  property("babel fee box use") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val sender = address

      val txB = ctx.newTxBuilder
      val babelFeeBoxState = BabelFeeBoxState.newBuilder().withValue(Parameters.OneErg)
        .withTokenId(ErgoId.create(mockTokenId))
        .withBoxCreator(Address.create(secondEip3AddrStr))
        .withPricePerToken(Parameters.MinFee)
        .build()

      val fee = Parameters.MinFee

      val output = txB.outBoxBuilder
        .value(Parameters.MinChangeValue)
        .contract(sender.toErgoContract)
        .tokens(new ErgoToken(
          ErgoId.create(mockTokenId),
          1000 - babelFeeBoxState.calcTokensToSellForErgAmount(fee)))
        .build()

      val input = txB.outBoxBuilder
        .value(Parameters.MinChangeValue)
        .contract(sender.toErgoContract)
        .tokens(new ErgoToken(ErgoId.create(mockTokenId), 1000))
        .build().convertToInputWith(mockTokenId, 0)

      txB.fee(fee)
        .outputs(output)
        .boxesToSpend(util.Arrays.asList(input))
        .sendChangeTo(sender)

      BabelFeeOperations.addBabelFeeBoxes(txB,
        babelFeeBoxState.buildOutbox(txB, null)
          .convertToInputWith(mockTokenId, 0),
        fee)

      val tx = txB.build()

      ctx.newProverBuilder()
        .withMnemonic(mnemonic, SecretString.empty(), false)
        .build()
        .sign(tx)
    }
  }

  property("fetch babel fee boxes") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val creator = address

      val tockenId = ErgoId.create(mockTokenId)

      // find no boxes
      val babelBox1 = BabelFeeOperations.findBabelFeeBox(ctx,
        new MockedBoxesLoader(new util.ArrayList[InputBox]()),
        tockenId, Parameters.MinFee, 1)

      babelBox1 shouldBe (null)

      val inputBabelBox = BabelFeeBoxState.newBuilder()
        .withValue(Parameters.OneErg)
        .withTokenId(tockenId)
        .withPricePerToken(Parameters.MinFee)
        .withBoxCreator(creator)
        .build().buildOutbox(ctx.newTxBuilder(), null)
        .convertToInputWith(mockTokenId, 0)

      val babelBox2 = BabelFeeOperations.findBabelFeeBox(ctx, new MockedBoxesLoader(util.Arrays.asList(inputBabelBox)),
        tockenId, Parameters.MinFee, 1)

      babelBox2 shouldBe inputBabelBox

      // the amount needed (2 ERG) is more than inputBabelBox can offer, so it is discarded
      val babelBox3 = BabelFeeOperations.findBabelFeeBox(ctx, new MockedBoxesLoader(util.Arrays.asList(inputBabelBox)),
        tockenId, Parameters.OneErg * 2, 1)

      babelBox3 shouldBe (null)
    }
  }
}
