package org.ergoplatform.polyglot

import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.polyglot.impl.BlockchainContextBuilderImpl
import org.ergoplatform.restapi.client.ApiClient
import scalan.util.FileUtil.{file, read}
import okhttp3.mockwebserver.{MockWebServer, MockResponse}

case class FileMockedRunner(nodeInfoFile: String, lastHeadersFile: String, val boxFile: String)
  extends MockedRunner {
  override def nodeInfoResp: String = read(file(nodeInfoFile))

  override def lastHeadersResp: String = read(file(lastHeadersFile))

  override def boxResp: String = read(file(boxFile))

  def run[T](scenario: BlockchainContext => T): T = {
    val server = new MockWebServer
    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(nodeInfoResp))

    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(lastHeadersResp))

    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(boxResp))
    server.start()

    val baseUrl = server.url("/")
    val client = new ApiClient(baseUrl.toString)
    val ctx = new BlockchainContextBuilderImpl(client, ErgoAddressEncoder.MainnetNetworkPrefix).build()

    val res = scenario(ctx)

    server.shutdown()
    res
  }
}
