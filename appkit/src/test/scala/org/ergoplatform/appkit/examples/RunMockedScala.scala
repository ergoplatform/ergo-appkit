package org.ergoplatform.appkit.examples

import org.ergoplatform.appkit.AppkitTesting
import org.ergoplatform.appkit.examples.util.FileMockedErgoClient

object RunMockedScala extends App with AppkitTesting {
  import org.ergoplatform.appkit.examples.MockData._
  val res = new FileMockedErgoClient(infoFile, lastHeadersFile, boxFile).execute { ctx =>
    val r = new ExampleScenarios(ctx)
    val res = r.aggregateUtxoBoxes("abc", addrStr, 10, "83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a")
    res
  }
  println(res)
}
