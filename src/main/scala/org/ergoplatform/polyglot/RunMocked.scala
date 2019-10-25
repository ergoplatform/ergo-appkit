package org.ergoplatform.polyglot

import org.ergoplatform.polyglot.impl.BlockchainContextBuilderImpl
import scalan.util.FileUtil.{read, file}
import okhttp3.mockwebserver.{MockResponse, MockWebServer}
import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.examples.ExampleScenarios
import org.ergoplatform.restapi.client.ApiClient

case class MockedRunner(
  nodeInfoResp: String, lastHeadersResp: String, boxResp: String) {
}

object RunMocked extends App {
  val server = new MockWebServer
  val nodeInfoJson = read(file("src/main/resources/org/ergoplatform/polyglot/response_NodeInfo.json"))
  server.enqueue(new MockResponse()
      .addHeader("Content-Type", "application/json; charset=utf-8")
      .setBody(nodeInfoJson))

  val lastHeadersJson = read(file("src/main/resources/org/ergoplatform/polyglot/response_LastHeaders.json"))
  server.enqueue(new MockResponse()
      .addHeader("Content-Type", "application/json; charset=utf-8")
      .setBody(lastHeadersJson))

  val boxJson = read(file("src/main/resources/org/ergoplatform/polyglot/response_Box.json"))
  server.enqueue(new MockResponse()
      .addHeader("Content-Type", "application/json; charset=utf-8")
      .setBody(boxJson))
  server.start()

  val baseUrl = server.url("/")
  val client = new ApiClient(baseUrl.toString)

  val ctx = new BlockchainContextBuilderImpl(client, ErgoAddressEncoder.MainnetNetworkPrefix).build()
  val r = new ExampleScenarios
  val res = r.spendBoxes(ctx, "83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a")
  println(res)

  server.shutdown()
}
