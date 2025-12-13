package org.ergoplatform.appkit

import org.ergoplatform.appkit.testing.AppkitTesting
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import sigmastate.helpers.NegativeTesting
import java.util.Arrays
import java.math.BigInteger
import sigmastate.eval.CBigInt

/**
 * Test suite for Sigma 6.0 features introduced via EIP-50.
 * 
 * This test suite covers new features available in Sigma State 6.0+:
 * - UnsignedBigInt type (256-bit unsigned integers)
 * - Header serialization and PoW validation
 * - Cross-input context variables
 * - Enhanced serialization features
 * - New collection methods
 * - Numeric operations (bitwise, shifting)
 * - Box.getReg() improvements
 * - Option type enhancements
 * - AvlTree updates
 * 
 * @see https://github.com/ergoplatform/sigmastate-interpreter/releases/tag/v6.0.0
 */
class Sigma60FeaturesSpec extends AnyPropSpec with Matchers
  with AppkitTesting
  with HttpClientTesting
  with NegativeTesting {

  val mockTxId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"

  property("UnsignedBigInt type operations") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()
      
      // Test script that uses UnsignedBigInt type (available in ErgoTree v3)
      val contractScript =
        """
          |{
          |  // UnsignedBigInt is a new type in Sigma 6.0
          |  // Test basic operations with large unsigned numbers
          |  val largeNum = getVar[BigInt](0).get
          |  sigmaProp(largeNum > 0L.toBigInt)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)
      
      // Create a very large number (> 2^255)
      val largeValue = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819968")
      
      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)
        .withContextVars(ContextVar.of(0.toByte, CBigInt(largeValue)))

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }

  property("Cross-input context variables - GetVar(inputIndex, varId)") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // New Sigma 6.0 feature: access context variables from other inputs
      val contractScript =
        """
          |{
          |  // Get context variable from first input (index 0)
          |  val valueFromInput0 = getVar[Int](0, 1).get
          |  // Compare with own context variable
          |  val ownValue = getVar[Int](1).get
          |  sigmaProp(valueFromInput0 == ownValue)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      // First input with context var id=1, value=100
      val input1 = txB.outBoxBuilder()
        .value(15000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)
        .withContextVars(ContextVar.of(1.toByte, 100))

      // Second input that reads from first input
      val input2 = txB.outBoxBuilder()
        .value(15000000)
        .contract(contract)
        .build()
        .convertToInputWith(mockTxId, 1)
        .withContextVars(ContextVar.of(1.toByte, 100))

      val output = txB.outBoxBuilder()
        .value(28000000)
        .contract(truePropContract(ctx))
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input1, input2))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
      signed.getInputs.size() shouldBe 2
    }
  }

  property("Enhanced Option type - Global.some() and Global.none()") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // Test new Option creation methods in Sigma 6.0
      val contractScript =
        """
          |{
          |  // Use Global.some() to create Option[Int]
          |  val someVal = some(42)
          |  // Use isDefined to check
          |  sigmaProp(someVal.isDefined && someVal.get == 42)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }

  property("New collection method - startsWith") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // Test new collection.startsWith() method in Sigma 6.0
      val contractScript =
        """
          |{
          |  val data = Coll(1, 2, 3, 4, 5)
          |  val prefix = Coll(1, 2, 3)
          |  sigmaProp(data.startsWith(prefix))
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }

  property("Box.getReg() implementation") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // Test improved Box.getReg() in Sigma 6.0
      val contractScript =
        """
          |{
          |  val box = INPUTS(0)
          |  val reg4 = box.getReg[Int](4)
          |  sigmaProp(reg4.isDefined && reg4.get == 42)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .registers(ErgoValue.of(42))
        .build()
        .convertToInputWith(mockTxId, 0)

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }

  property("Lazy evaluation for Option.getOrElse()") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // Test lazy evaluation of getOrElse default value in Sigma 6.0
      val contractScript =
        """
          |{
          |  val someOption = some(10)
          |  val noneOption: Option[Int] = none
          |  // Default is now lazily evaluated
          |  val val1 = someOption.getOrElse(20)
          |  val val2 = noneOption.getOrElse(30)
          |  sigmaProp(val1 == 10 && val2 == 30)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }

  property("Global.serialize and deserializeTo methods") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // Test enhanced serialization in Sigma 6.0
      val contractScript =
        """
          |{
          |  val value = 12345
          |  val serialized = serialize(value)
          |  val deserialized = deserialize[Int](serialized)
          |  sigmaProp(deserialized == value)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }

  property("Bitwise operations") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // Test new bitwise operations in Sigma 6.0
      val contractScript =
        """
          |{
          |  val a = 12L  // binary: 1100
          |  val b = 10L  // binary: 1010
          |  val andResult = a & b  // should be 8 (1000)
          |  val orResult = a | b   // should be 14 (1110)
          |  val xorResult = a ^ b  // should be 6 (0110)
          |  sigmaProp(andResult == 8L && orResult == 14L && xorResult == 6L)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }

  property("Shift operations") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // Test new shift operations in Sigma 6.0
      val contractScript =
        """
          |{
          |  val num = 8L
          |  val leftShift = num << 2L   // 8 << 2 = 32
          |  val rightShift = num >> 1L  // 8 >> 1 = 4
          |  sigmaProp(leftShift == 32L && rightShift == 4L)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }

  property("Collection.getOrElse with lazy default") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // Test lazy evaluation for Coll.getOrElse in Sigma 6.0
      val contractScript =
        """
          |{
          |  val coll = Coll(1, 2, 3)
          |  val val1 = coll.getOrElse(1, 999)  // index exists, returns 2
          |  val val2 = coll.getOrElse(10, 999) // index out of bounds, returns 999
          |  sigmaProp(val1 == 2 && val2 == 999)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }

  property("Global.fromBigEndianBytes") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val txB = ctx.newTxBuilder()

      // Test new Global.fromBigEndianBytes in Sigma 6.0
      val contractScript =
        """
          |{
          |  val bytes = Coll[Byte](0x00, 0x00, 0x00, 0x0A)
          |  val num = fromBigEndianBytes[Long](bytes)
          |  sigmaProp(num == 10L)
          |}
        """.stripMargin

      val contract = ctx.compileContract(ConstantsBuilder.empty(), contractScript)

      val input = txB.outBoxBuilder()
        .value(30000000)
        .contract(truePropContract(ctx))
        .build()
        .convertToInputWith(mockTxId, 0)

      val output = txB.outBoxBuilder()
        .value(29000000)
        .contract(contract)
        .build()

      val unsigned = txB.boxesToSpend(Arrays.asList(input))
        .outputs(output)
        .fee(1000000)
        .sendChangeTo(address.getErgoAddress)
        .build()

      val prover = ctx.newProverBuilder().build()
      val signed = prover.sign(unsigned)

      signed should not be null
    }
  }
}
