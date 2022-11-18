package org.ergoplatform.appkit

import java.util
import org.ergoplatform.appkit.Mnemonic._
import org.ergoplatform.appkit.MnemonicValidationException.{MnemonicWrongListSizeException, MnemonicChecksumException, MnemonicWordException, MnemonicEmptyException}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.collection.JavaConversions.seqAsJavaList

class MnemonicSpec extends AnyPropSpec with Matchers with ScalaCheckPropertyChecks {

  val testMnemonic = "walnut endorse maid alone fuel jump torch company ahead nice abstract earth pig spice reduce"
  val testEntropy: Array[Byte] = Array[Byte](-10, -55, 58, 24, 3, 117, -34, -15, -7, 81, 116, 5, 50, -84, 3, -94, -70, 73, -93, 45)

  property("generate") {
    val mnemonic = Mnemonic.generate(LANGUAGE_ID_ENGLISH, DEFAULT_STRENGTH, testEntropy)
    mnemonic shouldBe testMnemonic
  }

  property("entropy") {
    val entropy = getEntropy(DEFAULT_STRENGTH)
    entropy.length shouldBe 20
  }

  property("checkMnemonic") {
    val entropy = Mnemonic.toEntropy(LANGUAGE_ID_ENGLISH, seqAsJavaList(testMnemonic.split(' ')))
    entropy shouldBe testEntropy

    an[MnemonicWordException] should be thrownBy Mnemonic.checkEnglishMnemonic(
      seqAsJavaList("wanut endorse maid alone fuel jump torch company ahead nice abstract earth pig spice reduce".split(' ')))
    an[MnemonicWrongListSizeException] should be thrownBy Mnemonic.checkEnglishMnemonic(
      seqAsJavaList("walnut endorse maid alone fuel jump torch company ahead nice abstract earth pig spice".split(' ')))
    an[MnemonicChecksumException] should be thrownBy Mnemonic.checkEnglishMnemonic(
      seqAsJavaList("walnut endorse maid alone fuel jump torch company ahead nice abstract earth pig spice earth".split(' ')))
    an[MnemonicEmptyException] should be thrownBy Mnemonic.checkEnglishMnemonic(new util.ArrayList[String]())
  }

  property("serializeExtendedPublicKey") {
    val masterKey = JavaHelpers.seedToMasterKey(SecretString.create("lens stadium egg cage hollow noble gate belt impulse vicious middle endless angry buzz crack"), SecretString.empty(), false)

    Bip32Serialization.serializeExtendedPublicKeyToHex(masterKey, NetworkType.MAINNET) shouldBe "0488b21e04220c2217000000009216e49a70865823eff5381d6fd33ac96743af1f3051dc4cc8edd66a29a740860326cfc301b0c8d4d815ac721e0551304417e6133c2c9137f9f22c33895a3e1650"
  }
}

