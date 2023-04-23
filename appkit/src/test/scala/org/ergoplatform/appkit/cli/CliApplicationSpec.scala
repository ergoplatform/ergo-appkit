package org.ergoplatform.appkit.cli

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec

object TestCliApplication extends CliApplication

class CliApplicationSpec
    extends AnyPropSpec
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
