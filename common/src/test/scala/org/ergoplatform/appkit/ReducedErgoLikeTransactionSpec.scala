package org.ergoplatform.appkit

import org.ergoplatform.UnsignedErgoLikeTransaction
import org.ergoplatform.sdk.{ReducedErgoLikeTransaction, ReducedErgoLikeTransactionSerializer, ReducedInputData}
import org.scalacheck.Gen
import org.scalatest.Assertion
import sigma.interpreter.ContextExtension
import sigmastate.interpreter.Interpreter.ReductionResult
import sigma.serialization.SigmaSerializer
import sigma.serialization.generators.ObjectGenerators

class ReducedErgoLikeTransactionSpec extends TestingBase
    with ObjectGenerators {

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

  protected def reducedTxRoundTripTest(v: ReducedErgoLikeTransaction): Assertion = {
    val bytes = ReducedErgoLikeTransactionSerializer.toBytes(v)
    bytes.nonEmpty shouldBe true
    if (printDebugInfo) println(bytes.length)

    val r = SigmaSerializer.startReader(bytes)
    val positionLimitBefore = r.positionLimit
    val parsed = ReducedErgoLikeTransactionSerializer.parse(r)

    // ContextExtension values are stored in a Map, so assert the converted content.
    parsed.cost shouldBe v.cost
    parsed.unsignedTx.inputs.length shouldBe v.unsignedTx.inputs.length
    parsed.unsignedTx.inputs.zip(v.unsignedTx.inputs).foreach { case (actual, expected) =>
      actual.boxId.sameElements(expected.boxId) shouldBe true
      actual.extension.values shouldBe expected.extension.values
    }
    parsed.unsignedTx.dataInputs shouldBe v.unsignedTx.dataInputs
    parsed.unsignedTx.outputCandidates shouldBe v.unsignedTx.outputCandidates
    parsed.reducedInputs.map(_.reductionResult) shouldBe v.reducedInputs.map(_.reductionResult)
    parsed.reducedInputs.map(_.extension.values) shouldBe v.reducedInputs.map(_.extension.values)
    r.positionLimit shouldBe positionLimitBefore
  }

  property("serialization roundtrip") {
    forAll { reducedTx: ReducedErgoLikeTransaction =>
      if (printDebugInfo)
        println(s"Ins: ${reducedTx.unsignedTx.inputs.size}; Outs: ${reducedTx.unsignedTx.outputCandidates.length}")
      reducedTxRoundTripTest(reducedTx)
    }
  }
}
