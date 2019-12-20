package org.ergoplatform.appkit.examples

import java.io.File

import org.ergoplatform.appkit.{AppkitTesting, JavaHelpers}
import org.ergoplatform.appkit.examples.util.FileMockedErgoClient
import scalan.util.FileUtil
import JavaHelpers._
import java.lang.{String => JString}
import java.util.{List => JList}

object RunMockedScala extends App with AppkitTesting {
  import org.ergoplatform.appkit.examples.MockData._
  val nodeResponses = IndexedSeq(infoFile, lastHeadersFile, boxFile)
    .map(fn => FileUtil.read(new File(fn)))
    .convertTo[JList[JString]]

  val res = new FileMockedErgoClient(
    nodeResponses,
    java.util.Arrays.asList()).execute { ctx =>
    val r = new ExampleScenarios(ctx)
    val res = r.aggregateUtxoBoxes("abc", addrStr, 10, "83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a")
    res
  }
  println(res)
}
