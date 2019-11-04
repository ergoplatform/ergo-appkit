package org.ergoplatform.example

import org.ergoplatform.example.util.RestApiErgoClient
import org.ergoplatform.polyglot._

import scala.util.control.NonFatal
import JavaHelpers._

object PayToAddress {
  val baseUrl = "http://localhost:9051/"
  val delay = 30  // 1 hour (when 1 block is mined every 2 minutes)
  val CMD_LIST = "list"
  val CMD_PAY = "pay"

  def list(ergoClient: ErgoClient, cmdArgs: CmdArgs) = {
    val res = ergoClient.execute(ctx => {
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes.convertTo[IndexedSeq[InputBox]]//(Iso.JListToIndexedSeq(Iso.identityIso[InputBox]))
      val lines = boxes.map(b => b.toJson).mkString("[", ",\n", "]")
      lines
    })
    println(res)
  }

  def pay(ergoClient: ErgoClient, cmdArgs: CmdArgs) = {
    val res = ergoClient.execute(ctx => {
      println(s"Context: ${ctx.getHeight}, ${ctx.getNetworkType}")
      val prover = ctx.newProverBuilder()
          .withMnemonic(cmdArgs.seed, cmdArgs.password)
          .build()
      println(s"Prover: ${prover.getP2PKAddress}")
      val wallet = ctx.getWallet
      val boxes = wallet.getUnspentBoxes
      println(s"Unspent Boxes: ${boxes}")
      val box = boxes.get(1)
      println(s"Box to spend: ${box}")
      val txB = ctx.newTxBuilder()
      val newBox = txB.outBoxBuilder()
          .value(Parameters.OneErg)
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
      println(s"Signed transaction: ${signed}")
      val txId = ctx.sendTransaction(signed)
      s"""{ TransactionId: "$txId"}"""
    })
    println(res)
  }

  def parseArgs(args: Seq[String]): CmdArgs = {
    val cmd = args(0)
    val seed = args(1)
    val pass = args(2)
    val apiKey = args(3)
    CmdArgs(cmd, seed, pass, apiKey)
  }
  
  def main(args: Array[String]) = {
    try {
      val cmdArgs = parseArgs(args)
      val ergoClient = RestApiErgoClient.create(baseUrl, NetworkType.TESTNET, cmdArgs.apiKey)
      cmdArgs.cmd match {
        case CMD_LIST =>
          list(ergoClient, cmdArgs)
        case CMD_PAY =>
          pay(ergoClient, cmdArgs)
        case op =>
          sys.error(s"Unknown operation $op")
      }
    }
    catch { case NonFatal(t) =>
      println(t.getMessage)
      printUsage()
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

}

case class CmdArgs(cmd: String, seed: String, password: String, apiKey: String)
