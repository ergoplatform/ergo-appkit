package org.ergoplatform.appkit

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}

class AddressSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  val addrStr = "3WzR39tWQ5cxxWWX6ys7wNdJKLijPeyaKgx72uqg9FJRBCdZPovL"

  property("encoding vector") {
    val addr = Address.create(addrStr)
    addr.isMainnet shouldBe false
    addr.isP2PK shouldBe true
    addr.toString shouldBe addrStr
  }
}
