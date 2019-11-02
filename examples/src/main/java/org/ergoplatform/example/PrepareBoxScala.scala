package org.ergoplatform.example

import org.ergoplatform.example.util.FileMockedErgoClient
import org.ergoplatform.polyglot.ConstantsBuilder
import org.ergoplatform.polyglot.impl.ErgoScriptContract

object PrepareBoxScala {
  def main(args: Array[String]) = {
    import org.ergoplatform.example.MockData._
    val res = new FileMockedErgoClient(infoFile, lastHeadersFile, boxFile).execute { ctx =>
      val mockTxB = ctx.newTxBuilder()
      val out = mockTxB.outBoxBuilder()
          .contract(ctx.compileContract(
              ConstantsBuilder.empty(),
              "{ sigmaProp(CONTEXT.headers.size == 9) }"))
          .build()
      val spendingTxB = ctx.newTxBuilder()
      val tx = spendingTxB
          .boxesToSpend(out.convertToInputWith(mockTxId, 0))
          .outputs(
            spendingTxB.outBoxBuilder()
                .contract(ctx.compileContract(ConstantsBuilder.empty(), "{ true }"))
                .build())
          .build()
      val proverB = ctx.newProverBuilder
      val prover = proverB.withMnemonic("abc", null).build()
      val signed = prover.sign(tx)
      signed
    }
    println(res)
  }
}
