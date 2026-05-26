package org.ergoplatform.appkit.impl

import org.ergoplatform.Input
import org.ergoplatform.appkit.{AppkitIso, ErgoValue}
import org.ergoplatform.appkit.impl.ScalaBridge.isoSpendingProof
import org.ergoplatform.restapi.client._
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scorex.crypto.authds.ADKey
import sigma.ast.{ByteArrayConstant, ErgoTree, EvaluatedValue, IntConstant, SType}
import sigma.data.TrivialProp
import sigma.interpreter.{ContextExtension, ProverResult}
import sigma.serialization.generators.ObjectGenerators

import java.lang.{Byte => JByte}
import scala.collection.JavaConverters
import JavaConverters._

class ScalaBridgeTest extends AnyPropSpec with Matchers with ScalaCheckDrivenPropertyChecks
  with ObjectGenerators {
  private def id(byteHex: String): String = byteHex * 32

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

  property("contextExtensionValuesAsJava preserves boxed byte keys and evaluated values") {
    val res = ProverResult(
      Array[Byte](0, 1, 2),
      ContextExtension(Map(
        1.toByte -> IntConstant(10),
        2.toByte -> ByteArrayConstant(Array[Byte](1, 2, 3)))))
    val input = new Input(ADKey @@ Array.fill(32)(1.toByte), res)

    val javaMap = ScalaBridge.contextExtensionValuesAsJava(input)

    javaMap.size shouldBe 2
    AppkitIso.isoErgoValueToSValue.from(
      javaMap.get(JByte.valueOf(1.toByte)).asInstanceOf[EvaluatedValue[SType]]
    ) shouldBe ErgoValue.of(10)
    AppkitIso.isoErgoValueToSValue.from(
      javaMap.get(JByte.valueOf(2.toByte)).asInstanceOf[EvaluatedValue[SType]]
    ).toHex shouldBe ErgoValue.of(Array[Byte](1, 2, 3)).toHex
  }

  property("isoErgoTransaction preserves proof extensions, data inputs, output tokens and registers") {
    val proof = new SpendingProof()
      .proofBytes("000102")
      .extension(Map("1" -> "0414").asJava)
    val input = new ErgoTransactionInput()
      .boxId(id("01"))
      .spendingProof(proof)
    val dataInput = new ErgoTransactionDataInput()
      .boxId(id("02"))
    val registers = new Registers()
    registers.put("R4", "0414")
    val output = new ErgoTransactionOutput()
      .boxId(id("03"))
      .value(1000000L)
      .ergoTree(ScalaBridge.isoStringToErgoTree.from(ErgoTree.fromProposition(TrivialProp.TrueProp)))
      .assets(Seq(new Asset().tokenId(id("04")).amount(7L)).asJava)
      .additionalRegisters(registers)
      .creationHeight(100)
      .transactionId(id("05"))
      .index(Integer.valueOf(0))
    val apiTx = new ErgoTransaction()
      .id(id("06"))
      .inputs(Seq(input).asJava)
      .dataInputs(Seq(dataInput).asJava)
      .outputs(Seq(output).asJava)

    val roundtrip = ScalaBridge.isoErgoTransaction.from(ScalaBridge.isoErgoTransaction.to(apiTx))

    roundtrip.getInputs.get(0).getBoxId shouldBe input.getBoxId
    roundtrip.getInputs.get(0).getSpendingProof shouldBe proof
    roundtrip.getDataInputs.get(0).getBoxId shouldBe dataInput.getBoxId
    val roundtripOutput = roundtrip.getOutputs.get(0)
    roundtripOutput.getValue shouldBe output.getValue
    roundtripOutput.getErgoTree shouldBe output.getErgoTree
    roundtripOutput.getAssets shouldBe output.getAssets
    roundtripOutput.getAdditionalRegisters shouldBe output.getAdditionalRegisters
    roundtripOutput.getCreationHeight shouldBe output.getCreationHeight
    roundtripOutput.getIndex shouldBe output.getIndex
  }
}
