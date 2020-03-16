package org.ergoplatform.appkit.cli

import org.ergoplatform.appkit.FileMockedErgoClient
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}
import org.ergoplatform.appkit.JavaHelpers._
import java.lang.{String => JString}
import java.util.{List => JList}

object TestCliApplication extends CliApplication

class CliApplicationSpec
    extends PropSpec
        with Matchers
        with ScalaCheckDrivenPropertyChecks
        with ConsoleTesting {

  // NOTE, mainnet data is used for testing
  val testConfigFile = "ergotool.json"

  property("help command") {
    TestCliApplication.commandsMap.values.foreach { c =>
      val consoleOps = parseScenario("")
      val res = runScenario(consoleOps) { console =>
        val name = "help"
        val args = Seq(c.name)
        TestCliApplication.run(name +: (Seq(ConfigOption.cmdText, testConfigFile) ++ args), console, {
          ctx => {
            val nrs = IndexedSeq.empty[String]
            val ers = IndexedSeq.empty[String]
            new FileMockedErgoClient(nrs.convertTo[JList[JString]], ers.convertTo[JList[JString]])
          }
        })
      }

      res should include (s"Command Name:\t${c.name}")
      res should include (s"Doc page:\t${c.docUrl}")
    }
  }


}
