package org.ergoplatform.appkit

import org.ergoplatform.appkit.testing.AppkitTesting
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class DataInputsSpec extends PropSpec with Matchers
  with ScalaCheckDrivenPropertyChecks
  with AppkitTesting
  with HttpClientTesting {

  val expectedDataInputScript = "{sigmaProp(false)}"

  val incorrectDataInputScript = "{sigmaProp(true)}"

  val dummyTxId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"

  val inputBoxScript = "{sigmaProp(CONTEXT.dataInputs(0).propositionBytes == expectedDataInputPropBytes)}"

  // below is ergoTree of a random box picked from the block explorer. The boxId is 02abc29b6a28ccf7e9620afa16e1067caeb75fcd2e62c066e190742962cdcbae
  // We just need valid ergoTree to construct the change address
  val dummyErgoTree = "100207036ba5cfbc03ea2471fdf02737f64dbcd58c34461a7ec1e586dcd713dacbf89a120400d805d601db6a01ddd6027300d603b2a5730100d604e4c672030407d605e4c672030507eb02ce7201720272047205ce7201720472027205"

  property("Fail with no DataInputs") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))

    ergoClient.execute { ctx: BlockchainContext =>

      val dataInputContract: ErgoContract = ctx.compileContract(ConstantsBuilder.empty(), expectedDataInputScript);

      val dataInputPropBytes: Array[Byte] = dataInputContract.getErgoTree.bytes

      val input = ctx.newTxBuilder.outBoxBuilder.value(20000000).contract(ctx.compileContract(
        ConstantsBuilder.create().item("expectedDataInputPropBytes", dataInputPropBytes).build(), inputBoxScript
      )).build().convertToInputWith(dummyTxId, 0)

      val txB = ctx.newTxBuilder()

      val dummyOutput = txB.outBoxBuilder()
        .value(10000000)
        .contract(truePropContract(ctx)).build()

      val inputs = new java.util.ArrayList[InputBox]()

      inputs.add(input)

      val ergoTree = JavaHelpers.decodeStringToErgoTree(dummyErgoTree)
      val changeAddr = Address.fromErgoTree(ergoTree, NetworkType.MAINNET).getErgoAddress

      val unsigned = txB.boxesToSpend(inputs).outputs(dummyOutput).fee(10000000).sendChangeTo(changeAddr).build()

      an[Exception] shouldBe thrownBy {
        ctx.newProverBuilder().build().sign(unsigned)
      }
    }
  }

  property("Fail with wrong DataInputs") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))

    ergoClient.execute { ctx: BlockchainContext =>

      val dataInputContract: ErgoContract = ctx.compileContract(ConstantsBuilder.empty(), expectedDataInputScript);

      val dataInputPropBytes: Array[Byte] = dataInputContract.getErgoTree.bytes

      val dataInput = ctx.newTxBuilder().outBoxBuilder.value(10000000000000L).contract(ctx.compileContract(ConstantsBuilder.empty(), incorrectDataInputScript)).build().convertToInputWith(dummyTxId, 0)

      val input = ctx.newTxBuilder.outBoxBuilder.value(20000000).contract(ctx.compileContract(
        ConstantsBuilder.create().item("expectedDataInputPropBytes", dataInputPropBytes).build(), inputBoxScript
      )).build().convertToInputWith(dummyTxId, 0)

      val txB = ctx.newTxBuilder()

      val dummyOutput = txB.outBoxBuilder()
        .value(10000000)
        .contract(truePropContract(ctx)).build()

      val inputs = new java.util.ArrayList[InputBox]()
      val dataInputs = new java.util.ArrayList[InputBox]()

      inputs.add(input)
      dataInputs.add(dataInput)

      val ergoTree = JavaHelpers.decodeStringToErgoTree(dummyErgoTree)
      val changeAddr = Address.fromErgoTree(ergoTree, NetworkType.MAINNET).getErgoAddress

      val unsigned = txB.boxesToSpend(inputs).outputs(dummyOutput).withDataInputs(dataInputs).fee(10000000).sendChangeTo(changeAddr).build()

      an[Exception] shouldBe thrownBy {
        ctx.newProverBuilder().build().sign(unsigned)
      }
    }
  }

  property("Pass with correct DataInputs") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))

    ergoClient.execute { ctx: BlockchainContext =>

      val dataInputContract: ErgoContract = ctx.compileContract(ConstantsBuilder.empty(), expectedDataInputScript);

      val dataInputPropBytes: Array[Byte] = dataInputContract.getErgoTree.bytes

      val dataInput = ctx.newTxBuilder().outBoxBuilder.value(10000000000000L).contract(ctx.compileContract(ConstantsBuilder.empty(), expectedDataInputScript)).build().convertToInputWith(dummyTxId, 0)

      val input = ctx.newTxBuilder.outBoxBuilder.value(20000000).contract(ctx.compileContract(
        ConstantsBuilder.create().item("expectedDataInputPropBytes", dataInputPropBytes).build(), inputBoxScript
      )).build().convertToInputWith(dummyTxId, 0)

      val txB = ctx.newTxBuilder()

      val dummyOutput = txB.outBoxBuilder()
        .value(10000000)
        .contract(truePropContract(ctx)).build()

      val inputs = new java.util.ArrayList[InputBox]()
      val dataInputs = new java.util.ArrayList[InputBox]()

      inputs.add(input)
      dataInputs.add(dataInput)

      val ergoTree = JavaHelpers.decodeStringToErgoTree(dummyErgoTree)
      val changeAddr = Address.fromErgoTree(ergoTree, NetworkType.MAINNET).getErgoAddress

      val unsigned = txB.boxesToSpend(inputs).outputs(dummyOutput).withDataInputs(dataInputs).fee(10000000).sendChangeTo(changeAddr).build()

      ctx.newProverBuilder().build().sign(unsigned)
    }
  }
}

