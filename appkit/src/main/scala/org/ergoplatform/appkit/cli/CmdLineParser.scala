package org.ergoplatform.appkit.cli

import org.ergoplatform.appkit.NetworkType

import scala.collection.mutable.ArrayBuffer
import org.ergoplatform.appkit.commands.usageError

object CmdLineParser {
  /** Extracts options like `--conf myconf.json` from the command line.
    *
    * The command line is parsed using the following simple algorithm:
    *
    *   1) The whole line starting from executable file name is split by whitespace into parts
    *      which are passed as `args` of this method (this is done by java app launcher)
    *
    *   2) The sequence of args is traversed and each starting with `--` is parsed into option
    *      name-value pair and extracted from original args sequence. Any error in option parsing is
    *      reported via [[org.ergoplatform.appkit.commands.usageError()]]
    *
    *   3) Any option with [[CmdOption.isFlag]] == true is parsed without value (the value is not
    *      expected to be on the command line)
    *
    *   4) After the options with their values are extracted then everything that remains in `args` is
    *      interpreted as command parameters
    *
    * @param args the command line split by whitespaces into parts
    */
  def parseOptions(args: Seq[String]): (Map[String, String], Seq[String]) = {
    var resOptions = Map.empty[String, String]

    // clone args into mutable buffer to extract option
    // what will remain will be returned as parameters
    val resArgs: ArrayBuffer[String] = ArrayBuffer.empty
    resArgs ++= args.toArray.clone()

    // scan until the end of the buffer
    var i = 0
    while (i < resArgs.length) {
      val arg = resArgs(i)
      if (arg.startsWith(CmdOption.Prefix)) {
        // this must be an option name
        val name = arg.drop(CmdOption.Prefix.length)
        CmdOption.options.find(_.name == name) match {
          case Some(o) =>
            if (o.isFlag) {
              resOptions = resOptions + (o.name -> "true")
            } else {
              if (i + 1 >= resArgs.length)
                usageError(s"Value for the non-flag command ${o.name} is not provided: unexpected end of command line.", None)
              resOptions = resOptions + (o.name -> resArgs(i + 1))
              resArgs.remove(i + 1) // remove option value
            }
          case _ =>
            usageError(s"Unknown option name: $arg", None)
        }
        // option parsed, remove it from buffer
        resArgs.remove(i)
      } else {
        // this is parameter, leave it in buffer
        i += 1
      }
    }
    (resOptions, resArgs)
  }

  def parseNetwork(network: String): NetworkType = network match {
    case "testnet" => NetworkType.TESTNET
    case "mainnet" => NetworkType.MAINNET
    case _ => usageError(s"Invalid network type $network", None)
  }


}
