package org.ergoplatform.appkit

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}
import scalan.RType
import scorex.util.encode.Base16
import sigmastate.{SCollection, SByte}
import sigmastate.Values.Constant
import sigmastate.eval.SigmaDsl
import sigmastate.serialization.ValueSerializer

class ErgoValueSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks
  with AppkitTesting {

  def Coll[T](items: T*)(implicit cT: RType[T]) = SigmaDsl.Colls.fromItems(items:_*)

  property("create from sigma Constant hex") {
    val hex = "05e012"
    val v = ErgoValue.fromHex(hex)
    v.getValue shouldBe 1200L
    v.getType shouldBe ErgoType.longType()
  }

  property("ErgoValue with collection (hex test vector)") {
    val coll = Coll(
      Coll[Byte](1, 2, 3),
      Coll[Byte](10, 20)
    )

    val c = Constant[SCollection[SCollection[SByte.type]]](coll, SCollection(SCollection(SByte)))
    val bytes = ValueSerializer.serialize(c)
    val hex = Base16.encode(bytes)
    hex shouldBe "1a0203010203020a14"

    val t = ErgoType.collType(ErgoType.byteType)
    val collV = ErgoValue.of(coll, t)
    collV.toHex shouldBe hex

  }

}
