package org.ergoplatform.appkit.scalaapi

import org.ergoplatform.appkit.{TestingBase, AppkitTestingCommon, ErgoValue}

class ErgoValueBuilderSpec extends TestingBase with AppkitTestingCommon {
  property("buildFor") {

    val vInt = ErgoValueBuilder.buildFor(10)
    val jInt = ErgoValue.of(10)
    vInt shouldBe jInt

    val vLong = ErgoValueBuilder.buildFor(10L)
    val jLong = ErgoValue.of(10L)
    vLong shouldBe jLong

    val vPair = ErgoValueBuilder.buildFor((10, 10L))
    val jPair = ErgoValue.pairOf(jInt, jLong)
    vPair shouldBe jPair

  }
}
