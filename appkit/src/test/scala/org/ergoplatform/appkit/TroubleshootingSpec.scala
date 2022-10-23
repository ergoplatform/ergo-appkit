package org.ergoplatform.appkit

import org.ergoplatform.{ErgoLikeTransactionSerializer, ErgoLikeTransaction, JsonCodecs}
import org.ergoplatform.appkit.JavaHelpers.UniversalConverter
import org.ergoplatform.restapi.client.{BlockTransactions, PeerFinder, JSON, ErgoTransaction, BlocksApi}
import org.scalatest.{PropSpecLike, Matchers, PropSpec}
import scorex.util.encode.Base16

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

/** This suite can be used to troubleshoot problems with transactions.
  * That specific headerId `f797...c815` contains tx with problematic Tuple in register
  * The code do the following:
  * 1) loads block transactions by headerId
  * 2) for each transaction tx:
  *   - convert to Json
  *   - decode Json into ErgoLikeTransaction
  *   - serialize ergoTx to bytes and then print the txHex
  *   - deserialize txHex back to ErgoLikeTransaction
  *   - check serialization round-trip property
  */
class TroubleshootingSpec extends PeerFinder with PropSpecLike with Matchers {
  object JsonCodecs extends JsonCodecs

  property("deserialize block transactions") {
    val headerId = "f797c5ce22e10300549deffca757dcdff0421cc6440ee5e42e4eeb97866ec815"
    val api = findPeer(true).createService(classOf[BlocksApi])
    val response = api.getBlockTransactionsById(headerId).execute.body()
    val gson = JSON.createGson.setPrettyPrinting.create

    for (tx <- response.getTransactions.asScala) {
      val sTxJson = gson.toJson(tx)
      val txJson = io.circe.parser.parse(sTxJson).right.get
      val ergoTx = JsonCodecs.ergoLikeTransactionDecoder.decodeJson(txJson).right.get
      val bytes = ErgoLikeTransactionSerializer.toBytes(ergoTx)
      val txHex = Base16.encode(bytes)
      println(
        s"""TxID: ${ergoTx.id}
          |TxBytes: $txHex
          |""".stripMargin)
      val txBytes = Base16.decode(txHex).get
      val serializedTx = ErgoLikeTransactionSerializer.fromBytes(txBytes)
      ergoTx shouldBe serializedTx
    }
  }

}
