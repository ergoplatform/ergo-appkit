package org.ergoplatform.appkit

import java.util
import java.util.{Arrays, Collections}
import org.ergoplatform.appkit.babelfee.{BabelFeeBoxState, BabelFeeOperations}
import org.ergoplatform.sdk.{ErgoId, ErgoToken, SecretString}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.collection.JavaConverters._

class TokenSelectionAndBabelFeeSpec
    extends AnyPropSpec
    with Matchers
    with ScalaCheckDrivenPropertyChecks
    with HttpClientTesting
    with AppkitTestingCommon {

  private val tokenIdHex = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"

  property("UnsignedTransactionBuilder sums duplicate tokensToBurn entries and rejects non-positive burns") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val sender = address
      val tokenId = ErgoId.create(tokenIdHex)

      val inputBox = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.OneErg)
        .contract(sender.toErgoContract)
        .tokens(new ErgoToken(tokenId, 100L))
        .build()
        .convertToInputWith(tokenIdHex, 0)

      val outputBox = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.MinChangeValue)
        .contract(sender.toErgoContract)
        .build()

      val unsignedTx = ctx.newTxBuilder
        .addInputs(inputBox)
        .addOutputs(outputBox)
        .fee(Parameters.MinFee)
        .tokensToBurn(new ErgoToken(tokenId, 30L), new ErgoToken(tokenId, 20L))
        .sendChangeTo(sender)
        .build()

      // Change box should retain 100 - (30 + 20) = 50 tokens
      val changeBox = unsignedTx.getOutputs.asScala.find(_.getTokens.asScala.nonEmpty).get
      changeBox.getTokens.get(0).getValue shouldBe 50L

      val prover = ctx.newProverBuilder()
        .withMnemonic(mnemonic, SecretString.empty(), false)
        .build()
      prover.sign(unsignedTx) shouldNot be(null)

      intercept[IllegalArgumentException] {
        ctx.newTxBuilder
          .addInputs(inputBox)
          .addOutputs(outputBox)
          .fee(Parameters.MinFee)
          .tokensToBurn(new ErgoToken(tokenId, 0L))
          .sendChangeTo(sender)
          .build()
      }
    }
  }

  property("SelectTokensHelper and InputBoxesValidator sum duplicate token IDs and reject non-positive targets") {
    val tokenId = ErgoId.create(tokenIdHex)
    val helper = new SelectTokensHelper(Arrays.asList(new ErgoToken(tokenId, 100L), new ErgoToken(tokenId, 50L)))
    helper.useTokens(Collections.singletonList(new ErgoToken(tokenId, 100L)))
    helper.areTokensCovered() shouldBe false
    helper.getRemainingTokenList.get(0).getValue shouldBe 50L

    helper.useTokens(Collections.singletonList(new ErgoToken(tokenId, 50L)))
    helper.areTokensCovered() shouldBe true

    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val sender = address
      val box1 = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.OneErg)
        .contract(sender.toErgoContract)
        .tokens(new ErgoToken(tokenId, 50L))
        .build()
        .convertToInputWith(tokenIdHex, 0)

      intercept[InputBoxesSelectionException.NotEnoughTokensException] {
        InputBoxesValidatorJavaHelper.validateBoxes(
          Collections.singletonList(box1),
          Parameters.MinFee,
          Arrays.asList(new ErgoToken(tokenId, 40L), new ErgoToken(tokenId, 30L))
        )
      }

      intercept[IllegalArgumentException] {
        InputBoxesValidatorJavaHelper.validateBoxes(
          Collections.singletonList(box1),
          Parameters.MinFee,
          Collections.singletonList(new ErgoToken(tokenId, 0L))
        )
      }

      intercept[IllegalArgumentException] {
        InputBoxesValidatorJavaHelper.validateBoxes(
          Collections.singletonList(box1),
          Parameters.MinFee,
          Collections.singletonList(new ErgoToken(tokenId, -5L))
        )
      }

      intercept[IllegalArgumentException] {
        InputBoxesValidatorJavaHelper.validateBoxes(
          Collections.singletonList(box1),
          Parameters.MinFee,
          Arrays.asList(new ErgoToken(tokenId, 50L), new ErgoToken(tokenId, -20L))
        )
      }

      intercept[IllegalArgumentException] {
        new SelectTokensHelper(Arrays.asList(new ErgoToken(tokenId, 50L), new ErgoToken(tokenId, -20L)))
      }
    }
  }

  property("BabelFeeOperations.findBabelFeeBox skips boxes whose quantized token swap exceeds maxAmountToBuy") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val tokenId = ErgoId.create(tokenIdHex)
      val creator = Address.create(secondEip3AddrStr)

      // Higher pricePerToken (10_000_000) but valueAvailableToBuy = 1_000_000 => maxTokenAmountToBuy = 0 (unusable)
      val unusableHighPriceBox = BabelFeeBoxState.newBuilder()
        .withValue(Parameters.MinChangeValue + Parameters.MinFee)
        .withTokenId(tokenId)
        .withBoxCreator(creator)
        .withPricePerToken(10L * Parameters.MinFee)
        .build()
        .buildOutbox(ctx.newTxBuilder, null)
        .convertToInputWith(tokenIdHex, 0)

      // Lower pricePerToken (1_000_000) with valueAvailableToBuy = 2_000_000 => maxTokenAmountToBuy = 2 (usable)
      val usableBox = BabelFeeBoxState.newBuilder()
        .withValue(Parameters.MinChangeValue + 2L * Parameters.MinFee)
        .withTokenId(tokenId)
        .withBoxCreator(creator)
        .withPricePerToken(Parameters.MinFee)
        .build()
        .buildOutbox(ctx.newTxBuilder, null)
        .convertToInputWith(tokenIdHex, 1)

      val loader = new MockedBoxesLoader(Arrays.asList(unusableHighPriceBox, usableBox))
      val chosen = BabelFeeOperations.findBabelFeeBox(ctx, loader, tokenId, Parameters.MinFee, 1)
      chosen shouldNot be(null)
      chosen.getId shouldBe usableBox.getId
    }
  }

  property("BoxOperations.loadTop budgets enough ERG for multiple change boxes when >100 leftover tokens exist") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val sender = address
      val tokens = (1 to 101).map { i =>
        val hex = String.format("%064x", Int.box(i))
        new ErgoToken(ErgoId.create(hex), 1L)
      }
      val boxWithManyTokens = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.OneErg + Parameters.MinFee)
        .contract(sender.toErgoContract)
        .tokens(tokens: _*)
        .build()
        .convertToInputWith(tokenIdHex, 0)

      val extraErg1 = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.MinFee)
        .contract(sender.toErgoContract)
        .build()
        .convertToInputWith(tokenIdHex, 1)

      val extraErg2 = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.MinFee)
        .contract(sender.toErgoContract)
        .build()
        .convertToInputWith(tokenIdHex, 2)

      val loader = new MockedBoxesLoader(Arrays.asList(boxWithManyTokens, extraErg1, extraErg2))
      val selected: util.List[InputBox] = BoxOperations
        .createForSender(sender, ctx)
        .withAmountToSpend(Parameters.OneErg)
        .withFeeAmount(Parameters.MinFee)
        .withInputBoxesLoader(loader)
        .loadTop()

      selected.size() shouldBe 3
    }
  }

  property("BoxOperations.loadTop tracks leftover tokens across multiple senders when budgeting change boxes") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val sender1 = address
      val sender2 = Address.create(secondEip3AddrStr)
      val tokens1 = (1 to 60).map { i =>
        new ErgoToken(ErgoId.create(String.format("%064x", Int.box(i))), 1L)
      }
      val tokens2 = (61 to 120).map { i =>
        new ErgoToken(ErgoId.create(String.format("%064x", Int.box(i))), 1L)
      }

      val sender1Box = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.OneErg / 2)
        .contract(sender1.toErgoContract)
        .tokens(tokens1: _*)
        .build()
        .convertToInputWith(tokenIdHex, 0)

      val sender2Box1 = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.OneErg / 2 + 2L * Parameters.MinFee)
        .contract(sender2.toErgoContract)
        .tokens(tokens2: _*)
        .build()
        .convertToInputWith(tokenIdHex, 1)

      val sender2Box2 = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.MinFee)
        .contract(sender2.toErgoContract)
        .build()
        .convertToInputWith(tokenIdHex, 2)

      val loader = new BoxOperations.IUnspentBoxesLoader {
        override def prepare(
          ctx: BlockchainContext,
          addresses: util.List[Address],
          grossAmount: Long,
          tokensToSpend: util.List[ErgoToken]
        ): Unit = ()
        override def prepareForAddress(address: Address): Unit = ()
        override def loadBoxesPage(ctx: BlockchainContext, sender: Address, page: Integer): util.List[InputBox] =
          if (page > 0) Collections.emptyList()
          else if (sender == sender1) Collections.singletonList(sender1Box)
          else Arrays.asList(sender2Box1, sender2Box2)
      }

      val selected = BoxOperations
        .createForSenders(Arrays.asList(sender1, sender2), ctx)
        .withAmountToSpend(Parameters.OneErg)
        .withFeeAmount(Parameters.MinFee)
        .withInputBoxesLoader(loader)
        .loadTop()

      selected.size() shouldBe 3
    }
  }
}
