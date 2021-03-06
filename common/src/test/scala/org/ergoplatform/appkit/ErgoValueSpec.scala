package org.ergoplatform.appkit

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class ErgoValueSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks
  with AppkitTesting {

  property("create from sigma Constant hex") {
    val hex = "05e012"
    val v = ErgoValue.fromHex(hex)
    v.getValue shouldBe 1200L
    v.getType shouldBe ErgoType.longType()
  }
}
