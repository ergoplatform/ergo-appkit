package org.ergoplatform.appkit

import java.util

import org.ergoplatform.appkit.BoxOperations.{createProver, putToContractTx, loadTop}
import org.ergoplatform.appkit.ErgoContracts.sendToPK
import org.ergoplatform.appkit.Parameters.MinFee
import org.ergoplatform.appkit.config.ErgoToolConfig
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}
import sigmastate.lang.exceptions.InterpreterException

class AnonimousAccessSpec extends PropSpec with Matchers
    with ScalaCheckDrivenPropertyChecks
    with AppkitTesting
    with HttpClientTesting {

  val data = MockData(
    Seq(
      loadNodeResponse("response_Box1.json"),
      loadNodeResponse("response_Box2.json"),
      loadNodeResponse("response_Box3.json")),
    Seq(
      loadExplorerResponse("response_boxesByAddressUnspent.json")))

  property("Get unspent boxes containing given address") {
    val ergoClient = createMockedErgoClient(data)

    val boxes: java.util.List[InputBox] = ergoClient.execute { ctx: BlockchainContext =>
      ctx.getUnspentBoxesFor(Address.create(addr1))
    }
    boxes.forEach { b: InputBox =>
      println(b.toJson(true))
    }
  }

  property("PreHeader building") {
    val ergoClient = createMockedErgoClient(data)

    val signed = ergoClient.execute { ctx: BlockchainContext =>
      val aliceProver = createProver(ctx, "storage/E2.json", "abc").build
      val aliceAddr = aliceProver.getAddress
      val alicePk = aliceAddr.getPublicKeyGE

      val firstTx = BoxOperations.putToContractTx(ctx, aliceProver,
        ctx.compileContract(
          ConstantsBuilder.create.item("recipientGE", aliceAddr.getPublicKeyGE).build,
          "{ sigmaProp(CONTEXT.preHeader.minerPk == recipientGE) }"),
        MinFee * 2)
      val theBox = firstTx.getOutputsToSpend().get(0)

      val ph = ctx.createPreHeader()
        .height(ctx.getHeight + 1)
        .minerPk(alicePk).build()

      // TODO move this tx building code to some reusable method
      val txB = ctx.newTxBuilder().preHeader(ph)
      BoxOperations.spendBoxesTx(ctx, txB, util.Arrays.asList(theBox), aliceProver, aliceAddr, MinFee, MinFee)

      an[InterpreterException] shouldBe thrownBy {
        val txB = ctx.newTxBuilder()
        BoxOperations.spendBoxesTx(ctx, txB, util.Arrays.asList(theBox), aliceProver, aliceAddr, MinFee, MinFee)
      }
    }
  }

  property("getSignedInputs") {
    val ergoClient = createMockedErgoClient(data)

    val signed = ergoClient.execute[SignedTransaction] { ctx: BlockchainContext =>
      val aliceProver = createProver(ctx, "storage/E2.json", "abc").build
      val aliceAddr = aliceProver.getAddress
      val pkContract = ErgoContracts.sendToPK(ctx, aliceAddr)
      val signed = putToContractTx(ctx, aliceProver, pkContract, MinFee)
      signed
    }
    val in = signed.getSignedInputs.get(0)
    in.getProofBytes should not be empty
    in.getContextVars shouldBe empty
    assert(in.getTransaction eq signed)
  }

  property("signedTxFromJson") {
    val ergoClient = createMockedErgoClient(data)

    ergoClient.execute { ctx: BlockchainContext =>
      val aliceProver = createProver(ctx, "storage/E2.json", "abc").build
      val aliceAddr = aliceProver.getAddress
      val pkContract = ErgoContracts.sendToPK(ctx, aliceAddr)
      val signed = putToContractTx(ctx, aliceProver, pkContract, MinFee)
      val json = signed.toJson(false)
      val parsed = ctx.signedTxFromJson(json)
      val json2 = parsed.toJson(false)
      json shouldBe json2
    }
  }

  // see original example in sigma https://github.com/ScorexFoundation/sigmastate-interpreter/blob/b3695bdb785c9b3a94545ffea506358ee3f8ed3d/sigmastate/src/test/scala/sigmastate/utxo/examples/DHTupleExampleSpecification.scala#L28
  property("ProveDHTuple") {
    val ergoClient = createMockedErgoClient(
      data.appendExplorerResponses(Array(loadExplorerResponse("E1/response_boxesByAddressUnspent.json")))
          .appendNodeResponses(Array(loadNodeResponse("E1/response_Box1.json")))
    )
    val aliceStorage = "storage/E2.json"
    val bobStorage = "storage/E1.json"

    val res = ergoClient.execute { ctx: BlockchainContext =>

      val aliceProver = createProver(ctx, aliceStorage, "abc").build
      val aliceTx = DhtUtils.sendToDhtContractTx(ctx, aliceProver, MinFee)
      val contractBox = aliceTx.getOutputsToSpend().get(0)

      val bobProver = createProver(ctx, bobStorage, "abc")
              .withSecondDHTSecret(aliceProver.getAddress)
              .build

      val bobTx = DhtUtils.spendDhtBox(ctx, aliceProver, bobProver, contractBox)
      println(bobTx.toJson(true))
    }
  }

}

