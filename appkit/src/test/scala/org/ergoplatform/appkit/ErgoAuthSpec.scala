package org.ergoplatform.appkit

import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scorex.util.Random
import sigmastate.interpreter.HintsBag

import java.nio.charset.StandardCharsets

class ErgoAuthSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks
  with AppkitTestingCommon {

  property("ErgoAuth address roundtrip") {
    // server side
    val serializedSigmaBoolean = ErgoAuthUtils.serializeSigmaBoolean(address)
    // message to sign should be something random and not repeating
    val requestedMessage = addrStr + System.currentTimeMillis().toString

    // transferred to client, and now we are on client side
    val signedMessage = new String(Random.randomBytes(16)) + requestedMessage +
      new String(Random.randomBytes(32))
    val signature = new ColdErgoClient(address.getNetworkType, Parameters.ColdClientMaxBlockCost)
      .execute { ctx =>

        val prover = ctx.newProverBuilder().withMnemonic(mnemonic, SecretString.empty()).build()
        prover.signMessage(ErgoAuthUtils.deserializeSigmaBoolean(serializedSigmaBoolean),
          signedMessage.getBytes(StandardCharsets.UTF_8),
          HintsBag.empty)
      }

    // transferred to server...
    ErgoAuthUtils.verifyResponse(address, requestedMessage, signedMessage, signature) shouldBe (true)

    // and in case someone wanted to fool us
    ErgoAuthUtils.verifyResponse(address,
      requestedMessage,
      signedMessage,
      new Array[Byte](0)) shouldBe (false)
  }

}
