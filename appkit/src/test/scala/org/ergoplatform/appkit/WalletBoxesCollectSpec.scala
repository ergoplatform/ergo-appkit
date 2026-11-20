package org.ergoplatform.appkit

import okhttp3.mockwebserver.{MockResponse, MockWebServer}
import org.ergoplatform.restapi.client.{ApiClient, BoxesRequestHolder, WalletApi}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec

class WalletBoxesCollectSpec extends AnyPropSpec with Matchers {

  property("WalletApi.walletBoxesCollect sends POST /wallet/boxes/collect with object targetAssets and parses CollectedBoxes") {
    val server = new MockWebServer()
    server.start()
    try {
      val tokenId = "d47f958b201dc7162f641f7eb055e9fa7a9cb65cc24d4447a10f86675fc58328"
      val boxId = "6ee4d5d0dc518d52386cef251efa732680a8600ecac87ee3ad95c7aa1d8282b3"
      val txId = "5eb715015c6d894303f3ba9c1796791c9a2af61aab659b8a63d890fbbc92756b"
      val ergoTree = "0008cd02d3a9410ac758ad45dfc85af8626efdacf398439c73977b13064aa8e6c8f2ac88"

      val emptyChangeJson =
        s"""{
           |  "boxes": [{
           |    "boxId": "$boxId",
           |    "value": 1000000000,
           |    "ergoTree": "$ergoTree",
           |    "creationHeight": 120,
           |    "assets": [],
           |    "additionalRegisters": {},
           |    "transactionId": "$txId",
           |    "index": 0
           |  }],
           |  "changeBoxes": []
           |}""".stripMargin

      val assetChangeJson =
        s"""{
           |  "boxes": [{
           |    "boxId": "$boxId",
           |    "value": 2500000000,
           |    "ergoTree": "$ergoTree",
           |    "creationHeight": 120,
           |    "assets": [{"tokenId": "$tokenId", "amount": 75}],
           |    "additionalRegisters": {},
           |    "transactionId": "$txId",
           |    "index": 0
           |  }],
           |  "changeBoxes": [{
           |    "value": 500000000,
           |    "tokens": {"$tokenId": 25}
           |  }]
           |}""".stripMargin

      server.enqueue(new MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(emptyChangeJson))
      server.enqueue(new MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(assetChangeJson))

      val apiClient = new ApiClient(server.url("/").toString, "ApiKeyAuth", "test-api-key")
      val walletService = apiClient.createService(classOf[WalletApi])

      // 1. Default BoxesRequestHolder (targetBalance only -> empty targetAssets JSON object {})
      val req1 = new BoxesRequestHolder().targetBalance(Parameters.OneErg)
      val resp1 = walletService.walletBoxesCollect(req1).execute()
      resp1.isSuccessful shouldBe true
      resp1.body().getBoxes.size() shouldBe 1
      resp1.body().getBoxes.get(0).getBoxId shouldBe boxId
      resp1.body().getBoxes.get(0).getValue shouldBe Parameters.OneErg
      resp1.body().getChangeBoxes.isEmpty shouldBe true

      val recorded1 = server.takeRequest()
      recorded1.getMethod shouldBe "POST"
      recorded1.getPath shouldBe "/wallet/boxes/collect"
      recorded1.getHeader("api_key") shouldBe "test-api-key"
      val body1 = recorded1.getBody.readUtf8()
      body1 should include("\"targetAssets\":{}")
      body1 should include(s""""targetBalance":${Parameters.OneErg}""")

      // 2. BoxesRequestHolder with targetAssets map and returned changeBoxes
      val req2 = new BoxesRequestHolder()
        .targetBalance(2000000000L)
        .putTargetAssetsItem(tokenId, 50L)
      val resp2 = walletService.walletBoxesCollect(req2).execute()
      resp2.isSuccessful shouldBe true
      resp2.body().getBoxes.size() shouldBe 1
      resp2.body().getBoxes.get(0).getAssets.size() shouldBe 1
      resp2.body().getBoxes.get(0).getAssets.get(0).getTokenId shouldBe tokenId
      resp2.body().getBoxes.get(0).getAssets.get(0).getAmount shouldBe 75L
      resp2.body().getChangeBoxes.size() shouldBe 1
      resp2.body().getChangeBoxes.get(0).getValue shouldBe 500000000L
      resp2.body().getChangeBoxes.get(0).getTokens.get(tokenId) shouldBe 25L

      val recorded2 = server.takeRequest()
      recorded2.getMethod shouldBe "POST"
      recorded2.getPath shouldBe "/wallet/boxes/collect"
      val body2 = recorded2.getBody.readUtf8()
      body2 should include(s""""$tokenId":50""")
      body2 should include("\"targetBalance\":2000000000")
    } finally {
      server.shutdown()
    }
  }
}
