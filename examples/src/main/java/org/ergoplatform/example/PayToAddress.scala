package org.ergoplatform.example

import org.ergoplatform.example.util.RestApiErgoClient
import org.ergoplatform.polyglot.{Constants, NetworkType, ConstantsBuilder, Parameters}

object PayToAddress {

  val baseUrl = "http://localhost:9051/"

  def main(args: Array[String]) = {
    val seed = args(0)
    val pass = args(1)
    val apiKey = args(2)
    val delay = 30  // 1 hour
    val ergoClient = RestApiErgoClient.create(baseUrl, NetworkType.TESTNET, apiKey)
    // Exercise your application code, which should make those HTTP requests.
    // Responses are returned in the same order that they are enqueued.
    val res = ergoClient.execute(ctx => {
      println(s"Context: ${ctx.getHeight}, ${ctx.getNetworkType}")
      val prover = ctx.newProverBuilder()
          .withMnemonic(seed, pass)
          .build()
      println(s"Prover: ${prover.getP2PKAddress}")
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes
      println(s"Unspent Boxes: ${boxes}")
      val box = boxes.get(1)
      println(s"Box to spend: ${box}")
      val txB = ctx.newTxBuilder()
      val newBox = txB.outBoxBuilder()
          .value(Parameters.OneErg)
          .contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("deadline", ctx.getHeight + delay)
                .item("pkOwner", prover.getP2PKAddress.pubkey)
                .build(),
            "{ sigmaProp(HEIGHT > deadline) && pkOwner }"))
          .build()
      val tx = txB.boxesToSpend(box)
          .outputs(newBox)
          .fee(Parameters.MinFee)
          .sendChangeTo(prover.getP2PKAddress)
          .build()
      val signed = prover.sign(tx)
      println(s"Signed transaction: ${signed}")
      ctx.sendTransaction(signed)
      true
    })
    println(res)
  }
}
