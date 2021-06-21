package org.ergoplatform.appkit

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}
import org.ergoplatform.wallet.mnemonic.{Mnemonic => WMnemonic}

class JavaHelpersSpec extends PropSpec with Matchers
  with ScalaCheckDrivenPropertyChecks
  with AppkitTestingCommon {

  property("mnemonicToSeed") {
    // check that bouncycastle-based implementation is equivalent to the
    // original Java8-based implementation
    forAll(MinSuccessful(50)) { (mnemonic: String, passOpt: Option[String]) =>
      val seed = JavaHelpers.mnemonicToSeed(mnemonic, passOpt)
      val expSeed = WMnemonic.toSeed(mnemonic, passOpt)
      seed shouldBe expSeed
      println(s"Mnemonic: $mnemonic, Password: $passOpt")
    }
  }
}
