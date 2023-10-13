package org.ergoplatform.appkit.scalaapi

import org.scalacheck.{Gen, Arbitrary}
import org.ergoplatform.appkit.TestingBase
import org.scalacheck.Prop.{propBoolean}


class ByteArrayOrderingSpec extends TestingBase {

  implicit val arbitraryByteArray: Arbitrary[Array[Byte]] = Arbitrary(Gen.containerOf[Array, Byte](Arbitrary.arbByte.arbitrary))
  val ord = Utils.byteArrayOrdering

  property("byteArrayOrdering should satisfy required properties") {
    forAll { (a: Array[Byte], b: Array[Byte], c: Array[Byte]) =>


      // reflexivity
      propBoolean(ord.compare(a, a) == 0)
        // antisymmetry
        .&&((ord.compare(a, b) <= 0 && ord.compare(b, a) <= 0) ==> (a sameElements b))
        .&&((ord.compare(a, b) >= 0 && ord.compare(b, a) >= 0) ==> (a sameElements b))

        // transitivity
        .&&((ord.compare(a, b) <= 0 && ord.compare(b, c) <= 0) ==> (ord.compare(a, c) <= 0))
        .&&((ord.compare(a, b) >= 0 && ord.compare(b, c) >= 0) ==> (ord.compare(a, c) >= 0))
    }
  }

  property("it should compare arrays with different lengths correctly") {
      forAll { (a: Array[Byte], b: Array[Byte]) =>
        (a.length != b.length) ==> (ord.compare(a, b) != 0)
      }
  }

  property("it should compare arrays with equal elements correctly") {
      forAll { (a: Array[Byte], b: Array[Byte]) =>
        val commonPrefix = a.zip(b).takeWhile { case (x, y) => x == y }.map(_._1)
        val longer = a.length > b.length
        val shorter = a.length < b.length

        (a startsWith commonPrefix) && (b startsWith commonPrefix) ==> (
          (longer && ord.compare(a, b) > 0) ||
            (shorter && ord.compare(a, b) < 0) ||
            (ord.compare(a, b) == 0)
          )
      }
  }
}
