package org.ergoplatform.appkit

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.ergoplatform.appkit.InputBoxesSelectionException.{InputBoxLimitExceededException, NotEnoughCoinsForChangeException, NotEnoughErgsException}
import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.impl.{Eip4TokenBuilder, ErgoTreeContract}
import org.ergoplatform.appkit.testing.AppkitTesting
import org.ergoplatform.explorer.client.model.{Items, TokenInfo}
import org.ergoplatform.{ErgoBox, ErgoScriptPredef}
import org.scalacheck.Gen
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scalan.util.FileUtil
import scorex.util.ModifierId
import sigmastate.eval.CBigInt
import sigmastate.helpers.NegativeTesting
import sigmastate.interpreter.HintsBag

import java.io.File
import java.math.BigInteger
import java.util
import java.util.Arrays
import java.util.function.Consumer

class TxBuilderSpec extends PropSpec with Matchers
  with ScalaCheckDrivenPropertyChecks
  with AppkitTesting
  with HttpClientTesting
  with NegativeTesting {

  val mockTxId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"

  def createTestInput(ctx: BlockchainContext, amount: Long, script: String): InputBox = {
    val out = ctx.newTxBuilder.outBoxBuilder
      .value(amount)
      .contract(ctx.compileContract(ConstantsBuilder.empty(), script))
      .build()

    out.getCreationHeight shouldBe ctx.getHeight
    out.convertToInputWith(mockTxId, 0)
  }

  def createTestInput(ctx: BlockchainContext): InputBox = {
    createTestInput(ctx, 30000000,
      """{
       |  val v1 = getVar[Int](1).get
       |  val v10 = getVar[BigInt](10).get
       |  sigmaProp(v1.toBigInt == v10)
       |}""".stripMargin)
  }

  def loadStorageE2(): (SecretStorage, util.List[Address]) = {
    val storage = SecretStorage.loadFrom("storage/E2.json")
    storage.unlock("abc")
    val senders = Arrays.asList(storage.getAddressFor(NetworkType.MAINNET))
    (storage, senders)
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

 property("Sign and Verify a message round trip") {
  forAll(Gen.alphaNumStr){ msg =>
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val proverA = BoxOperations.createProver(ctx,
           new File("storage/E2.json").getPath, "abc")
           .build

      val proverB = BoxOperations.createProver(ctx,
          new File("storage/E1.json").getPath, "abc")
         .build

      val proverASigmaPropFromAddress = SigmaProp.createFromAddress(proverA.getAddress)
      val signedMessage = proverA.signMessage(proverASigmaPropFromAddress,
        msg.getBytes, HintsBag.empty)

      Signature.verifySignature(proverASigmaPropFromAddress,
        msg.getBytes, signedMessage) shouldBe true

      Signature.verifySignature(proverASigmaPropFromAddress,
        msg.getBytes, signedMessage) shouldBe true

      Signature.verifySignature(SigmaProp.createFromAddress(proverB.getAddress),
        msg.getBytes, signedMessage) shouldBe false
      }
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
      val unsigned = txB.addInputs(input)
        .outputs(output).addOutputs(feeOut)
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

      unsigned.getOutputs.size() shouldBe 2
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
          .withMnemonic(mnemonic, SecretString.empty(), false)
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
      mnemonic, SecretString.empty(), false)
  }

  property("ErgoProverBuilder.withEip3Secret should pass secrets to the prover") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val prover = ctx.newProverBuilder()
        .withMnemonic(mnemonic, SecretString.empty(), false)
        .withEip3Secret(0)
        .withEip3Secret(1)
        .build()
      assert(prover.getEip3Addresses.size() == 2)
      assert(prover.getEip3Addresses.get(0) == testEip3Address(ctx, 0))
      assert(prover.getEip3Addresses.get(1) == testEip3Address(ctx, 1))
    }
  }

  val data = MockData(
    Seq(
      loadNodeResponse("response_Box1.json"),
      loadNodeResponse("response_Box2.json"),
      loadNodeResponse("response_Box3.json"),
      "21f84cf457802e66fb5930fb5d45fbe955933dc16a72089bf8980797f24e2fa1"),
    Seq(
      loadExplorerResponse("response_boxesByAddressUnspent.json")))

  property("send to recipient (non EIP-3)") {
    val ergoClient = createMockedErgoClient(data)

    ergoClient.execute { ctx: BlockchainContext =>
      val senderProver = BoxOperations.createProver(ctx,
          new File("storage/E2.json").getPath, "abc")
        .withEip3Secret(0)
        .withEip3Secret(1)
        .build

      val recipient = senderProver.getEip3Addresses.get(1)
      val amountToSend = 1000000
      val signed = BoxOperations.createForProver(senderProver, ctx).withAmountToSpend(amountToSend)
        .send(recipient)
      assert(signed != null)
    }
  }

  property("reduce transaction") {
    val ergoClient = createMockedErgoClient(data)

    val reducedTx: ReducedTransaction = ergoClient.execute { ctx: BlockchainContext =>
      val (_, senders) = loadStorageE2()
      val recipient = address
      val pkContract = recipient.toErgoContract

      val amountToSend = 1000000

      val unsigned = BoxOperations.createForSenders(senders, ctx)
        .withAmountToSpend(amountToSend)
        .withMessage("Test message")
        .putToContractTxUnsigned(pkContract)
      unsigned.getOutputs.get(0).getRegisters.size() shouldBe 6
      unsigned.getOutputs.get(0).getAttachment.getType shouldBe BoxAttachment.Type.PLAIN_TEXT

      val prover = ctx.newProverBuilder.build // prover without secrets
      val reduced = prover.reduce(unsigned, 0)
      reduced should not be(null)
      reduced.getInputBoxesIds.size() shouldBe unsigned.getInputs.size()
      reduced.getInputBoxesIds shouldBe unsigned.getInputBoxesIds
      reduced.getOutputs.size() shouldBe unsigned.getOutputs.size()
      reduced
    }

    val reducedTxBytes = reducedTx.toBytes

    // the only necessary parameter can either be hard-coded or passed
    // together with ReducedTransaction
    val maxBlockCost = Parameters.ColdClientMaxBlockCost
    val coldClient = new ColdErgoClient(NetworkType.MAINNET, maxBlockCost, Parameters.ColdClientBlockVersion)

    coldClient.execute { ctx: BlockchainContext =>
      // test that context is cold
      assertExceptionThrown(ctx.getHeight, exceptionLike[NotImplementedError]())
      assertExceptionThrown(
        ctx.getBoxesById("d47f958b201dc7162f641f7eb055e9fa7a9cb65cc24d4447a10f86675fc58328"),
        exceptionLike[NotImplementedError]())

      // create prover with secrets in the cold context
      val prover = BoxOperations.createProver(ctx,
        new File("storage/E2.json").getPath, "abc")
        .build

      // sign with the cold prover
      val deserializedTx = ctx.parseReducedTransaction(reducedTxBytes)
      deserializedTx shouldBe reducedTx

      val signed = prover.signReduced(deserializedTx, 0)

      signed should not be(null)
      val deserializedSignedTx = ctx.parseSignedTransaction(signed.toBytes)
      deserializedSignedTx shouldBe signed
    }
  }

  property("omit mempool boxes") {
    val ergoClient = createMockedErgoClient(MockData(
      Seq(
        loadNodeResponse("response_mempool.json"),
        loadNodeResponse("response_Box1.json"),
        loadNodeResponse("response_Box2.json"),
        loadNodeResponse("response_Box3.json")),
      Seq(
        loadExplorerResponse("response_boxesByAddressUnspent.json"),
        // fake an empty last page
        "{ \"items\": [ ], \"total\": 0\n}",
        // fake an empty mempool tx list
        "{ \"items\": [ ], \"total\": 0\n}")))

    a[NotEnoughErgsException] shouldBe thrownBy {
      ergoClient.execute { ctx: BlockchainContext =>
        val (_, senders) = loadStorageE2()
        val recipientContract = address.toErgoContract

        val amountToSend = 1000000
        val unsigned = BoxOperations.createForSenders(senders, ctx)
          .withAmountToSpend(amountToSend)
          .withInputBoxesLoader(new ExplorerAndPoolUnspentBoxesLoader())
          .putToContractTxUnsigned(recipientContract)

        val prover = ctx.newProverBuilder.build // prover without secrets
        val reduced = prover.reduce(unsigned, 0)
        reduced should not be (null)
        reduced
      }
    }

  }

  property("consider changebox amount") {
    val ergoClient = createMockedErgoClient(data)

    ergoClient.execute { ctx: BlockchainContext =>
      val (_, senders) = loadStorageE2()
      val recipient = address
      val pkContract = recipient.toErgoContract

      // send 1 ERG
      val amountToSend = 1000L * 1000 * 1000

      // first box: 1 ERG + tx fee + token that will cause a change
      val input1 = ctx.newTxBuilder.outBoxBuilder
        .value(amountToSend + Parameters.MinFee)
        .contract(pkContract)
        .tokens(new ErgoToken(mockTxId, 2))
        .build().convertToInputWith(mockTxId, 0)
      // second box: enough ERG for the change box
      val input2 = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.MinFee)
        .contract(pkContract)
        .build().convertToInputWith(mockTxId, 1)

      val operations = BoxOperations.createForSenders(senders, ctx)
        .withAmountToSpend(amountToSend)
        .withTokensToSpend(Arrays.asList(new ErgoToken(mockTxId, 1)))
        .withInputBoxesLoader(new MockedBoxesLoader(Arrays.asList(input1, input2)))
      val inputsSelected = operations.loadTop()

      // both boxes should be selected
      inputsSelected.size() shouldBe 2

      // if we restrict to a single box, we face InputBoxLimitExceededException
      assertExceptionThrown(
        operations.withMaxInputBoxesToSelect(1).loadTop(),
        exceptionLike[InputBoxLimitExceededException]("could not cover 1000000 nanoERG")
      )

      // if there is only a single input box, we face NotEnoughCoinsForChangeException
      val operations2 = BoxOperations.createForSenders(senders, ctx)
        .withAmountToSpend(amountToSend)
        .withInputBoxesLoader(new MockedBoxesLoader(util.Arrays.asList(input1)))

      assertExceptionThrown(
        operations2.loadTop(),
        exceptionLike[NotEnoughCoinsForChangeException]()
      )
    }

  }

  property("use changebox to consolidate") {
    // this demonstrates that unnecessary input boxes can be picked up to consolidate wallet boxes
    // via change box on the fly (issue #182)

    val ergoClient = createMockedErgoClient(data)

    ergoClient.execute { ctx: BlockchainContext =>
      val (_, senders) = loadStorageE2()
      val recipient = address
      val pkContract = recipient.toErgoContract

      // send 0.5 ERG
      val amountToSend = 500L * 1000 * 1000

      // first box: 1 ERG
      val input1 = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.OneErg)
        .contract(pkContract)
        .build().convertToInputWith(mockTxId, 0)
      // second box: 1 ERG
      val input2 = ctx.newTxBuilder.outBoxBuilder
        .value(Parameters.OneErg)
        .contract(pkContract)
        .build().convertToInputWith(mockTxId, 1)

      val tx = ctx.newTxBuilder().addInputs(input1).addInputs(input2)
        .outputs(ctx.newTxBuilder().outBoxBuilder().contract(pkContract).value(amountToSend).build())
        .sendChangeTo(recipient.getErgoAddress)
        .fee(Parameters.MinFee)
        .build()

      // both boxes should be selected
      // ergo-wallet's DefaultBoxSelector discarded the second input because it is not necessary for
      // the outputs, so this test checks if all inputs are used (size 2)
      tx.getInputs.size() shouldBe 2
      tx.getOutputs.size() shouldBe 3

    }

  }

  property("Test changebox token amount max 100") {
    val ergoClient = createMockedErgoClient(data)

    val tokenList: Items[TokenInfo] = new Gson().fromJson(FileUtil.read(FileUtil.file(s"appkit/src/test/resources/tokens.json")), new TypeToken[Items[TokenInfo]]() {}.getType)

    ergoClient.execute { ctx: BlockchainContext =>
      val (storage, _) = loadStorageE2()

      val recipient = address

      // send 1 ERG
      val amountToSend = 1000L * 1000 * 1000
      val pkContract = recipient.toErgoContract

      val senders = Arrays.asList(storage.getAddressFor(NetworkType.MAINNET))

      val ergoTokens = tokenList.getItems
        .convertTo[IndexedSeq[TokenInfo]]
        .map { ti => new ErgoToken(ti.getId, ti.getEmissionAmount) }

      val tokenList1 = ergoTokens.take(150)
      val tokenList2 = ergoTokens.takeRight(110)
      // first box: 1 ERG + tx fee + token that will cause a change
      val input1 = ctx.newTxBuilder.outBoxBuilder
        .value(amountToSend + Parameters.MinFee)
        .contract(pkContract)
        .tokens(tokenList1:_*)
        .build().convertToInputWith(mockTxId, 0)
      // second box: enough ERG for the change box
      val input2 = ctx.newTxBuilder.outBoxBuilder
        .value(amountToSend + Parameters.MinFee)
        .tokens(tokenList2:_*)
        .contract(pkContract)
        .build().convertToInputWith(mockTxId, 1)

      val operations = BoxOperations.createForSenders(senders, ctx)
        .withAmountToSpend(amountToSend)
        .withInputBoxesLoader(new MockedBoxesLoader(Arrays.asList(input1, input2)))
      val unsigned = operations.putToContractTxUnsigned(pkContract)

      // all outputs should have 100 tokens at max, and it should contain all input tokens
      unsigned.getOutputs.forEach { output: OutBox =>
        output.getTokens.size() <= 100 shouldBe true

        output.getTokens.forEach(new Consumer[ErgoToken] {
          override def accept(outToken: ErgoToken): Unit = {
            // we know that ergoTokens list does not contain multiple entries for a single token, so
            // we can use this simplified check here
            ergoTokens.count(_ == outToken) shouldBe 1
          }
        })
      }
      val outTokenNum = unsigned.getOutputs
        .map(_.getTokens.size())
        .convertTo[IndexedSeq[Int]].sum
      (tokenList1.length + tokenList2.length) shouldBe outTokenNum
    }

  }

  property("Test same token multiple times") {
    val ergoClient = createMockedErgoClient(data)

    ergoClient.execute { ctx: BlockchainContext =>
      val (storage, _) = loadStorageE2()

      val recipient = address

      // send 1 ERG
      val amountToSend = 1000L * 1000 * 1000
      val pkContract = recipient.toErgoContract

      val senders = Arrays.asList(storage.getAddressFor(NetworkType.MAINNET))

      val input1 = ctx.newTxBuilder.outBoxBuilder
        .value(amountToSend + Parameters.MinFee + Parameters.MinChangeValue)
        .contract(pkContract)
        // the same token twice
        .tokens(new ErgoToken(mockTxId, 1), new ErgoToken(mockTxId, 1))
        .build().convertToInputWith(mockTxId, 0)

      val unsigned = BoxOperations.createForSenders(senders, ctx)
        .withAmountToSpend(amountToSend)
        .withInputBoxesLoader(new MockedBoxesLoader(Arrays.asList(input1)))
        .putToContractTxUnsigned(pkContract)

      // this fails due to token burning check - instead, tokens should be in change box FIXME
      val prover = ctx.newProverBuilder.build // prover without secrets
      val reduced = prover.reduce(unsigned, 0)

      // this fails with NotEnoughTokensException, although there are enough tokens available
      val spendAllTokens = BoxOperations.createForSenders(senders, ctx)
        .withAmountToSpend(amountToSend)
        .withTokensToSpend(Arrays.asList(new ErgoToken(mockTxId, 2)))
        .withInputBoxesLoader(new MockedBoxesLoader(Arrays.asList(input1)))
        .putToContractTxUnsigned(pkContract)

    }

  }

  property("Special tx building cases") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val ergsInBox = 10
      val ebox = new ErgoBox(
        value = ergsInBox,
        ergoTree = truePropContract(ctx).getErgoTree,
        transactionId = ModifierId @@ mockTxId,
        index = 0,
        creationHeight = 100000
      )
      val contextVars = Seq(
        ContextVar.of(1.toByte, ebox)  // the Box as a context variable
      )
      val input = createTestInput(ctx, amount = 30000000,
        script = // check that the variable and the register is accessible
          s"""
           |sigmaProp(
           |  getVar[Box](1).get.value == $ergsInBox &&
           |  OUTPUTS(0).R4[Box].get.id == getVar[Box](1).get.id)
           |""".stripMargin)
        .withContextVars(contextVars:_*)
      val txB = ctx.newTxBuilder()
      val output = txB.outBoxBuilder()
        .value(15000000)
        .registers(ErgoValue.of(ebox)) // the Box as a register value
        .contract(truePropContract(ctx))
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      unsigned.getOutputs.size() shouldBe signed.getOutputsToSpend.size()
    }
  }

  property("Mint a token and rebuild it from BoxCandidate") {
    val ergoClient = createMockedErgoClient(data)
    ergoClient.execute { ctx: BlockchainContext =>
      val unsigned = BoxOperations.createForSender(address, ctx).withAmountToSpend(15000000)
        .mintTokenToContractTxUnsigned(new ErgoTreeContract(address.getErgoAddress.script, address.getNetworkType), { tokenId: String =>
          new Eip4Token(tokenId, 1, "Test name", "Test desc", 0)
        })

      val eip4Token = Eip4TokenBuilder.buildFromErgoBox(unsigned.getInputs.get(0).getId.toString, unsigned.getOutputs.get(0))

      eip4Token.getDecimals shouldBe 0
      eip4Token.getValue shouldBe 1
      eip4Token.getTokenName shouldBe "Test name"
    }
  }

}
