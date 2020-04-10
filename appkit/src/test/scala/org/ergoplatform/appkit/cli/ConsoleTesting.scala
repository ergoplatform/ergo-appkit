package org.ergoplatform.appkit.cli

import java.io.{StringReader, ByteArrayOutputStream, BufferedReader, PrintStream}

import org.scalatest.Matchers

trait ConsoleTesting { self: Matchers =>

  def prepareConsole(inputText: String): (Console, ByteArrayOutputStream) = {
    val in = new BufferedReader(new StringReader(inputText))
    val baos = new ByteArrayOutputStream()
    val out = new PrintStream(baos)
    (new TestConsole(in, out), baos)
  }

  case class WriteRead(write: String, read: String)
  case class ConsoleScenario(operations: Seq[WriteRead]) {
    /** Text passed to the input stream of the console */
    def getReadText: String = {
      operations.map(i => s"${i.read}\n").mkString("")
    }
    /** Text printed to the output stream of the console */
    def getWriteText: String = {
      operations.map(i => i.write).mkString("")
    }
  }

  /** Parses text into ConsoleScenario using simple syntax shown below.
   * Example:
   * <pre>
   * |# lines started from '#' are ignored
   * |# to separate output from input '::' combination is used
   * |this it output> ::this is input
   * |# each line should end with '\n' new line symbol
   * |# input may be empty
   * |output::
   * </pre>
   */
  def parseScenario(scenarioText: String): ConsoleScenario = {
    val withoutComments = scenarioText.split("\n")
        .filterNot(l => l.startsWith("#"))
        .mkString("\n") + "\n"
    val operations = withoutComments.split(";\n")
            .filterNot(l => l.isEmpty)
            .map(l => {
              val parts = l.split("::")
              WriteRead(parts(0), if (parts.length > 1) parts(1) else "")
            })
    ConsoleScenario(operations)
  }

  /** Runs the scenario and returns the printed output text. */
  def runScenario(scenario: ConsoleScenario)(action: Console => Unit): String = {
    val (console, out) = prepareConsole(scenario.getReadText)
    action(console)
    out.toString()
  }

  /** Runs the scenario and checks the actual output text against expected in the scenario. */
  def testScenario(scenario: ConsoleScenario)(action: Console => Unit) = {
    val res = runScenario(scenario)(action)
    val output = scenario.getWriteText
    output shouldBe res
  }

}
