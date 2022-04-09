package org.ergoplatform.appkit.impl

import org.scalatest.{Matchers, PropSpec}

class AgeUsdBankTest extends PropSpec with Matchers {
  property("Age USD calculation tests") {
    val ageUsdBank = new AgeUsdBank(210526315, 160402193, 1375438973, 1477201069508651L)

    ageUsdBank.getStableCoinPrice shouldBe 2105263
    ageUsdBank.getReserveCoinPrice shouldBe 828471
    ageUsdBank.getCurrentReserveRatio shouldBe 437
  }
}
