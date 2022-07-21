package org.ergoplatform.appkit.scalaapi

import org.ergoplatform.appkit.{ErgoValue, ErgoType, TestingBase, AppkitTestingCommon}
import special.collection.Coll

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

    val vCollInt = ErgoValueBuilder.buildFor(Coll(10, 20))
    val jCollInt = ErgoValue.of(Coll(10, 20).asInstanceOf[Coll[Integer]], ErgoType.integerType())
    vCollInt shouldBe jCollInt

    val vCollPair = ErgoValueBuilder.buildFor(Coll((10, 10L), (20, 20L)))
    val jCollPair = ErgoValue.of(
      Coll((10, 10L), (20, 20L)).asInstanceOf[Coll[(Integer, java.lang.Long)]],
      ErgoType.pairType(ErgoType.integerType(), ErgoType.longType())
    )
    vCollPair shouldBe jCollPair

  }
}
