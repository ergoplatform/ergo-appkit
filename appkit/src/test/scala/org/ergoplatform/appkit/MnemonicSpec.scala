package org.ergoplatform.appkit

import java.util
import org.ergoplatform.appkit.Mnemonic._
import org.ergoplatform.appkit.MnemonicValidationException.{MnemonicWrongListSizeException, MnemonicChecksumException, MnemonicWordException, MnemonicEmptyException}
import org.ergoplatform.sdk.{JavaHelpers, SecretString}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.collection.JavaConverters._

class MnemonicSpec extends AnyPropSpec with Matchers with ScalaCheckDrivenPropertyChecks {

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
    val entropy = Mnemonic.toEntropy(LANGUAGE_ID_ENGLISH, testMnemonic.split(' ').toSeq.asJava)
    entropy shouldBe testEntropy

    an[MnemonicWordException] should be thrownBy Mnemonic.checkEnglishMnemonic(
      "wanut endorse maid alone fuel jump torch company ahead nice abstract earth pig spice reduce".split(' ').toSeq.asJava)
    an[MnemonicWrongListSizeException] should be thrownBy Mnemonic.checkEnglishMnemonic(
      "walnut endorse maid alone fuel jump torch company ahead nice abstract earth pig spice".split(' ').toSeq.asJava)
    an[MnemonicChecksumException] should be thrownBy Mnemonic.checkEnglishMnemonic(
      "walnut endorse maid alone fuel jump torch company ahead nice abstract earth pig spice earth".split(' ').toSeq.asJava)
    an[MnemonicEmptyException] should be thrownBy Mnemonic.checkEnglishMnemonic(new util.ArrayList[String]())
  }

  property("serializeExtendedPublicKey") {
    val masterKey = JavaHelpers.seedToMasterKey(SecretString.create("lens stadium egg cage hollow noble gate belt impulse vicious middle endless angry buzz crack"), SecretString.empty(), false)

    Bip32Serialization.serializeExtendedPublicKeyToHex(masterKey, NetworkType.MAINNET) shouldBe "0488b21e04220c2217000000009216e49a70865823eff5381d6fd33ac96743af1f3051dc4cc8edd66a29a740860326cfc301b0c8d4d815ac721e0551304417e6133c2c9137f9f22c33895a3e1650"
  }

  property("toEntropy non-English wordlists") {
    // Valid 12-word BIP39 phrases with known entropy, one per non-English
    // wordlist whose file order is not code-point sorted order. Each phrase
    // contains a word that binary search cannot find in the file-ordered list
    // (e.g. french index 756), so lookup must not assume a sorted wordlist.
    val cases = Seq(
      ("french", Seq("lexique", "hypnose", "gestuel", "cloche", "axial", "viseur", "période", "piquer", "nébuleux", "évoquer", "chasseur", "vernir"), Array[Byte](-115, 79, -75, -57, 25, -47, 121, -8, 45, -75, -50, -93, 75, -48, -71, -4)),
      ("spanish", Seq("familia", "jugador", "abeja", "lámpara", "títere", "lógica", "choque", "furia", "venta", "proceso", "premio", "iglesia"), Array[Byte](85, 78, -28, 1, 61, 62, -113, 6, -116, 42, -3, -9, -9, 114, -23, 56)),
      ("japanese", Seq("あきる", "ないす", "みえる", "にんむ", "ざつおん", "れんぞく", "しゃいん", "おっと", "いたりあ", "くめる", "すいとう", "ばあい"), Array[Byte](0, -75, 119, -114, 90, -107, -73, -7, 90, 9, 33, 11, 39, -47, -73, 95)),
      ("chinese_simplified", Seq("奶", "弟", "垂", "范", "泽", "成", "汁", "朵", "芳", "涂", "罗", "司"), Array[Byte](-78, -47, -39, -61, 37, -88, 4, 8, 123, 30, -60, -98, -71, 9, 101, 36)),
      ("chinese_traditional", Seq("和", "日", "澤", "觀", "政", "醫", "脆", "順", "玉", "滅", "平", "要"), Array[Byte](0, -31, -34, 1, 23, -96, -22, -59, -70, 91, 95, 119, 47, -112, 62, 1)),
    )
    for ((languageId, words, expectedEntropy) <- cases) {
      Mnemonic.toEntropy(languageId, words.asJava) shouldBe expectedEntropy
    }
  }
}
