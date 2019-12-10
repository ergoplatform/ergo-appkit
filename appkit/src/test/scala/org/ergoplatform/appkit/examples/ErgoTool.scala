package org.ergoplatform.appkit.examples

import java.util.Arrays

import org.ergoplatform.appkit.{RestApiErgoClient, _}

import scala.util.control.NonFatal
import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.config.ErgoToolConfig

object ErgoTool {
  val delay = 30  // 1 hour (when 1 block is mined every 2 minutes)
  val CMD_LIST = "list"
  val CMD_PAY = "pay"

  def main(args: Array[String]) = {
    val cmdOpt = try {
      Some(parseCmd(args))
    }
    catch { case NonFatal(t) =>
      println(t.getMessage)
      printUsage()
      None
    }
    cmdOpt.foreach { cmd =>
      val ergoClient = RestApiErgoClient.create(cmd.apiUrl, cmd.networkType, cmd.apiKey)
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
    val toolConf = ErgoToolConfig.load("ergotool.json")
    cmd match {
      case CMD_LIST =>
        val limit = if (args.length > 1) args(1).toInt else 10
        ListCmd(toolConf, cmd, limit)
      case CMD_PAY =>
        val amount = if (args.length > 1) args(1).toLong else sys.error(s"Payment amound is not defined")
        PayCmd(toolConf, cmd, amount)
      case _ =>
        sys.error(s"Unknown command: $cmd")
    }
  }

  def printUsage() = {
    val msg =
      s"""
        | Usage:
        | ergotool action [action parameters]
        |
        | Available actions:
        |   list <limit> - list top <limit> confirmed wallet boxes
        |   pay  <amount> - amount of NanoErg to put into the new box
     """.stripMargin
    println(msg)
  }

  def list(ergoClient: ErgoClient, cmd: ListCmd) = {
    val res = ergoClient.execute(ctx => {
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes(0).get().convertTo[IndexedSeq[InputBox]]
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
      val boxes = wallet.getUnspentBoxes(0).get()
      val box = boxes.get(0)
      println(s"InputBox: ${box.toJson(true)}")
      val txB = ctx.newTxBuilder()
      val newBox = txB.outBoxBuilder()
          .value(cmd.payAmount)
          .contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("freezeDeadline", ctx.getHeight + delay)
                .item("pkOwner", prover.getP2PKAddress.pubkey)
                .build(),
            "{ sigmaProp(HEIGHT > freezeDeadline) && pkOwner }"))
          .build()
      val tx = txB.boxesToSpend(Arrays.asList(box))
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
  def toolConf: ErgoToolConfig
  def name: String
  def seed: String = toolConf.getNode.getWallet.getMnemonic
  def password: String = toolConf.getNode.getWallet.getPassword
  def apiUrl: String = toolConf.getNode.getNodeApi.getApiUrl
  def apiKey: String = toolConf.getNode.getNodeApi.getApiKey
  def networkType: NetworkType = toolConf.getNode.getNetworkType
}

case class ListCmd(toolConf: ErgoToolConfig, name: String, limit: Int) extends Cmd
case class PayCmd(toolConf: ErgoToolConfig, name: String, payAmount: Long) extends Cmd
