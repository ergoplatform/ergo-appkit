package org.ergoplatform.appkit

import org.ergoplatform.UnsignedErgoLikeTransaction
import org.scalacheck.Gen
import org.scalatest.{PropSpec, Matchers, Assertion}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import sigmastate.interpreter.Interpreter.ReductionResult
import sigmastate.serialization.SigmaSerializer
import sigmastate.serialization.generators.ObjectGenerators

class ReducedErgoLikeTransactionSpec extends PropSpec
    with Matchers with ScalaCheckDrivenPropertyChecks with ObjectGenerators {

  lazy val reducedInputDataGen: Gen[ReducedInputData] = for {
    sb <- sigmaBooleanGen
    cost <- Gen.choose(10L, 1000L)
    extension <- contextExtensionGen
  } yield
    ReducedInputData(ReductionResult(sb, cost), extension)

  def reducedErgoLikeTransactionGen(unsignedTx: UnsignedErgoLikeTransaction): Gen[ReducedErgoLikeTransaction] = for {
    reducedInputs <- arrayOfN(unsignedTx.inputs.length, reducedInputDataGen)
  } yield
    ReducedErgoLikeTransaction(unsignedTx, reducedInputs )

  implicit lazy val reducedErgoLikeTransactionGen: Gen[ReducedErgoLikeTransaction] = for {
    unsignedTx <- unsignedErgoLikeTransactionGen
    tx <- reducedErgoLikeTransactionGen(unsignedTx)
  } yield tx

  protected def roundTripTest[T](v: T)(implicit serializer: SigmaSerializer[T, T]): Assertion = {
    // using default sigma reader/writer
    val bytes = serializer.toBytes(v)
    bytes.nonEmpty shouldBe true
    val r = SigmaSerializer.startReader(bytes)
    val positionLimitBefore = r.positionLimit
    serializer.parse(r) shouldBe v
    r.positionLimit shouldBe positionLimitBefore
  }

  property("serialization roundtrip") {
    forAll { reducedTx: ReducedErgoLikeTransaction =>
      roundTripTest(reducedTx)(ReducedErgoLikeTransactionSerializer)
    }
  }
}
