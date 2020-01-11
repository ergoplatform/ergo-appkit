package org.ergoplatform.appkit

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}

class AnonimousAccessSpec extends PropSpec with Matchers
    with ScalaCheckDrivenPropertyChecks
    with AppkitTesting
    with HttpClientTesting {

  property("Get unspent boxes containing given address") {
    val data = MockData(
      Seq(
        loadNodeResponse("response_Box1.json"),
        loadNodeResponse("response_Box2.json"),
        loadNodeResponse("response_Box3.json")),
      Seq(
        loadExplorerResponse("response_boxesByAddressUnspent.json")))

    val ergoClient = createMockedErgoClient(data)

    val boxes: java.util.List[InputBox] = ergoClient.execute { ctx: BlockchainContext =>
      ctx.getUnspentBoxesFor(Address.create(addr1))
    }
    boxes.forEach { b: InputBox =>
      println(b.toJson(true))
    }
  }

}

