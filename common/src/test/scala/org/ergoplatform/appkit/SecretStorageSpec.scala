package org.ergoplatform.appkit

import java.io.File
import com.google.common.io.Files
import org.ergoplatform.sdk.JavaHelpers
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class SecretStorageSpec extends AnyPropSpec with Matchers with ScalaCheckDrivenPropertyChecks
    with AppkitTestingCommon {
  val mnemonicWithPassword = Mnemonic.create("phrase".toCharArray, "mnemonic pass".toCharArray)
  val encryptionPass = "encryption pass"

  property("create from mnemonic") {
    withNewStorageFor(mnemonicWithPassword, encryptionPass) { storage =>
      storage.isLocked shouldBe true
      storage.getFile().exists() shouldBe true
    }
  }

  property("unlocked by password with ") {
    withNewStorageFor(mnemonicWithPassword, encryptionPass) { storage =>
      storage.unlock(encryptionPass)
      storage.isLocked shouldBe false
      val addr = Address.fromMnemonic(NetworkType.TESTNET, mnemonicWithPassword, false)
      val secret = storage.getSecret()
      secret should not be(null)
      val expSecret = JavaHelpers.seedToMasterKey(mnemonicWithPassword.getPhrase, mnemonicWithPassword.getPassword, false)
      expSecret shouldBe secret
      storage.getAddressFor(NetworkType.TESTNET) shouldBe addr
    }
  }

  property("not unlock by wrong password") {
    a[RuntimeException] shouldBe thrownBy {
      withNewStorageFor(mnemonicWithPassword, encryptionPass) { storage =>
        storage.unlock("wrong password")
      }
    }
  }

  property("load from file") {
    withNewStorageFor(mnemonicWithPassword, encryptionPass) { storage =>
      val fileName = storage.getFile.getPath
      val loaded = SecretStorage.loadFrom(fileName)
      loaded.isLocked shouldBe true
      loaded.unlock(encryptionPass)
      loaded.isLocked shouldBe false

      // compare created and loaded storages
      storage.unlock(encryptionPass)
      storage.getSecret shouldBe loaded.getSecret
      storage.getAddressFor(NetworkType.TESTNET) shouldBe loaded.getAddressFor(NetworkType.TESTNET)
    }
  }

  def withNewStorageFor(mnemonic: Mnemonic, encryptionPass: String)(block: SecretStorage => Unit): Unit = {
    withTempDir { dir =>
      val dirPath = dir.getPath
      val storage = SecretStorage.createFromMnemonicIn(dirPath, mnemonic, encryptionPass, false)
      try {
        block(storage)
      }
      finally {
        storage.getFile.delete() shouldBe true
      }
    }
  }

  def withTempDir(block: File => Unit): Unit = {
    val dir = Files.createTempDir()
    try {
      block(dir)
    }
    finally {
      dir.delete() shouldBe true
    }
  }
}
