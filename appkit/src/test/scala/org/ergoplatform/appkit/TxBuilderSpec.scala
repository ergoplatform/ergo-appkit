package org.ergoplatform.appkit

import java.math.BigInteger
import java.util.Arrays

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}
import sigmastate.eval.CBigInt

class TxBuilderSpec extends PropSpec with Matchers
  with ScalaCheckDrivenPropertyChecks
  with AppkitTesting
  with HttpClientTesting {

  def createTestInput(ctx: BlockchainContext): InputBox = {
    ctx.newTxBuilder.outBoxBuilder
      .value(30000000)
      .contract(ctx.compileContract(
        ConstantsBuilder.empty(),
        """{
         |  val v1 = getVar[Int](1).get
         |  val v10 = getVar[BigInt](10).get
         |  sigmaProp(v1.toBigInt == v10)
         |}""".stripMargin))
      .build().convertToInputWith(
      "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809",
      0)
  }

  property("InputBox support context variables") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val contextVars = Seq(
        ContextVar.of(1.toByte, 100),
        ContextVar.of(10.toByte, CBigInt(BigInteger.valueOf(100)))
      )
      val input = createTestInput(ctx)
        .withContextVars(contextVars:_*)
      val txB = ctx.newTxBuilder()
      val output = txB.outBoxBuilder()
        .value(15000000)
        .contract(ctx.compileContract(
          ConstantsBuilder.empty(),"{sigmaProp(true)}"))
        .build()

      val changeAddr = Address.fromErgoTree(input.getErgoTree, NetworkType.MAINNET).getErgoAddress
      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(changeAddr)
        .build()
      // alice signing bob's box. Does not work here but works in other cases.
      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      // check the signed transaction contains all the context variables
      // we attached to the input box
      val extensions = signed.getSignedInputs.get(0).getContextVars
      contextVars.foreach { cv =>
        extensions.containsKey(cv.getId) shouldBe true
        extensions.get(cv.getId) shouldBe cv.getValue
      }
    }
  }
}
