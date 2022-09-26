package org.ergoplatform.appkit

import org.ergoplatform.appkit.impl.{AgeUsdBank, AgeUsdExchangeTransactionBuilder}
import org.ergoplatform.appkit.testing.AppkitTesting
import org.scalatest.{Matchers, PropSpec}

class AgeUsdBankSpec extends PropSpec with Matchers
  with AppkitTesting
  with HttpClientTesting {

  property("Age USD calculation tests") {
    val ageUsdBank = new AgeUsdBank(210526315, 160402193, 1375438973, 1477201069508651L)

    ageUsdBank.getStableCoinPrice shouldBe 2105263
    ageUsdBank.getReserveCoinPrice shouldBe 828471
    ageUsdBank.getCurrentReserveRatio shouldBe 437

    val scAvailable = ageUsdBank.getStableCoinAmountAvailable
    ageUsdBank.canExchangeStableCoin(scAvailable) shouldBe true
    ageUsdBank.canExchangeStableCoin(scAvailable * 2) shouldBe false

    val rcAvailable = ageUsdBank.getReserveCoinAmountAvailable
    ageUsdBank.canExchangeReserveCoin(rcAvailable) shouldBe true
    ageUsdBank.canExchangeReserveCoin(rcAvailable * 2) shouldBe false
    val rcRedeemable = ageUsdBank.getReserveCoinAmountRedeemable
    ageUsdBank.canExchangeReserveCoin(-rcRedeemable) shouldBe true
    ageUsdBank.canExchangeReserveCoin(-(rcRedeemable * 2)) shouldBe false

    val ageUsdBank2 = new AgeUsdBank(229357798, 155058786, 1361641506, 1454615342036303L)
    ageUsdBank2.getStableCoinPrice shouldBe 2293577
    ageUsdBank2.getReserveCoinPrice shouldBe 807096
    ageUsdBank2.getCurrentReserveRatio shouldBe 409
    val scAvailable2 = ageUsdBank2.getStableCoinAmountAvailable
    ageUsdBank2.canExchangeStableCoin(scAvailable2) shouldBe true
    ageUsdBank2.canExchangeStableCoin(scAvailable2 * 2) shouldBe false
    val rcAvailable2 = ageUsdBank2.getReserveCoinAmountAvailable
    ageUsdBank2.canExchangeReserveCoin(rcAvailable2) shouldBe true
    ageUsdBank2.canExchangeReserveCoin(rcAvailable2 * 2) shouldBe(false)
    val rcRedeemable2 = ageUsdBank2.getReserveCoinAmountRedeemable
    ageUsdBank2.canExchangeReserveCoin(-rcRedeemable2) shouldBe true
    ageUsdBank2.canExchangeReserveCoin(-(rcRedeemable2 * 2)) shouldBe false

    val ageUsdBankLow = new AgeUsdBank(229357798, 155058786, 1361641506, 145461534203630L)
    ageUsdBankLow.getStableCoinPrice shouldBe 938105 // lower price due to not enough reserve
    ageUsdBankLow.getReserveCoinPrice shouldBe AgeUsdBank.RC_DEFAULT_PRICE
    ageUsdBankLow.getStableCoinAmountAvailable shouldBe 0
    ageUsdBankLow.getReserveCoinAmountRedeemable shouldBe 0

    val ageUsdBankHigh = new AgeUsdBank(229357798, 155058786, 1361641506, 14546153420363000L)
    ageUsdBankHigh.getStableCoinPrice shouldBe 2293577
    ageUsdBankHigh.getReserveCoinPrice shouldBe 10421622
    ageUsdBankHigh.getReserveCoinAmountAvailable shouldBe 0
  }

  property("Age USD exchange tx tests") {
    // Withdraw from bank
    val ageUsdBank = new AgeUsdBank(230946882, 155871738, 1363943769, 1458253206733705L)
    ageUsdBank.getCurrentReserveRatio shouldBe 405
    ageUsdBank.getStableCoinPrice shouldBe 2309468

    val scAvailable = ageUsdBank.getStableCoinAmountAvailable
    ageUsdBank.canExchangeStableCoin(scAvailable) shouldBe true

    val rcAvailable = ageUsdBank.getReserveCoinAmountAvailable
    ageUsdBank.canExchangeReserveCoin(rcAvailable) shouldBe true
    ageUsdBank.canExchangeReserveCoin(-ageUsdBank.getReserveCoinAmountRedeemable) shouldBe true

    // redeem stable coin
    val unsignedTx = testAgeUsdTransaction(ageUsdBank, { txBuilder: AgeUsdExchangeTransactionBuilder =>
      txBuilder.buildStableCoinExchangeTransaction(-100)
    })
    unsignedTx.getOutputs.size() shouldBe 4

    // mint stable coin
    testAgeUsdTransaction(ageUsdBank, { txBuilder: AgeUsdExchangeTransactionBuilder =>
      txBuilder.buildStableCoinExchangeTransaction(ageUsdBank.getStableCoinAmountAvailable)
    })

    // redeem reserve coin
    testAgeUsdTransaction(ageUsdBank, { txBuilder: AgeUsdExchangeTransactionBuilder =>
      txBuilder.buildReserveCoinExchangeTransaction(-ageUsdBank.getReserveCoinAmountRedeemable)
    })

    // mint reserve coin
    testAgeUsdTransaction(ageUsdBank, { txBuilder: AgeUsdExchangeTransactionBuilder =>
      txBuilder.buildReserveCoinExchangeTransaction(ageUsdBank.getReserveCoinAmountAvailable)
    })

    // use additional fee for redeeming
    val additionalFeeTx = testAgeUsdTransaction(ageUsdBank, { txBuilder: AgeUsdExchangeTransactionBuilder =>
      txBuilder.withAdditionalFee(address, 1000L * 1000L * 100L).buildReserveCoinExchangeTransaction(1000)
    })
    additionalFeeTx.getOutputs.size() shouldBe 5

    // use additional fee for minting
    testAgeUsdTransaction(ageUsdBank, { txBuilder: AgeUsdExchangeTransactionBuilder =>
      txBuilder.withAdditionalFee(address, 1000L * 1000L * 100L).buildReserveCoinExchangeTransaction(-1000)
    })
  }

  private def testAgeUsdTransaction(ageUsdBank: AgeUsdBank, action: AgeUsdExchangeTransactionBuilder => UnsignedTransaction): UnsignedTransaction = {
    val data = MockData(
      Seq(
        loadNodeResponse("ageusd/response_Box1.json"),
        loadNodeResponse("ageusd/response_bankbox.json"),
        loadNodeResponse("ageusd/response_ratebox.json")
      ),
      Seq(
        loadExplorerResponse("ageusd/response_boxesByAddressUnspent.json"),
        loadExplorerResponse("ageusd/bankboxbytoken.json"),
        loadExplorerResponse("ageusd/rateboxtoken.json")
      ))

    val ergoClient = createMockedErgoClient(data)
    val storage = SecretStorage.loadFrom("storage/E2.json")
    storage.unlock("abc")

    val unsignedTransaction = ergoClient.execute { ctx: BlockchainContext =>
      action(ageUsdBank.getExchangeTransactionBuilder(BoxOperations.createForSender(storage.getAddressFor(NetworkType.MAINNET), ctx)
        .withFeeAmount(Parameters.MinFee * 5)))
    }

    unsignedTransaction.getDataInputs.size() shouldBe 1
    checkSmartContractRun(unsignedTransaction) shouldBe true
    ergoClient.execute { ctx: BlockchainContext =>
      ctx.newProverBuilder().withSecretStorage(storage).build().sign(unsignedTransaction)
    }

    unsignedTransaction
  }

  def checkSmartContractRun(unsignedTransaction: UnsignedTransaction): Boolean = {
    // this is the code for EIP-15, see https://github.com/ergoplatform/eips/blob/master/eip-0015.md
    // it is not necessary to have this here as script signing above will reduce the ErgoTree
    // and test. However, with having this here debugging is possible

    val rateBox = unsignedTransaction.getDataInputs.get(0)
    val bankBoxIn = unsignedTransaction.getInputs.get(0)
    val bankBoxOut = unsignedTransaction.getOutputs.get(0)
    val receiptBox = unsignedTransaction.getOutputs.get(1)
    val rate = rateBox.getRegisters.get(0).getValue.asInstanceOf[Long] / 100

    val scCircIn = bankBoxIn.getRegisters.get(0).getValue.asInstanceOf[Long]
    val rcCircIn = bankBoxIn.getRegisters.get(1).getValue.asInstanceOf[Long]
    val bcReserveIn = bankBoxIn.getValue

    val scTokensIn = bankBoxIn.getTokens.get(0).getValue
    val rcTokensIn = bankBoxIn.getTokens.get(1).getValue

    val scCircOut = bankBoxOut.getRegisters.get(0).getValue.asInstanceOf[Long]
    val rcCircOut = bankBoxOut.getRegisters.get(1).getValue.asInstanceOf[Long]
    val bcReserveOut = bankBoxOut.getValue

    val scTokensOut = bankBoxOut.getTokens.get(0).getValue
    val rcTokensOut = bankBoxOut.getTokens.get(1).getValue

    val totalScIn = scTokensIn + scCircIn
    val totalScOut = scTokensOut + scCircOut

    val totalRcIn = rcTokensIn + rcCircIn
    val totalRcOut = rcTokensOut + rcCircOut

    val rcExchange = rcTokensIn != rcTokensOut
    val scExchange = scTokensIn != scTokensOut

    val rcExchangeXorScExchange = (rcExchange || scExchange) && !(rcExchange && scExchange)

    val circDelta = receiptBox.getRegisters.get(0).getValue.asInstanceOf[Long]
    val bcReserveDelta = receiptBox.getRegisters.get(1).getValue.asInstanceOf[Long]

    val rcCircDelta = if (rcExchange) circDelta else 0L
    val scCircDelta = if (rcExchange) 0L else circDelta

    val validDeltas = (scCircIn + scCircDelta == scCircOut) &&
      (rcCircIn + rcCircDelta == rcCircOut) &&
      (bcReserveIn + bcReserveDelta == bcReserveOut) &&
      scCircOut >= 0 && rcCircOut >= 0

    val coinsConserved = totalRcIn == totalRcOut && totalScIn == totalScOut

    val tokenIdsConserved = bankBoxOut.getTokens.get(0).getId == bankBoxIn.getTokens.get(0).getId &&
      bankBoxOut.getTokens.get(1).getId == bankBoxIn.getTokens.get(1).getId &&
      bankBoxOut.getTokens.get(2).getId == bankBoxIn.getTokens.get(2).getId

    val mandatoryRateConditions = true // simplified
    val mandatoryBankConditions = bankBoxOut.getValue >= 0 &&
      bankBoxOut.getErgoTree == bankBoxIn.getErgoTree &&
      rcExchangeXorScExchange &&
      coinsConserved &&
      validDeltas &&
      tokenIdsConserved

    // exchange equations
    val bcReserveNeededOut = scCircOut * rate
    val bcReserveNeededIn = scCircIn * rate
    val liabilitiesIn = Math.max(Math.min(bcReserveIn, bcReserveNeededIn), 0)
    val maxReserveRatioPercent = AgeUsdBank.MAX_RESERVE_RATIO_PERCENT

    val reserveRatioPercentOut = if (bcReserveNeededOut == 0) maxReserveRatioPercent else bcReserveOut * 100 / bcReserveNeededOut

    val validReserveRatio = if (scExchange) {
      if (scCircDelta > 0) {
        reserveRatioPercentOut >= AgeUsdBank.MIN_RESERVE_RATIO_PERCENT
      } else true
    } else {
      if (rcCircDelta > 0) {
        reserveRatioPercentOut <= maxReserveRatioPercent
      } else {
        reserveRatioPercentOut >= AgeUsdBank.MIN_RESERVE_RATIO_PERCENT
      }
    }

    val brDeltaExpected = if (scExchange) { // sc
      val liableRate = if (scCircIn == 0) Long.MaxValue else liabilitiesIn / scCircIn
      val scNominalPrice = Math.min(rate, liableRate)
      scNominalPrice * scCircDelta
    } else { // rc
      val equityIn = bcReserveIn - liabilitiesIn
      val equityRate = if (rcCircIn == 0) AgeUsdBank.RC_DEFAULT_PRICE else equityIn / rcCircIn
      val rcNominalPrice = if (equityIn == 0) AgeUsdBank.RC_DEFAULT_PRICE else equityRate
      rcNominalPrice * rcCircDelta
    }

    val fee = brDeltaExpected * AgeUsdBank.FEE_PERCENT / 100
    val actualFee = if (fee < 0) {
      -fee
    } else fee

    // actualFee is always positive, irrespective of brDeltaExpected
    val brDeltaExpectedWithFee = brDeltaExpected + actualFee

    mandatoryRateConditions &&
      mandatoryBankConditions &&
      bcReserveDelta == brDeltaExpectedWithFee &&
      validReserveRatio
  }
}
