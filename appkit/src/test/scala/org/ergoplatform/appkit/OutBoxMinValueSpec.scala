package org.ergoplatform.appkit

import org.ergoplatform.appkit.impl.UnsignedTransactionImpl
import org.ergoplatform.appkit.testing.AppkitTesting
import org.ergoplatform.sdk.ErgoToken
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec

class OutBoxMinValueSpec extends AnyPropSpec with Matchers
  with AppkitTesting
  with HttpClientTesting {

  private val mockTxId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"

  property("OutBoxBuilder.getMinBoxValue and OutBox.getMinBoxValue satisfy node minimum box value rule") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))

    ergoClient.execute { ctx: BlockchainContext =>
      val contract = address.toErgoContract
      val minValuePerByte = ctx.getDataSource.getParameters.getMinValuePerByte

      // Requires contract to be defined before calling getMinBoxValue
      val missingContractEx = intercept[IllegalStateException] {
        ctx.newTxBuilder().outBoxBuilder().getMinBoxValue()
      }
      missingContractEx.getMessage should include("Contract is not defined")

      // Negative minValuePerByte is rejected
      intercept[IllegalArgumentException] {
        ctx.newTxBuilder().outBoxBuilder().contract(contract).getMinBoxValue(-1)
      }

      // Simple box: computed minimum >= actual serialized box size * minValuePerByte
      val simpleBuilder = ctx.newTxBuilder().outBoxBuilder().contract(contract)
      val simpleMin = simpleBuilder.getMinBoxValue()
      simpleMin shouldBe simpleBuilder.getMinBoxValue(minValuePerByte)
      val simpleOut = simpleBuilder.value(simpleMin).build()
      simpleOut.getMinBoxValue(minValuePerByte) shouldBe simpleMin

      val input = ctx.newTxBuilder().outBoxBuilder()
        .value(Parameters.OneErg * 5)
        .contract(contract)
        .build()
        .convertToInputWith(mockTxId, 0)

      val simpleTx = ctx.newTxBuilder()
        .addInputs(input)
        .addOutputs(simpleOut)
        .fee(Parameters.MinFee)
        .sendChangeTo(address.getErgoAddress)
        .build()
      val actualSimpleOut = simpleTx.asInstanceOf[UnsignedTransactionImpl].getTx.outputs.head
      actualSimpleOut.value shouldBe simpleMin
      actualSimpleOut.value should be >= (actualSimpleOut.bytes.length.toLong * minValuePerByte.toLong)

      // Box with 100 tokens and registers (where minimum exceeds Parameters.MinChangeValue)
      val tokens100 = (1 to 100).map(i => new ErgoToken(f"$i%064x", 1L))
      val tokenInput = ctx.newTxBuilder().outBoxBuilder()
        .value(Parameters.OneErg * 5)
        .tokens(tokens100: _*)
        .contract(contract)
        .build()
        .convertToInputWith(mockTxId, 1)

      val richBuilder = ctx.newTxBuilder().outBoxBuilder()
        .contract(contract)
        .tokens(tokens100: _*)
        .registers(ErgoValue.of(42L), ErgoValue.of(Array.fill[Byte](128)(7.toByte)))
      val richMin = richBuilder.getMinBoxValue()
      richMin should be > Parameters.MinChangeValue

      val richOut = richBuilder.value(richMin).build()
      richOut.getMinBoxValue(minValuePerByte) shouldBe richMin

      val richTx = ctx.newTxBuilder()
        .addInputs(tokenInput)
        .addOutputs(richOut)
        .fee(Parameters.MinFee)
        .sendChangeTo(address.getErgoAddress)
        .build()
      val actualRichOut = richTx.asInstanceOf[UnsignedTransactionImpl].getTx.outputs.head
      actualRichOut.value shouldBe richMin
      actualRichOut.value should be >= (actualRichOut.bytes.length.toLong * minValuePerByte.toLong)

      // Zero minValuePerByte returns 0L
      richBuilder.getMinBoxValue(0) shouldBe 0L
      richOut.getMinBoxValue(0) shouldBe 0L
    }
  }
}
