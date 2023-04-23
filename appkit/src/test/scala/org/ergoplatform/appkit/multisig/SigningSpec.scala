package org.ergoplatform.appkit.multisig

import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.matchers.should.Matchers
import org.ergoplatform.appkit.testing.AppkitTesting

class SigningSpec extends AnyPropSpec with Matchers with ScalaCheckDrivenPropertyChecks
  with AppkitTesting {

  property("Signing workflow") {
    val server = new SigningServer
  }
}
