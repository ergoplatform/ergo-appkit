package org.ergoplatform.appkit

import org.ergoplatform.ErgoBox
import org.ergoplatform.appkit.testing.AppkitTesting
import org.ergoplatform.sdk.ErgoToken
import org.ergoplatform.sdk.JavaHelpers
import org.ergoplatform.wallet.mnemonic.{Mnemonic => WMnemonic}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scorex.util.encode.Base16
import sigma.ast.{ByteArrayConstant, EvaluatedValue, IntConstant, SType}
import sigma.data.TrivialProp
import sigmastate.helpers.TestingHelpers._

import scala.collection.JavaConverters._

class JavaHelpersSpec extends AnyPropSpec with Matchers
    with ScalaCheckDrivenPropertyChecks
    with AppkitTesting {
  import ErgoBox._

  type Registers = Map[NonMandatoryRegisterId, _ <: EvaluatedValue[_ <: SType]]

  def boxWithRegs(regs: Registers) = {
    testBox(10, sigma.ast.ErgoTree.fromProposition(TrivialProp.TrueProp), 100, Nil, regs)
  }

  def check(regs: Registers, expRegs: IndexedSeq[ErgoValue[_]]) = {
    val box = boxWithRegs(regs)
    val res = AppkitHelpers.getBoxRegisters(box).asScala.toIndexedSeq
    res shouldBe expRegs
  }

  property("InputBox.getRegisters") {
    check(Map.empty, IndexedSeq.empty)
    check(Map(R4 -> IntConstant(10)), IndexedSeq(ErgoValue.of(10)))
    check(
      Map(
        R4 -> IntConstant(10),
        R5 -> ByteArrayConstant(Array[Byte](10, 20, 30))),
      IndexedSeq(
        ErgoValue.of(10), ErgoValue.of(Array[Byte](10, 20, 30))))

    an[IllegalArgumentException] shouldBe thrownBy {
      check(Map(R5 -> IntConstant(10)), IndexedSeq(ErgoValue.of(10)))
    }

    an[IllegalArgumentException] shouldBe thrownBy {
      check(
        Map(
          R4 -> IntConstant(10),
          R6 -> ByteArrayConstant(Array[Byte](10, 20, 30))),
        IndexedSeq(
          ErgoValue.of(10), ErgoValue.of(Array[Byte](10, 20, 30))))
    }
  }

  property("ContextVars roundtrip through ContextExtension") {
    val bytes = Array[Byte](1, 2, 3, 4)
    val vars = Seq(
      ContextVar.of(1.toByte, 42),
      ContextVar.of(2.toByte, 123456789L),
      ContextVar.of(3.toByte, bytes)
    )

    val extension = AppkitIso.isoContextVarsToContextExtension.to(vars.asJava)
    val roundtrip = AppkitIso.isoContextVarsToContextExtension.from(extension).asScala.toSeq.sortBy(_.getId)

    roundtrip.map(_.getId) shouldBe vars.map(_.getId)
    roundtrip(0).getValue shouldBe ErgoValue.of(42)
    roundtrip(1).getValue shouldBe ErgoValue.of(123456789L)
    roundtrip(2).getValue.toHex shouldBe ErgoValue.of(bytes).toHex
  }

  property("ContextVars reject duplicate ids before creating ContextExtension") {
    val ex = intercept[RuntimeException] {
      AppkitIso.isoContextVarsToContextExtension.to(Seq(
        ContextVar.of(1.toByte, 1),
        ContextVar.of(1.toByte, 2)
      ).asJava)
    }
    ex.getMessage should include("Duplicate variable id")
  }

  property("createBoxCandidate preserves token ids and register values") {
    val token1 = new ErgoToken("01" * 32, 7L)
    val token2 = new ErgoToken("02" * 32, 11L)
    val regBytes = Array[Byte](10, 20, 30)
    val registers = Seq(ErgoValue.of(10), ErgoValue.of(regBytes))

    val box = AppkitHelpers.createBoxCandidate(
      1000000L,
      sigma.ast.ErgoTree.fromProposition(TrivialProp.TrueProp),
      Seq(token1, token2),
      registers,
      100)

    val tokens = box.additionalTokens.toArray.map { case (id, value) =>
      new ErgoToken(Base16.encode(id.toArray), value)
    }
    tokens.toSeq shouldBe Seq(token1, token2)

    val roundtripRegisters = AppkitHelpers.getBoxRegisters(box).asScala.toSeq
    roundtripRegisters.head shouldBe registers.head
    roundtripRegisters(1).toHex shouldBe registers(1).toHex
  }

  property("mnemonicToSeed") {
    // check that bouncycastle-based implementation is equivalent to the
    // original Java8-based implementation
    forAll(MinSuccessful(50)) { (mnemonic: String, passOpt: Option[String]) =>
      val seed = JavaHelpers.mnemonicToSeed(mnemonic, passOpt)
      val expSeed = WMnemonic.toSeed(
        org.ergoplatform.sdk.SecretString.create(mnemonic),
        passOpt.map(a => org.ergoplatform.sdk.SecretString.create(a)))
      seed shouldBe expSeed
    }
  }

}
