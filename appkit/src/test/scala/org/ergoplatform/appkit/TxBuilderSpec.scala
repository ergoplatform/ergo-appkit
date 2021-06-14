package org.ergoplatform.appkit

import java.io.File
import java.math.BigInteger
import java.util.Arrays

import org.ergoplatform.ErgoScriptPredef
import org.ergoplatform.appkit.testing.AppkitTesting
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}
import sigmastate.eval.CBigInt
import sigmastate.helpers.NegativeTesting

class TxBuilderSpec extends PropSpec with Matchers
  with ScalaCheckDrivenPropertyChecks
  with AppkitTesting
  with HttpClientTesting
  with NegativeTesting {

  val mockTxId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"

  def createTestInput(ctx: BlockchainContext): InputBox = {
    val out = ctx.newTxBuilder.outBoxBuilder
      .value(30000000)
      .contract(ctx.compileContract(
        ConstantsBuilder.empty(),
        """{
         |  val v1 = getVar[Int](1).get
         |  val v10 = getVar[BigInt](10).get
         |  sigmaProp(v1.toBigInt == v10)
         |}""".stripMargin))
      .build()

    out.getCreationHeight shouldBe ctx.getHeight
    out.convertToInputWith(mockTxId, 0)
  }

  property("ContextVar id should be in range") {
    for (id <- 0 to Byte.MaxValue) {
      ContextVar.of(id.toByte, 10)
    }

    def checkFailed(invalidId: Int) = {
      assertExceptionThrown(
        ContextVar.of(invalidId.toByte, 10),
        exceptionLike[IllegalArgumentException]("Context variable id should be in range"),
        clue = s"id: $invalidId"
      )
    }

    for (id <- Byte.MinValue to -1) {
      checkFailed(id)
    }
  }

  property("InputBox support context variables") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val contextVars = Seq(
        ContextVar.of(1.toByte, 100),
        ContextVar.of(10.toByte, CBigInt(BigInteger.valueOf(100)))
      )
      val input = createTestInput(ctx)
        .withContextVars(contextVars:_*)
      val txB = ctx.newTxBuilder()
      val output = txB.outBoxBuilder()
        .value(15000000)
        .creationHeight(ctx.getHeight + 1) // just an example of using height
        .contract(truePropContract(ctx))
        .build()
      output.getCreationHeight shouldBe ctx.getHeight + 1

      val changeAddr = Address.fromErgoTree(input.getErgoTree, NetworkType.MAINNET).getErgoAddress
      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(changeAddr)
        .build()
      // alice signing bob's box. Does not work here but works in other cases.
      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)
      signed.getCost shouldBe 14685

      // check the signed transaction contains all the context variables
      // we attached to the input box
      val extensions = signed.getSignedInputs.get(0).getContextVars
      contextVars.foreach { cv =>
        extensions.containsKey(cv.getId) shouldBe true
        extensions.get(cv.getId) shouldBe cv.getValue
      }
    }
  }

  property("Transaction builder can bypass creating feeOut box") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()
      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)
      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(truePropContract(ctx)).build()
      val feeOut = txB.outBoxBuilder()
        .value(1000000)
        .contract(ctx.newContract(ErgoScriptPredef.feeProposition()))
        .build()

      val changeAddr = Address.fromErgoTree(input.getErgoTree, NetworkType.MAINNET).getErgoAddress
      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output, feeOut)
        .sendChangeTo(changeAddr)
        .build()
      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed.getOutputsToSpend.size() shouldBe 2
    }
  }
  
  property("non-standard fee contract") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()
      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)
      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(truePropContract(ctx)).build()
      val feeOut = txB.outBoxBuilder()
        .value(1000000)
        .contract(truePropContract(ctx))
        .build()

      val changeAddr = Address.fromErgoTree(input.getErgoTree, NetworkType.MAINNET).getErgoAddress

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output, feeOut)
        .sendChangeTo(changeAddr)
        .build()
      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed.getOutputsToSpend.size() shouldBe 2
    }
  }

  property("ErgoProverBuilder.withEip3Secret require mnemonic") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    assertExceptionThrown(
      ergoClient.execute { ctx: BlockchainContext =>
        ctx.newProverBuilder()
          .withEip3Secret(0)
          .build()
      },
      exceptionLike[IllegalArgumentException](
        "Mnemonic is not specified, use withMnemonic method.")
    )
  }

  property("ErgoProverBuilder.withEip3Secret check uniqueness of derivation index") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    assertExceptionThrown(
      ergoClient.execute { ctx: BlockchainContext =>
        ctx.newProverBuilder()
          .withMnemonic(mnemonic, SecretString.empty())
          .withEip3Secret(0)
          .withEip3Secret(0) // attempt to add the same index
          .build()
      },
      exceptionLike[IllegalArgumentException](
        "Secret key for derivation index 0 has already been added.")
    )
  }

  private def testEip3Address(ctx: BlockchainContext, index: Int): Address = {
    Address.createEip3Address(index, ctx.getNetworkType,
      mnemonic, SecretString.empty())
  }

  property("ErgoProverBuilder.withEip3Secret should pass secrets to the prover") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val prover = ctx.newProverBuilder()
        .withMnemonic(mnemonic, SecretString.empty())
        .withEip3Secret(0)
        .withEip3Secret(1)
        .build()
      assert(prover.getEip3Addresses.size() == 2)
      assert(prover.getEip3Addresses.get(0) == testEip3Address(ctx, 0))
      assert(prover.getEip3Addresses.get(1) == testEip3Address(ctx, 1))
    }
  }

  property("send to recipient (non EIP-3)") {
    val data = MockData(
      Seq(
        loadNodeResponse("response_Box1.json"),
        loadNodeResponse("response_Box2.json"),
        loadNodeResponse("response_Box3.json"),
        "21f84cf457802e66fb5930fb5d45fbe955933dc16a72089bf8980797f24e2fa1"),
      Seq(
        loadExplorerResponse("response_boxesByAddressUnspent.json")))

    val ergoClient = createMockedErgoClient(data)

    ergoClient.execute { ctx: BlockchainContext =>
      val senderProver = BoxOperations.createProver(ctx,
          new File("storage/E2.json").getPath, "abc")
        .withEip3Secret(0)
        .withEip3Secret(1)
        .build

      val recipient = senderProver.getEip3Addresses.get(1)
      val amountToSend = 1000000
      val pkContract = ErgoContracts.sendToPK(ctx, recipient)
      val signed = BoxOperations.putToContractTx(ctx,
          senderProver, false, pkContract, amountToSend)
      assert(signed != null)
    }
  }

}
