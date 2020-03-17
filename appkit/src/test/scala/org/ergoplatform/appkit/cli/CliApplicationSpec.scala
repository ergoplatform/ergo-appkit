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
        with ConsoleTesting
        with CommandsTesting {

  val testConfigFile = "ergotool.json"
  val responsesDir: String = "appkit/src/test/resources/mockwebserver"

  property("help command") {
    TestCliApplication.commandsMap.values.foreach { c =>
      val res = runCommand(TestCliApplication, "help", Seq(c.name), expectedConsoleScenario = "")
      res should include (s"Command Name:\t${c.name}")
      res should include (s"Doc page:\t${c.docUrl}")
    }
  }

}
