package org.ergoplatform.appkit

import org.ergoplatform.appkit.testing.AppkitTesting
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class Bip32SerializationSpec extends AnyPropSpec with Matchers with ScalaCheckPropertyChecks
  with AppkitTesting {

  property("Serialization roundtrip") {
    val masterKey = JavaHelpers.seedToMasterKey(mnemonic, SecretString.empty(), false)
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
