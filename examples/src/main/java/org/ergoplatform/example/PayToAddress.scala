package org.ergoplatform.example

import org.ergoplatform.example.util.RestApiErgoClient
import org.ergoplatform.polyglot.NetworkType

object PayToAddress {
  def main(args: Array[String]) = {
    val seed = args(0)
    val pass = args(1)
    val baseUrl = "http://localhost:9051/"
    val ergoClient = RestApiErgoClient.create(baseUrl, NetworkType.TESTNET)

    // Exercise your application code, which should make those HTTP requests.
    // Responses are returned in the same order that they are enqueued.
    val res = ergoClient.execute(ctx => {
       val prover = ctx.newProverBuilder()
           .withMnemonic(seed, pass)
           .build()
       Array(
         s"Context: ${ctx.getHeight}, ${ctx.getNetworkType}",
         s"Prover: ${prover.getP2PKAddress}",
       ).mkString("\n")
    })

    println(res)
  }
}
