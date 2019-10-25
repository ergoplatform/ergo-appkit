package org.ergoplatform.polyglot

import org.ergoplatform.ErgoBox
import org.ergoplatform.wallet.serialization.JsonCodecsWrapper
import scalan.util.FileUtil.{file, read}
import JsonCodecsWrapper._

object PrepareBox  {

  def main(args: Array[String]) = {
    import MockData._
    val boxTemplate = read(file(boxFile))
    val boxJson = io.circe.parser.parse(boxTemplate).toOption.get
    val box = boxJson.as[ErgoBox]
      .getOrElse(sys.error(s"Cannot read box template from $boxFile"))

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
