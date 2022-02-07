package org.ergoplatform.appkit

import org.ergoplatform.appkit.testing.AppkitTesting
import org.ergoplatform.wallet.secrets.{ExtendedPublicKey, ExtendedSecretKey}
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class Bip32SerializationSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks
  with AppkitTesting {

  property("Serialization roundtrip") {
    val masterKey = JavaHelpers.seedToMasterKey(mnemonic, SecretString.empty())
    val xpubString = Bip32Serialization.serializeExtendedPublicKeyToHex(masterKey, NetworkType.MAINNET)

    an[IllegalArgumentException] shouldBe thrownBy {
      Bip32Serialization.parseExtendedPublicKeyFromHex(xpubString, NetworkType.TESTNET)
    }

    val eip3ParentKeyDeserialized = Bip32Serialization.parseExtendedPublicKeyFromHex(xpubString, NetworkType.MAINNET)

    val firstEip3Addr = Address.createEip3Address(0, NetworkType.MAINNET, eip3ParentKeyDeserialized)
    firstEip3Addr.toString shouldBe firstEip3AddrStr

    val eip3ParentKeyDerivedFromMaster = masterKey.derive(JavaHelpers.eip3DerivationParent()).publicKey

    eip3ParentKeyDerivedFromMaster shouldBe eip3ParentKeyDeserialized
  }

}
