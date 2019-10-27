package org.ergoplatform.polyglot

import org.scalatest.{PropSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.example.ExampleScenarios
import org.ergoplatform.polyglot.impl.BlockchainContextBuilderImpl
import org.ergoplatform.restapi.client.ApiClient
import org.ergoplatform.settings.ErgoAlgos
import org.ergoplatform.validation.ValidationRules
import scalan.util.FileUtil._
import sigmastate.Values.SigmaPropConstant
import sigmastate.serialization.ErgoTreeSerializer

class ApiClientSpec
    extends PropSpec
        with Matchers
        with ScalaCheckDrivenPropertyChecks {

  val seed = "abc"
  val masterKey = JavaHelpers.seedToMasterKey(seed)
  implicit val vs = ValidationRules.currentSettings

  property("parse ErgoTree") {
    val script = "100204a00b08cd02d3a9410ac758ad45dfc85af8626efdacf398439c73977b13064aa8e6c8f2ac88ea02d192a39a8cc7a70173007301"
    val treeBytes = ErgoAlgos.decode(script).get
    val tree = ErgoTreeSerializer.DefaultSerializer.deserializeErgoTree(treeBytes)
    println(tree)
    val newBytes = ErgoTreeSerializer.DefaultSerializer.substituteConstants(
      treeBytes, positions = Array(1), newVals = Array(SigmaPropConstant(masterKey.key.publicImage)))
    val newTree = ErgoTreeSerializer.DefaultSerializer.deserializeErgoTree(newBytes)
    println(newTree)
    println(ErgoAlgos.encode(newBytes))
  }

  property("BlockchainContext") {
    // Create a MockWebServer. These are lean enough that you can create a new
    // instance for every unit test.
    val server = new MockWebServer
    // Schedule some responses.
    val nodeInfoJson = read(file("examples/src/main/resources/org/ergoplatform/polyglot/response_NodeInfo.json"))
    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(nodeInfoJson))

    val lastHeadersJson = read(file("examples/src/main/resources/org/ergoplatform/polyglot/response_LastHeaders.json"))
    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(lastHeadersJson))

    val boxJson = read(file("examples/src/main/resources/org/ergoplatform/polyglot/response_Box.json"))
    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(boxJson))
    server.start()

    // Ask the server for its URL. You'll need this to make HTTP requests.
    val baseUrl = server.url("/")
    val client = new ApiClient(baseUrl.toString)

    // Exercise your application code, which should make those HTTP requests.
    // Responses are returned in the same order that they are enqueued.
    val ctx = new BlockchainContextBuilderImpl(client, ErgoAddressEncoder.MainnetNetworkPrefix).build()
    val r = new ExampleScenarios(ctx)
    val res = r.aggregateUtxoBoxes(seed, 10, "83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a")
    println(res)

    // Optional: confirm that your app made the HTTP requests you were expecting.
    val request1 = server.takeRequest
    request1.getRequestLine shouldBe "GET /info HTTP/1.1"
    val request2 = server.takeRequest
    request2.getRequestLine shouldBe "GET /blocks/lastHeaders/10 HTTP/1.1"
    val request3 = server.takeRequest
    request3.getRequestLine shouldBe "GET /utxo/byId/83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a HTTP/1.1"

    // Shut down the server. Instances cannot be reused.
    server.shutdown()
  }
}
