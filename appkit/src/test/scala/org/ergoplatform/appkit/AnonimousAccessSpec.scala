package org.ergoplatform.appkit

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class AnonimousAccessSpec extends PropSpec with Matchers
    with ScalaCheckDrivenPropertyChecks
    with AppkitTesting {


  val baseUrl = "http://138.68.98.248:9053"
  val ergoClient = RestApiErgoClient.create(baseUrl, NetworkType.MAINNET, "")


  property("Get unspent boxes containing given address") {
    val boxes: java.util.List[InputBox] = ergoClient.execute { ctx =>
      ctx.getUnspentBoxesFor(Address.create("9f4QF8AD1nQ3nJahQVkMj8hFSVVzVom77b52JU7EW71Zexg6N8v"))
    }

    boxes.forEach(b => {
      println(b.toJson(true))
    })
  }

}
