package org.ergoplatform.appkit.impl

import org.ergoplatform.appkit.{Address, NetworkType, MultisigAddress}
import org.scalacheck.Gen
import sigmastate.serialization.generators.ObjectGenerators
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}
import sigmastate.CTHRESHOLD
import sigmastate.basics.DLogProtocol.ProveDlog
import sigmastate.helpers.NegativeTesting

import scala.collection.JavaConverters._

class MultisigAddressTests extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks
  with ObjectGenerators with NegativeTesting {

  lazy val thresholdGen = for {
    n <- Gen.choose(1, 20)
    k <- Gen.choose(1, n)
    children <- Gen.listOfN(n, proveDlogGen)
  } yield CTHRESHOLD(k, children)

  property("getAddress/fromAddress roundtrip") {
    // do create a multisig address, convert to Address and back
    forAll(thresholdGen, MinSuccessful(50)) { threshold: CTHRESHOLD =>
      val multisigAddress = MultisigAddress.buildFromParticipants(
        threshold.k,
        threshold.children.map(Address.fromSigmaBoolean(_, NetworkType.MAINNET)).asJava,
        NetworkType.MAINNET)

      multisigAddress.getSignersRequiredCount shouldBe threshold.k
      multisigAddress.getParticipants.asScala.map(_.asP2PK().pubkey) shouldBe threshold.children.map(_.asInstanceOf[ProveDlog])

      // check roundtrip
      val address = multisigAddress.getAddress
      val multisigAddress2 = MultisigAddress.buildFromAddress(address)
      multisigAddress2 shouldBe multisigAddress
    }
  }

  property("fromAddress negative") {
    // check that fromAddress fails on non-P2S addresses
    forAll(proveDlogGen) { dlog: ProveDlog =>
      val address = Address.fromSigmaBoolean(dlog, NetworkType.MAINNET)
      assertExceptionThrown(
        MultisigAddress.buildFromAddress(address),
        exceptionLike[IllegalArgumentException]("is not a P2S address and cannot be multisig address")
      )
    }

    { // check that fromAddress fails on non-Pay2SigmaProp addresses
      val address = Address.fromErgoTree(TrueTree, NetworkType.MAINNET)
      assertExceptionThrown(
        MultisigAddress.buildFromAddress(address),
        exceptionLike[IllegalArgumentException]("is not a valid multisig address")
      )
    }
  }
}