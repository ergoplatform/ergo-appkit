package org.ergoplatform.example

import org.ergoplatform.example.util.RestApiErgoClient
import org.ergoplatform.polyglot.NetworkType

object PayToAddress {
  def main(args: Array[String]) = {
    val seed = args(0)
    val pass = args(1)
    val apiKey = args(2)
    val baseUrl = "http://localhost:9051/"
    val ergoClient = RestApiErgoClient.create(baseUrl, NetworkType.TESTNET, apiKey)

    // Exercise your application code, which should make those HTTP requests.
    // Responses are returned in the same order that they are enqueued.
    val res = ergoClient.execute(ctx => {
       val prover = ctx.newProverBuilder()
           .withMnemonic(seed, pass)
           .build()
       val wallet = ctx.getWallet
       val boxes = wallet.getUnspentBoxes
       Array(
         s"Context: ${ctx.getHeight}, ${ctx.getNetworkType}",
         s"Prover: ${prover.getP2PKAddress}",
         s"Unspent Boxes: ${boxes}",

       ).mkString("\n")
    })

    println(res)
  }
}
