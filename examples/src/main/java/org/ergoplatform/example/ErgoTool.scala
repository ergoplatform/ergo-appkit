package org.ergoplatform.example

import org.ergoplatform.example.util.RestApiErgoClient
import org.ergoplatform.polyglot._

import scala.util.control.NonFatal
import JavaHelpers._
import com.typesafe.config.ConfigFactory
import pureconfig.ConfigReader.Result
import pureconfig.generic.ProductHint
import pureconfig.{ConfigSource, CamelCase, ConfigFieldMapping}
import pureconfig.generic.auto._

object Configurations {
  implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  case class ApiConfig(apiUrl: String, apiKey: String)
  case class WalletConfig(mnemonic: String, password: String, mnemonicPassword: String)
  case class ErgoNodeConfig(nodeApi: ApiConfig, wallet: WalletConfig, networkType: NetworkType)
  case class ErgoToolConfig(node: ErgoNodeConfig)

  def load(): Result[ErgoToolConfig] = {
    val conf = ConfigSource.default.at("ergotool").load[ErgoToolConfig]
    conf
  }
}

object ErgoTool {

  val baseUrl = "http://localhost:9051/"
  val delay = 30  // 1 hour (when 1 block is mined every 2 minutes)
  val CMD_LIST = "list"
  val CMD_PAY = "pay"

  def main(args: Array[String]) = {
    val conf = Configurations.load()
//    val conf = ConfigFactory.load()
    val cmdOpt = try {
      Some(parseCmd(args))
    }
    catch { case NonFatal(t) =>
      println(t.getMessage)
      printUsage()
      None
    }
    cmdOpt.foreach { cmd =>
      val ergoClient = RestApiErgoClient.create(baseUrl, NetworkType.TESTNET, cmd.apiKey)
      cmd match {
        case c: ListCmd =>
          list(ergoClient, c)
        case c: PayCmd =>
          pay(ergoClient, c)
      }
    }
  }


  def parseCmd(args: Seq[String]): Cmd = {
    val cmd = args(0)
    val seed = args(1)
    val pass = args(2)
    val apiKey = args(3)
    cmd match {
      case CMD_LIST =>
        val limit = if (args.length > 4) args(4).toInt else 10
        ListCmd(cmd, seed, pass, apiKey, limit)
      case CMD_PAY =>
        val amount = if (args.length > 4) args(4).toLong else sys.error(s"Payment amound is not defined")
        PayCmd(cmd, seed, pass, apiKey, amount)
      case _ =>
        sys.error(s"Unknown command: $cmd")
    }
  }

  def printUsage() = {
    val msg =
      s"""
        | Usage:
        | wallet (list|pay) seed pass apiKey
     """.stripMargin
    println(msg)
  }

  def list(ergoClient: ErgoClient, cmd: ListCmd) = {
    val res = ergoClient.execute(ctx => {
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes.convertTo[IndexedSeq[InputBox]]
      val lines = boxes.take(cmd.limit).map(b => b.toJson(true)).mkString("[", ",\n", "]")
      lines
    })
    println(res)
  }

  def pay(ergoClient: ErgoClient, cmd: PayCmd) = {
    val res = ergoClient.execute(ctx => {
      println(s"Context: ${ctx.getHeight}, ${ctx.getNetworkType}")
      val prover = ctx.newProverBuilder()
          .withMnemonic(cmd.seed, cmd.password)
          .build()
      println(s"ProverAddress: ${prover.getP2PKAddress}")
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes
      val box = boxes.get(0)
      println(s"InputBox: ${box.toJson(true)}")
      val txB = ctx.newTxBuilder()
      val newBox = txB.outBoxBuilder()
          .value(cmd.payAmount)
          .contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("deadline", ctx.getHeight + delay)
                .item("pkOwner", prover.getP2PKAddress.pubkey)
                .build(),
            "{ sigmaProp(HEIGHT > deadline) && pkOwner }"))
          .build()
      val tx = txB.boxesToSpend(box)
          .outputs(newBox)
          .fee(Parameters.MinFee)
          .sendChangeTo(prover.getP2PKAddress)
          .build()
      val signed = prover.sign(tx)
      val txId = ctx.sendTransaction(signed)
      (signed.toJson(true), txId)
    })
    println(s"SignedTransaction: ${res}")
  }


}

sealed trait Cmd {
  def name: String
  def seed: String
  def password: String
  def apiKey: String
}

case class ListCmd(name: String, seed: String, password: String, apiKey: String, limit: Int) extends Cmd
case class PayCmd(name: String, seed: String, password: String, apiKey: String, payAmount: Long) extends Cmd
