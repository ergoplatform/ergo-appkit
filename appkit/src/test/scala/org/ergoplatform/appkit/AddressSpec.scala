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

  property("Address fromMnemonic") {
    val mnemonic = "slow silly start wash bundle suffer bulb ancient height spin express remind today effort helmet"
    val addr = Address.fromMnemonic(NetworkType.TESTNET, mnemonic, "")
    addr.toString shouldBe addrStr
    val addr2 = Address.fromMnemonic(NetworkType.MAINNET, mnemonic, "")
    addr2.toString shouldNot be (addrStr)
  }

}