object DhtUtils {
  val aliceStorage = "storage/E2.json"
  val bobStorage = "storage/E1.json"

  def sendToDhtContractTx(ctx: BlockchainContext, aliceProver: ErgoProver, amountToSend: Long): SignedTransaction = {
    val g_x = aliceProver.getAddress.getPublicKeyGE

    val contract = ctx.compileContract(
      ConstantsBuilder.create().item("g_x", g_x).build(),
      """{
       |  val g_y = OUTPUTS(0).R4[GroupElement].get
       |  val g_xy = OUTPUTS(0).R5[GroupElement].get
       |
       |  proveDHTuple(groupGenerator, g_x, g_y, g_xy) || // for bob
       |  proveDHTuple(groupGenerator, g_y, g_x, g_xy)    // for alice
       |}""".stripMargin);
    val aliceTx = putToContractTx(ctx, aliceProver, contract, amountToSend)
    aliceTx
  }

  def spendDhtBox(
        ctx: BlockchainContext,
        aliceProver: ErgoProver, bobProver: ErgoProver, dhtBox: InputBox): SignedTransaction = {
    val g_x = aliceProver.getAddress.getPublicKeyGE
    val g_y = bobProver.getAddress.getPublicKeyGE
    val g_xy = g_x.exp(bobProver.getSecretKey)

    val txB = ctx.newTxBuilder
    val aliceBox = txB.outBoxBuilder
        .value(dhtBox.getValue)
        .contract(sendToPK(ctx, aliceProver.getAddress))
        .registers(ErgoValue.of(g_y), ErgoValue.of(g_xy))
        .build

    val boxesToPayFee = loadTop(ctx, bobProver.getAddress, MinFee)

    val inputs = new util.ArrayList[InputBox]()
    inputs.add(dhtBox)
    inputs.addAll(boxesToPayFee)

    val tx = txB.boxesToSpend(inputs)
        .outputs(aliceBox)
        .fee(MinFee)
        .sendChangeTo(bobProver.getP2PKAddress).build

    val bobTx = bobProver.sign(tx)
    bobTx
  }

}

object RunAlice {
  import DhtUtils._

  def main(args: Array[String]): Unit = {
    val toolConf = ErgoToolConfig.load("ergotool.json")

    val ergoClient = RestApiErgoClient.create(toolConf.getNode)

    val tx: SignedTransaction = ergoClient.execute { ctx: BlockchainContext =>
      val aliceProver = createProver(ctx, aliceStorage, "abc").build
      val aliceTx = sendToDhtContractTx(ctx, aliceProver, MinFee)
      ctx.sendTransaction(aliceTx)
      aliceTx
    }

    println(tx.toJson(true))
  }
}

object RunBob {
  import DhtUtils._

  def main(args: Array[String]): Unit = {
    val toolConf = ErgoToolConfig.load("ergotool.json")

    val ergoClient = RestApiErgoClient.create(toolConf.getNode)

    ergoClient.execute { ctx: BlockchainContext =>

      val aliceProver = createProver(ctx, aliceStorage, "abc").build

      val bobProver = createProver(ctx, bobStorage, "abc")
          .withSecondDHTSecret(aliceProver.getAddress)
          .build

      val contractBox = ctx.getBoxesById("02abc29b6a28ccf7e9620afa16e1067caeb75fcd2e62c066e190742962cdcbae")(0)

      val bobTx = spendDhtBox(ctx, aliceProver, bobProver, contractBox)
      ctx.sendTransaction(bobTx)

      println(bobTx.toJson(true))
    }
  }
}
