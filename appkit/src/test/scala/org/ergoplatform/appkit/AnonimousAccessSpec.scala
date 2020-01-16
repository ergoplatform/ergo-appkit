package org.ergoplatform.appkit

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
    val aliceDlog = alice.getPublicKey
    val g_x = aliceDlog.value

    val bob = Address.create("9hHDQb26AjnJUXxcqriqY1mnhpLuUeC81C4pggtK7tupr92Ea1K")
    val bobDlog = bob.getPublicKey
    val g_y = bobDlog.value

    val res = ergoClient.execute { ctx: BlockchainContext =>
      val bobProver = BoxOperations.createProver(ctx, "storage/E2.json", "abc")

      ctx.getUnspentBoxesFor(Address.create(addr1))
    }

  }
}

