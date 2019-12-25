package org.ergoplatform.appkit

import okhttp3.mockwebserver.{MockResponse, MockWebServer}
import org.ergoplatform.Height
import org.ergoplatform.appkit.examples.ExampleScenarios
import org.ergoplatform.appkit.impl.ErgoTreeContract
import org.ergoplatform.settings.ErgoAlgos
import org.ergoplatform.validation.ValidationRules
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scalan.util.FileUtil._
import sigmastate.{BoolToSigmaProp, LT, SInt}
import sigmastate.Values.{ConstantPlaceholder, IntConstant, SigmaPropConstant}
import sigmastate.serialization.ErgoTreeSerializer
import sigmastate.verification.contract.DummyContractCompilation

class ApiClientSpec
    extends PropSpec
        with Matchers
        with ScalaCheckDrivenPropertyChecks
        with AppkitTesting {

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

  def createMockWebServer(): MockWebServer = {
    val server = new MockWebServer
    // Schedule some responses.
    val nodeInfoJson = read(file("appkit/src/test/resources/org/ergoplatform/appkit/response_NodeInfo.json"))
    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(nodeInfoJson))

    val lastHeadersJson = read(file("appkit/src/test/resources/org/ergoplatform/appkit/response_LastHeaders.json"))
    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(lastHeadersJson))

    val boxJson = read(file("appkit/src/test/resources/org/ergoplatform/appkit/response_Box.json"))
    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(boxJson))
    server
  }


  property("BlockchainContext") {
    // Create a MockWebServer. These are lean enough that you can create a new
    // instance for every unit test.
    val server = createMockWebServer()
    server.start()

    // Ask the server for its URL. You'll need this to make HTTP requests.
    val baseUrl = server.url("/").toString
    val ergoClient = RestApiErgoClient.create(baseUrl, NetworkType.TESTNET, "")

    // Exercise your application code, which should make those HTTP requests.
    // Responses are returned in the same order that they are enqueued.
    val res = ergoClient.execute(ctx => {
      val r = new ExampleScenarios(ctx)
      val res = r.aggregateUtxoBoxes(seed, addrStr, 10, "83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a")
      res
    })

    println(res)

    // Optional: confirm that your app made the HTTP requests you were expecting.
    val request1 = server.takeRequest
    request1.getRequestLine shouldBe "GET /info HTTP/1.1"
    val request2 = server.takeRequest
    request2.getRequestLine shouldBe "GET /blocks/lastHeaders/10 HTTP/1.1"

    // Shut down the server. Instances cannot be reused.
    server.shutdown()
  }

  property("get ErgoTree from verified contract") {
    val verifiedContract = DummyContractCompilation.contractInstance(1000)
    println(verifiedContract.ergoTree)
    verifiedContract.ergoTree.constants.length shouldBe 1
    verifiedContract.ergoTree.constants.head shouldEqual IntConstant(1000)
    verifiedContract.ergoTree.root.right.get shouldEqual
      BoolToSigmaProp(LT(Height, ConstantPlaceholder(0, SInt)))
  }
}
