package org.ergoplatform.appkit

import org.ergoplatform.appkit.testing.AppkitTesting
import org.ergoplatform.sdk.JavaHelpers
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import sigma.crypto.CryptoConstants
import sigma.GroupElement
import sigma.data.CBigInt
import sigma.util.Extensions.{BigIntegerOps, BigIntOps, EcpOps}

import java.math.BigInteger

class DHTProverSpec extends AnyPropSpec with Matchers
  with ScalaCheckDrivenPropertyChecks
  with AppkitTesting
  with HttpClientTesting {

  property("DHTProver") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    val g: GroupElement = CryptoConstants.dlogGroup.generator.toGroupElement
    val x = CBigInt(new BigInteger("187235612876647164378132684712638457631278"))
    val y = CBigInt(new BigInteger("340956873409567839086738967389673896738906"))
    val gX:GroupElement = g.exp(x)
    val gY:GroupElement = g.exp(y)
    val gXY:GroupElement = gX.exp(y)

    ergoClient.execute { ctx: BlockchainContext =>
      val input = ctx.newTxBuilder.outBoxBuilder.registers(
        ErgoValue.of(gY), ErgoValue.of(gXY) // <--- correct one, (Alice's full mix box)
        //        ErgoValue.of(gXY), ErgoValue.of(gY) // <--- wrong one, (Bob's full mix box). Alice should not be able to sign.
      ).value(20000000).contract(ctx.compileContract(
        ConstantsBuilder.empty(),
        """{
          |  val gY = SELF.R4[GroupElement].get
          |  val gXY = SELF.R5[GroupElement].get
          |  proveDHTuple(gY, gY, gXY, gXY)
          |}""".stripMargin
      )).build().convertToInputWith("f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809", 0)

      val txB = ctx.newTxBuilder()
      val output = txB.outBoxBuilder()
        .value(10000000)
        .contract(truePropContract(ctx)).build()
      val inputs = new java.util.ArrayList[InputBox]()
      inputs.add(input)

      // below is ergoTree of a random box picked from the block explorer. The boxId is 02abc29b6a28ccf7e9620afa16e1067caeb75fcd2e62c066e190742962cdcbae
      // We just need valid ergoTree to construct the change address
      val tree = "100207036ba5cfbc03ea2471fdf02737f64dbcd58c34461a7ec1e586dcd713dacbf89a120400d805d601db6a01ddd6027300d603b2a5730100d604e4c672030407d605e4c672030507eb02ce7201720272047205ce7201720472027205"
      val ergoTree = JavaHelpers.decodeStringToErgoTree(tree)
      val changeAddr = Address.fromErgoTree(ergoTree, NetworkType.MAINNET).getErgoAddress
      val unsigned = txB.boxesToSpend(inputs).outputs(output).fee(10000000).sendChangeTo(changeAddr).build()
      ctx.newProverBuilder().withDHTData(gY, gY, gXY, gXY, x).build().sign(unsigned) // alice signing bob's box. Does not work here but works in other cases.
    }
  }
}

