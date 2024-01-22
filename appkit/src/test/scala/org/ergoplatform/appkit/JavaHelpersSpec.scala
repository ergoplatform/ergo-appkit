package org.ergoplatform.appkit

import org.ergoplatform.ErgoBox
import org.ergoplatform.appkit.testing.AppkitTesting
import org.ergoplatform.sdk.JavaHelpers
import org.ergoplatform.sdk.JavaHelpers.UniversalConverter
import org.ergoplatform.wallet.mnemonic.{Mnemonic => WMnemonic}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import sigma.ast.{EvaluatedValue, ByteArrayConstant, SType, IntConstant, ErgoTree}
import sigma.data.TrivialProp
import sigmastate.helpers.TestingHelpers._
import org.ergoplatform.sdk.SdkIsos._

class JavaHelpersSpec extends AnyPropSpec with Matchers
    with ScalaCheckDrivenPropertyChecks
    with AppkitTesting {
  import ErgoBox._

  type Registers = Map[NonMandatoryRegisterId, _ <: EvaluatedValue[_ <: SType]]

  def boxWithRegs(regs: Registers) = {
    testBox(10, ErgoTree.fromSigmaBoolean(TrivialProp.TrueProp), 100, Nil, regs)
  }

  def check(regs: Registers, expRegs: IndexedSeq[ErgoValue[_]]) = {
    val box = boxWithRegs(regs)
    val res = AppkitHelpers.getBoxRegisters(box).convertTo[IndexedSeq[ErgoValue[_]]]
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

  property("mnemonicToSeed") {
    // check that bouncycastle-based implementation is equivalent to the
    // original Java8-based implementation
    forAll(MinSuccessful(50)) { (mnemonic: String, passOpt: Option[String]) =>
      val seed = JavaHelpers.mnemonicToSeed(mnemonic, passOpt)
      val expSeed = WMnemonic.toSeed(org.ergoplatform.wallet.interface4j.SecretString.create(mnemonic), passOpt.map(a => org.ergoplatform.wallet.interface4j.SecretString.create(a)))
      seed shouldBe expSeed
      println(s"Mnemonic: $mnemonic, Password: $passOpt")
    }
  }

}
