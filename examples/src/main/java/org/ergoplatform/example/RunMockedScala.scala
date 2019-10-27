package org.ergoplatform.example

import org.ergoplatform.example.util.FileMockedErgoClient

object RunMockedScala extends App {
  import org.ergoplatform.example.MockData._
  val res = new FileMockedErgoClient(infoFile, lastHeadersFile, boxFile).execute { ctx =>
    val r = new ExampleScenarios(ctx)
    val res = r.aggregateUtxoBoxes("abc", 10, "83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a")
    res
  }
  println(res)
}
