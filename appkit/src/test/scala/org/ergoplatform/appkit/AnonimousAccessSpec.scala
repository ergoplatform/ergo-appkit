package org.ergoplatform.appkit

import java.util

import org.ergoplatform.appkit.BoxOperations.{createProver, putToContractTx}
import org.ergoplatform.appkit.ErgoContracts.sendToPK
import org.ergoplatform.appkit.Parameters.MinFee
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}
import sigmastate.basics.DLogProtocol.ProveDlog

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
    val ergoClient = createMockedErgoClient(data)
    val alice = Address.create("9f4QF8AD1nQ3nJahQVkMj8hFSVVzVom77b52JU7EW71Zexg6N8v")
    val aliceStorage = "storage/E1.json"
    val aliceDlog = alice.getPublicKey

    val bob = Address.create("9hHDQb26AjnJUXxcqriqY1mnhpLuUeC81C4pggtK7tupr92Ea1K")
    val bobStorage = "storage/E2.json"
    val bobDlog = bob.getPublicKey
    val g_y = bobDlog.value

    val res = ergoClient.execute { ctx: BlockchainContext =>

      val aliceProver = createProver(ctx, aliceStorage, "abc").build
      val g_x = aliceProver.getAddress.getPublicKeyGE

      val bobProver = createProver(ctx, bobStorage, "abc")
              .withSecondDHTSecret(alice)
              .build

      val contract = ctx.compileContract(
        ConstantsBuilder.create().item("g_x", g_x).build(),
        """{
         |  val g_y = OUTPUTS(0).R4[GroupElement].get
         |  val g_xy = OUTPUTS(0).R5[GroupElement].get
         |
         |  proveDHTuple(g, g_x, g_y, g_xy) || // for bob
         |  proveDHTuple(g, g_y, g_x, g_xy)    // for alice
         |}""".stripMargin);
      val aliceTx = putToContractTx(ctx, aliceProver, contract, MinFee)
      val contractBox = aliceTx.getOutputsToSpend().get(0)
      val amountOnContract = contractBox.getValue

      val txB = ctx.newTxBuilder
      val aliceBox = txB.outBoxBuilder
        .value(amountOnContract)
        .contract(sendToPK(ctx, aliceProver.getAddress)).build

      val boxesToPayFee = BoxOperations.loadTop(ctx, bobProver.getAddress, MinFee)

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

