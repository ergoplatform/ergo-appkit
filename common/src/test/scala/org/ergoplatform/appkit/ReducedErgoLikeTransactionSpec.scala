package org.ergoplatform.appkit

import org.ergoplatform.UnsignedErgoLikeTransaction
import org.ergoplatform.sdk.{ReducedErgoLikeTransaction, ReducedErgoLikeTransactionSerializer, ReducedInputData}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import sigmastate.CrossVersionProps
import sigmastate.interpreter.ContextExtension
import sigmastate.interpreter.Interpreter.ReductionResult
import sigmastate.serialization.SigmaSerializer
import sigmastate.serialization.generators.ObjectGenerators

class ReducedErgoLikeTransactionSpec extends CrossVersionProps
    with Matchers with ScalaCheckDrivenPropertyChecks with ObjectGenerators {

  override val printDebugInfo: Boolean = false

  def reducedInputDataGen(extension: ContextExtension): Gen[ReducedInputData] = for {
    sb <- sigmaBooleanGen
    cost <- Gen.choose(10L, 1000L)
  } yield
    ReducedInputData(ReductionResult(sb, cost), extension)

  def reducedErgoLikeTransactionGen(
        unsignedTx: UnsignedErgoLikeTransaction): Gen[ReducedErgoLikeTransaction] = {
    val extensions = unsignedTx.inputs.map(ui => reducedInputDataGen(ui.extension))
    for {
      reducedInputs <- Gen.sequence[Seq[ReducedInputData], ReducedInputData](extensions)
      cost <- Gen.choose(10, 100000)
    } yield
      ReducedErgoLikeTransaction(unsignedTx, reducedInputs, cost)
  }

  implicit lazy val reducedErgoLikeTransactionGen: Gen[ReducedErgoLikeTransaction] = for {
    unsignedTx <- unsignedErgoLikeTransactionGen
    tx <- reducedErgoLikeTransactionGen(unsignedTx)
  } yield tx

  protected def roundTripTest[T](v: T)(implicit serializer: SigmaSerializer[T, T]): Assertion = {
    // using default sigma reader/writer
    val bytes = serializer.toBytes(v)
    bytes.nonEmpty shouldBe true
    if (printDebugInfo) println(bytes.length)
    val r = SigmaSerializer.startReader(bytes)
    val positionLimitBefore = r.positionLimit
    serializer.parse(r) shouldBe v
    r.positionLimit shouldBe positionLimitBefore
  }

  property("serialization roundtrip") {
    forAll { reducedTx: ReducedErgoLikeTransaction =>
      if (printDebugInfo)
        println(s"Ins: ${reducedTx.unsignedTx.inputs.size}; Outs: ${reducedTx.unsignedTx.outputCandidates.length}")
      roundTripTest(reducedTx)(ReducedErgoLikeTransactionSerializer)
    }
  }
}
