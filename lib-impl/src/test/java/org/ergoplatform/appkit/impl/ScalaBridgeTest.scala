package org.ergoplatform.appkit.impl

import org.ergoplatform.appkit.impl.ScalaBridge.isoSpendingProof
import org.ergoplatform.restapi.client.SpendingProof
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}
import sigmastate.Values.{ByteArrayConstant, IntConstant}
import sigmastate.interpreter.{ContextExtension, ProverResult}
import sigmastate.serialization.generators.ObjectGenerators

import scala.collection.JavaConverters
import JavaConverters._

class ScalaBridgeTest extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks
  with ObjectGenerators {
  property("isoSpendingProof") {
    val fakeProof = Array[Byte](0, 1, 2)
    val ext = ContextExtension(Map(
      1.toByte -> IntConstant(10),
      2.toByte -> ByteArrayConstant(Array[Byte](1, 2, 3)))
    )
    val res = ProverResult(fakeProof, ext)
    val p = isoSpendingProof.from(res)
    val expected = new SpendingProof()
      .proofBytes("000102")
      .extension(
        Map(
          "2" -> "0e03010203", // note, the order doesn't matter
          "1" -> "0414").asJava
      )
    p shouldBe expected
    val res2 = isoSpendingProof.to(p)
    res2 shouldBe res
  }

  property("isoSpendingProof identity") {
    forAll(MinSuccessful(100)) { res: ProverResult =>
      val p = isoSpendingProof.from(res)
      isoSpendingProof.to(p) shouldBe res

      isoSpendingProof.from(isoSpendingProof.to(p)) shouldBe p
    }
  }
}
