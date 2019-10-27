package org.ergoplatform.example

import org.ergoplatform.example.util.FileMockedErgoClient
import org.ergoplatform.polyglot.{ConstantsBuilder, ErgoContract}

object PrepareBoxScala {
  def main(args: Array[String]) = {
    import org.ergoplatform.example.MockData._
    val res = new FileMockedErgoClient(infoFile, lastHeadersFile, boxFile).execute { ctx =>
      val mockTxB = ctx.newTxBuilder()
      val out = mockTxB.outBoxBuilder()
          .contract(ErgoContract.create(
              ConstantsBuilder.empty(),
              "{ sigmaProp(CONTEXT.headers.size == 9) }"))
          .build()
      val spendingTxB = ctx.newTxBuilder()
      val tx = spendingTxB
          .boxesToSpend(out.convertToInputWith(mockTxId, 0))
          .outputs(
            spendingTxB.outBoxBuilder()
                .contract(ErgoContract.create(ConstantsBuilder.empty(), "{ true }"))
                .build())
          .build()
      val proverB = ctx.newProverBuilder
      val prover = proverB.withSeed("abc").build()
      val signed = prover.sign(tx)
      signed
    }
    println(res)
  }
}
