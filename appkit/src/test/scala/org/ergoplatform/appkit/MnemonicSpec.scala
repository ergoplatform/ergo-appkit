package org.ergoplatform.appkit

import org.ergoplatform.appkit.sandbox.Mnemonic
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}

class MnemonicSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks {
//  val addrStr = "3WzR39tWQ5cxxWWX6ys7wNdJKLijPeyaKgx72uqg9FJRBCdZPovL"
  import MnemonicSpec._

  property("generate") {
    val entropy = Array[Byte](-10, -55, 58, 24, 3, 117, -34, -15, -7, 81, 116, 5, 50, -84, 3, -94, -70, 73, -93, 45)
    val mnemonic = Mnemonic.generate("english", defaultStrength, entropy)
    mnemonic shouldBe "walnut endorse maid alone fuel jump torch company ahead nice abstract earth pig spice reduce"
  }

  property("entropy") {
    val entropy = getEntropy(defaultStrength)
    entropy.length shouldBe 20
  }
}

object MnemonicSpec extends App {
  val defaultStrength = 160
  def getEntropy(strength: Int) = scorex.utils.Random.randomBytes(strength / 8)
  val m = Mnemonic.generate("english", defaultStrength, getEntropy(defaultStrength))
  println(m)
}
