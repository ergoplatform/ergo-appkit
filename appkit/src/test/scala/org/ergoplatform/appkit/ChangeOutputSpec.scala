package org.ergoplatform.appkit

import org.scalatest.{PropSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import sigmastate.eval._
import sigmastate.interpreter.CryptoConstants
import special.sigma.GroupElement
import JavaHelpers._
import java.util.{Arrays, List => JList}

import org.ergoplatform.appkit.Parameters.MinFee
import org.ergoplatform.appkit.testing.AppkitTesting

class ChangeOutputSpec extends PropSpec with Matchers
  with ScalaCheckDrivenPropertyChecks
  with AppkitTesting
  with HttpClientTesting {

  property("YesChangeOutput") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    val g: GroupElement = CryptoConstants.dlogGroup.generator
    val x = BigInt("187235612876647164378132684712638457631278").bigInteger
    val y = BigInt("340956873409567839086738967389673896738906").bigInteger
    val gX:GroupElement = g.exp(x)
    val gY:GroupElement = g.exp(y)
    val gXY:GroupElement = gX.exp(y)

    ergoClient.execute { ctx: BlockchainContext =>
      val input = ctx.newTxBuilder.outBoxBuilder.registers(
        ErgoValue.of(gY), ErgoValue.of(gXY)
      ).value(30000000).contract(ctx.compileContract(
        ConstantsBuilder.empty(),
        """{
          |  val gY = SELF.R4[GroupElement].get
          |  val gXY = SELF.R5[GroupElement].get
          |  proveDHTuple(gY, gY, gXY, gXY)
          |}""".stripMargin
      )).build().convertToInputWith(
        "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809",
        0)

      val txB = ctx.newTxBuilder()
      val output = txB.outBoxBuilder()
        .value(15000000)
        .contract(truePropContract(ctx)).build()
      val inputs = Arrays.asList(input)

      // below is ergoTree of a random box picked from the block explorer.
      // The boxId is 02abc29b6a28ccf7e9620afa16e1067caeb75fcd2e62c066e190742962cdcbae
      // We just need valid ergoTree to construct the change address
      val tree = "100207036ba5cfbc03ea2471fdf02737f64dbcd58c34461a7ec1e586dcd713dacbf89a120400d805d601db6a01ddd6027300d603b2a5730100d604e4c672030407d605e4c672030507eb02ce7201720272047205ce7201720472027205"
      val ergoTree = JavaHelpers.decodeStringToErgoTree(tree)
      val changeAddr = Address.fromErgoTree(ergoTree, NetworkType.MAINNET).getErgoAddress
      val unsigned = txB.boxesToSpend(inputs)
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(changeAddr).build()
      // alice signing bob's box. Does not work here but works in other cases.
      val signed = ctx.newProverBuilder().withDHTData(gY, gY, gXY, gXY, x).build().sign(unsigned)
      val outputs = signed.getOutputsToSpend
      assert(outputs.size == 3)
      println(signed.toJson(false))
    }
  }

  property("NoChangeOutput") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    val g: GroupElement = CryptoConstants.dlogGroup.generator
    val x = BigInt("187235612876647164378132684712638457631278").bigInteger
    val y = BigInt("340956873409567839086738967389673896738906").bigInteger
    val gX:GroupElement = g.exp(x)
    val gY:GroupElement = g.exp(y)
    val gXY:GroupElement = gX.exp(y)

    ergoClient.execute { ctx: BlockchainContext =>
      val input = ctx.newTxBuilder.outBoxBuilder.registers(
        ErgoValue.of(gY), ErgoValue.of(gXY)
      ).value(30000000).contract(ctx.compileContract(
        ConstantsBuilder.empty(),
        """{
          |  val gY = SELF.R4[GroupElement].get
          |  val gXY = SELF.R5[GroupElement].get
          |  proveDHTuple(gY, gY, gXY, gXY)
          |}""".stripMargin
      )).build().convertToInputWith("f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809", 0)

      val txB = ctx.newTxBuilder()
      val output = txB.outBoxBuilder()
        .value(15000000)
        .contract(truePropContract(ctx))
        .build()
      val inputs = new java.util.ArrayList[InputBox]()
      inputs.add(input)

      // below is ergoTree of a random box picked from the block explorer.
      // The boxId is 02abc29b6a28ccf7e9620afa16e1067caeb75fcd2e62c066e190742962cdcbae
      // We just need valid ergoTree to construct the change address
      val tree = "100207036ba5cfbc03ea2471fdf02737f64dbcd58c34461a7ec1e586dcd713dacbf89a120400d805d601db6a01ddd6027300d603b2a5730100d604e4c672030407d605e4c672030507eb02ce7201720272047205ce7201720472027205"
      val ergoTree = JavaHelpers.decodeStringToErgoTree(tree)
      val changeAddr = Address.fromErgoTree(ergoTree, NetworkType.MAINNET).getErgoAddress
      val unsigned = txB.boxesToSpend(inputs)
        .outputs(output)
        .fee(15000000)
        .sendChangeTo(changeAddr).build()
      val signed = ctx.newProverBuilder()
        .withDHTData(gY, gY, gXY, gXY, x)
        .build()
        .sign(unsigned) // alice signing bob's box. Does not work here but works in other cases.
      val outputs = signed.getOutputsToSpend
      assert(outputs.size == 2)
      println(signed.toJson(false))
    }
  }

  property("NoTokenChangeOutput + token burning") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    val g: GroupElement = CryptoConstants.dlogGroup.generator
    val x = BigInt("187235612876647164378132684712638457631278").bigInteger
    val y = BigInt("340956873409567839086738967389673896738906").bigInteger
    val gX:GroupElement = g.exp(x)
    val gY:GroupElement = g.exp(y)
    val gXY:GroupElement = gX.exp(y)

    ergoClient.execute { ctx: BlockchainContext =>

      val input0 = ctx.newTxBuilder.outBoxBuilder.registers(
        ErgoValue.of(gY), ErgoValue.of(gXY)
      ).value(30000000).contract(ctx.compileContract(
        ConstantsBuilder.empty(),
        """{
          |  val gY = SELF.R4[GroupElement].get
          |  val gXY = SELF.R5[GroupElement].get
          |  proveDHTuple(gY, gY, gXY, gXY)
          |}""".stripMargin
      )).build().convertToInputWith(
        "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809", 0)

      val tokenId = input0.getId.toString
      val tokenAmount = 5000000000L
      val tokenAmountToBurn = 2000000000L

      val ph = ctx.createPreHeader()
        .height(ctx.getHeight + 1)
        .build()

      val txB = ctx.newTxBuilder().preHeader(ph) // for issuing token
      val tokenBox = txB.outBoxBuilder
        .value(15000000) // value of token box, doesn't really matter
        .tokens(new ErgoToken(tokenId, tokenAmount)) // amount of token issuing
        .contract(ctx.compileContract(
          // contract of the box containing tokens, just has to be spendable
          ConstantsBuilder.empty(), "{sigmaProp(1 < 2)}"
        ))
        .build()

      val inputs = new java.util.ArrayList[InputBox]()
      inputs.add(input0)

      // below is ergoTree of a random box picked from the block explorer.
      // The boxId is 02abc29b6a28ccf7e9620afa16e1067caeb75fcd2e62c066e190742962cdcbae
      // We just need valid ergoTree to construct the change address
      val tree = "100207036ba5cfbc03ea2471fdf02737f64dbcd58c34461a7ec1e586dcd713dacbf89a120400d805d601db6a01ddd6027300d603b2a5730100d604e4c672030407d605e4c672030507eb02ce7201720272047205ce7201720472027205"
      val ergoTree = JavaHelpers.decodeStringToErgoTree(tree)
      val changeAddr = Address.fromErgoTree(ergoTree, NetworkType.MAINNET).getErgoAddress
      val unsigned = txB.boxesToSpend(inputs)
        .outputs(tokenBox)
        .fee(15000000)
        .sendChangeTo(changeAddr).build()
      val signed = ctx.newProverBuilder()
        .withDHTData(gY, gY, gXY, gXY, x)
        .build()
        .sign(unsigned) // alice signing bob's box. Does not work here but works in other cases.
      val outputs = signed.getOutputsToSpend
      assert(outputs.size == 2)
      println(signed.toJson(false))
      val boxWithToken = outputs.get(0)
      assert(boxWithToken.getTokens.size() == 1)

      // move Ergs from boxWithToken and burn tokens in the transaction
      {
        val expectedChange = MinFee
        val txB = ctx.newTxBuilder()
        val ergAmountToSend = boxWithToken.getValue - MinFee - expectedChange
        val out = txB.outBoxBuilder
          .value(ergAmountToSend)
          .contract(ctx.compileContract(
            // contract of the box containing tokens, just has to be spendable
            ConstantsBuilder.empty(), "{sigmaProp(1 < 2)}"
          ))
          .build()
        val unsigned = txB
          .boxesToSpend(IndexedSeq(boxWithToken).convertTo[JList[InputBox]])
          .outputs(out)
          .tokensToBurn(new ErgoToken(tokenId, tokenAmountToBurn))
          .fee(MinFee)
          .sendChangeTo(changeAddr)
          .build()
        val signed = ctx.newProverBuilder().build().sign(unsigned)
        val outputs = signed.getOutputsToSpend
        assert(outputs.size == 3)
        val out0 = outputs.get(0)
        val fee = outputs.get(1)
        val change = outputs.get(2)
        out0.getValue shouldBe ergAmountToSend
        out0.getTokens.size() shouldBe 0

        fee.getValue shouldBe MinFee
        fee.getTokens.size shouldBe 0

        change.getValue shouldBe expectedChange
        change.getTokens.size() shouldBe 1
        change.getTokens.get(0).getValue shouldBe (tokenAmount - tokenAmountToBurn)
      }
    }
  }
}

