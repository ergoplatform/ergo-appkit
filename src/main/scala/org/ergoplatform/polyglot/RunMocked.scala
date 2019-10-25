package org.ergoplatform.polyglot

import org.ergoplatform.examples.ExampleScenarios



object RunMocked extends App {
  import MockData._
  val res = FileMockedRunner(infoFile, lastHeadersFile, boxFile).run { ctx =>
    val r = new ExampleScenarios
    val res = r.spendBoxes(ctx, "83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a")
    res
  }
  println(res)
}
