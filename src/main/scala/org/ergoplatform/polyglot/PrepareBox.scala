package org.ergoplatform.polyglot

object PrepareBox  {

  def main(args: Array[String]) = {
    import MockData._

    val res = FileMockedRunner(infoFile, lastHeadersFile, boxFile).run { ctx =>
      val mockTxB = ctx.newTxBuilder()
      val out = mockTxB.outBoxBuilder()
          .contract(ConstantsBuilder.empty(), "{ sigmaProp(CONTEXT.headers.size == 9) }")
          .build()
      val spendingTxB = ctx.newTxBuilder()
      val tx = spendingTxB
          .boxesToSpend(out.convertToInputWith(mockTxId, 0))
          .outputs(
               spendingTxB.outBoxBuilder()
                   .contract(ConstantsBuilder.empty(), "{ true }")
                   .build())
          .build()
      val proverB = ctx.newProver
      val prover = proverB.withSeed("abc").build()
      val signed = prover.sign(tx)
      signed
    }

    println(res)
  }
}
