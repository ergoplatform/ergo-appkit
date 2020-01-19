package org.ergoplatform.appkit

import java.util

import org.ergoplatform.appkit.BoxOperations.{createProver, putToContractTx, loadTop}
import org.ergoplatform.appkit.ErgoContracts.sendToPK
import org.ergoplatform.appkit.Parameters.MinFee
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}

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

  // see original example in sigma https://github.com/ScorexFoundation/sigmastate-interpreter/blob/b3695bdb785c9b3a94545ffea506358ee3f8ed3d/sigmastate/src/test/scala/sigmastate/utxo/examples/DHTupleExampleSpecification.scala#L28
  property("ProveDHTuple") {
    val ergoClient = createMockedErgoClient(
      data.appendExplorerResponses(Array(loadExplorerResponse("E1/response_boxesByAddressUnspent.json")))
          .appendNodeResponses(Array(loadNodeResponse("E1/response_Box1.json")))
    )
//    val alice = Address.create("9f4QF8AD1nQ3nJahQVkMj8hFSVVzVom77b52JU7EW71Zexg6N8v")
    val aliceStorage = "storage/E2.json"

//    val bob = Address.create("9hHDQb26AjnJUXxcqriqY1mnhpLuUeC81C4pggtK7tupr92Ea1K")
    val bobStorage = "storage/E1.json"

    val res = ergoClient.execute { ctx: BlockchainContext =>

      val aliceProver = createProver(ctx, aliceStorage, "abc").build
      val g_x = aliceProver.getAddress.getPublicKeyGE

      val bobProver = createProver(ctx, bobStorage, "abc")
              .withSecondDHTSecret(aliceProver.getAddress)
              .build
      val g_y = bobProver.getAddress.getPublicKeyGE
      val g_xy = g_x.exp(bobProver.getSecretKey)

      val contract = ctx.compileContract(
        ConstantsBuilder.create().item("g_x", g_x).build(),
        """{
         |  val g_y = OUTPUTS(0).R4[GroupElement].get
         |  val g_xy = OUTPUTS(0).R5[GroupElement].get
         |
         |  proveDHTuple(groupGenerator, g_x, g_y, g_xy) || // for bob
         |  proveDHTuple(groupGenerator, g_y, g_x, g_xy)    // for alice
         |}""".stripMargin);
      val aliceTx = putToContractTx(ctx, aliceProver, contract, MinFee)
      val contractBox = aliceTx.getOutputsToSpend().get(0)
      val amountOnContract = contractBox.getValue

      val txB = ctx.newTxBuilder
      val aliceBox = txB.outBoxBuilder
        .value(amountOnContract)
        .contract(sendToPK(ctx, aliceProver.getAddress))
        .registers(ErgoValue.of(g_y), ErgoValue.of(g_xy))
        .build

      val boxesToPayFee = loadTop(ctx, bobProver.getAddress, MinFee)

      val inputs = new util.ArrayList[InputBox]()
      inputs.add(contractBox)
      inputs.addAll(boxesToPayFee)

      val tx = txB.boxesToSpend(inputs)
        .outputs(aliceBox)
        .fee(MinFee)
        .sendChangeTo(bobProver.getP2PKAddress).build

      val bobTx = bobProver.sign(tx)
      println(bobTx.toJson(true))
    }

  }
}

