package org.ergoplatform.appkit

import org.ergoplatform.appkit.examples.util.FileMockedErgoClient
import scalan.util.FileUtil
import JavaHelpers._
import java.util.{List => JList}
import java.lang.{String => JString}

trait HttpClientTesting {
  val responsesDir = "appkit/src/test/resources/mockwebserver"
  val addr1 = "9f4QF8AD1nQ3nJahQVkMj8hFSVVzVom77b52JU7EW71Zexg6N8v"

  def loadNodeResponse(name: String) = {
    FileUtil.read(FileUtil.file(s"$responsesDir/node_responses/$name"))
  }

  def loadExplorerResponse(name: String) = {
    FileUtil.read(FileUtil.file(s"$responsesDir/explorer_responses/$name"))
  }

  case class MockData(nodeResponses: Seq[String] = Nil, explorerResponses: Seq[String] = Nil)
  object MockData {
    def empty = MockData()
  }

  def createMockedErgoClient(data: MockData): FileMockedErgoClient = {
    val nrs = IndexedSeq(
      loadNodeResponse("response_NodeInfo.json"),
      loadNodeResponse("response_LastHeaders.json")) ++ data.nodeResponses
    val ers: IndexedSeq[String] = data.explorerResponses.toIndexedSeq
    new FileMockedErgoClient(nrs.convertTo[JList[JString]], ers.convertTo[JList[JString]])
  }
}
