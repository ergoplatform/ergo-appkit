package org.ergoplatform.appkit

import org.ergoplatform.appkit.testing.AppkitTesting
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import sigmastate.eval._
import sigmastate.interpreter.CryptoConstants
import special.sigma.GroupElement

import scala.util.Try

class MultiProveDHTSpec extends PropSpec with Matchers
  with ScalaCheckDrivenPropertyChecks
  with AppkitTesting
  with HttpClientTesting {

  property("Multi DHTProver") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    val g: GroupElement = CryptoConstants.dlogGroup.generator
    val x = BigInt("187235612876647164378132684712638457631278").bigInteger
    val y = BigInt("340956873409567839086738967389673896738906").bigInteger
    val gX:GroupElement = g.exp(x)
    val gY:GroupElement = g.exp(y)
    val gXY:GroupElement = gX.exp(y)

    val x1 = BigInt("287235612876647164378132684712638457").bigInteger
    val y1 = BigInt("640956873409567839086738967389673896").bigInteger

    val gX1:GroupElement = g.exp(x1)
    val gY1:GroupElement = g.exp(y1)
    val gX1Y1:GroupElement = gX1.exp(y1)


    ergoClient.execute { ctx: BlockchainContext =>
      val input1 = ctx.newTxBuilder.outBoxBuilder.registers(
        ErgoValue.of(gY), ErgoValue.of(gX), ErgoValue.of(gXY)
      ).value(20000000).contract(ctx.compileContract(
        ConstantsBuilder.empty(),
        """proveDHTuple(groupGenerator, SELF.R4[GroupElement].get, SELF.R5[GroupElement].get, SELF.R6[GroupElement].get)""".stripMargin
      )).build().convertToInputWith("f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809", 0)

      val input2 = ctx.newTxBuilder.outBoxBuilder.registers(
        ErgoValue.of(gY1), ErgoValue.of(gX1), ErgoValue.of(gX1Y1)
      ).value(20000000).contract(ctx.compileContract(
        ConstantsBuilder.empty(),
        """proveDHTuple(groupGenerator, SELF.R4[GroupElement].get, SELF.R5[GroupElement].get, SELF.R6[GroupElement].get)""".stripMargin
      )).build().convertToInputWith("f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809", 1)

      val txB = ctx.newTxBuilder()
      val output = txB.outBoxBuilder()
        .value(20000000)
        .contract(truePropContract(ctx)).build()
      val inputs = new java.util.ArrayList[InputBox]()
      inputs.add(input1)
      inputs.add(input2)

      // below is ergoTree of a random box picked from the block explorer. The boxId is 02abc29b6a28ccf7e9620afa16e1067caeb75fcd2e62c066e190742962cdcbae
      // We just need valid ergoTree to construct the change address
      val tree = "100207036ba5cfbc03ea2471fdf02737f64dbcd58c34461a7ec1e586dcd713dacbf89a120400d805d601db6a01ddd6027300d603b2a5730100d604e4c672030407d605e4c672030507eb02ce7201720272047205ce7201720472027205"
      val ergoTree = JavaHelpers.decodeStringToErgoTree(tree)
      val changeAddr = Address.fromErgoTree(ergoTree, NetworkType.MAINNET).getErgoAddress
      val unsigned = txB.boxesToSpend(inputs).outputs(output).fee(10000000).sendChangeTo(changeAddr).build()

      // DHT with two different secrets
      Try(ctx.newProverBuilder().withDHTData(g, gY, gX, gXY, x).withDHTData(g, gY1, gX1, gX1Y1, x1).build().sign(unsigned)).isSuccess shouldBe true

      // DHT with invalid secret(s)
      // Currently due to a bug in the prover, using an invalid secret does not throw an exception. When the bug is fixed, the following should be changed to false
      // See https://github.com/ScorexFoundation/sigmastate-interpreter/pull/636
      Try(ctx.newProverBuilder().withDHTData(g, gY, gX, gXY, BigInt(1).bigInteger).withDHTData(g, gY1, gX1, gX1Y1, x1).build().sign(unsigned)).isSuccess shouldBe true

      // DHT with duplicate secrets
      Try(ctx.newProverBuilder().withDHTData(g, gY, gX, gXY, x).withDHTData(g, gY1, gX1, gX1Y1, x1).withDHTData(g, gY, gX, gXY, x).build().sign(unsigned)).isSuccess shouldBe false
    }
  }
}


