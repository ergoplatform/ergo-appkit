package org.ergoplatform.appkit

import scorex.util.encode.Base16
import sigmastate._
import sigmastate.Values.Constant
import sigmastate.serialization.ValueSerializer
import sigmastate.serialization.generators.ObjectGenerators
import JavaHelpers._
import special.collection.Coll

class ErgoValueSpec extends TestingBase with AppkitTestingCommon with ObjectGenerators {

  def constToHex[T <: SType](c: Constant[T]): String = {
    val bytes = ValueSerializer.serialize(c)
    val hex = Base16.encode(bytes)
    hex
  }

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
    val hex = constToHex(c)
    hex shouldBe "1a0203010203020a14"

    val t = ErgoType.collType(ErgoType.byteType)
    val collV = ErgoValue.of(coll.convertTo[Coll[Coll[java.lang.Byte]]], t)
    collV.toHex shouldBe hex

  }

  property("fromHex/toHex roundtrip") {
    def test[T <: SType](tpe: T) = {
      implicit val wWrapped = wrappedTypeGen(tpe)
      forAll { v: T#WrappedType =>
        val c = Constant(v, tpe)
        val hex = constToHex(c)
        val ev = ErgoValue.fromHex(hex)
        ev.toHex shouldBe hex
        ev.getValue shouldBe v
      }
    }
    test(SByte)
    test(SShort)
    test(SInt)
    test(SLong)
    test(SBigInt)
    test(SGroupElement)
    test(SSigmaProp)
    test(SAvlTree)
  }

  property("checkTypeDeclaration") {
    val intErgoValue = ErgoValue.of(1)

    val intValue: Integer = intErgoValue.getValue

    intValue shouldBe 1
  }
}
