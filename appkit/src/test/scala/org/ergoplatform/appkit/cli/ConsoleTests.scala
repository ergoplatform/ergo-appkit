package org.ergoplatform.appkit.cli

import java.io.{InputStreamReader, BufferedReader, PrintStream}

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers}

class ConsoleTests extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with ConsoleTesting {
  val scenario = ConsoleScenario(Seq(
    WriteRead("Enter line 1> ", "input line 1"),
    WriteRead("Enter line 2> ", "input line 2"),
    WriteRead("You entered: input line 1input line 2\n", "")
  ))

  property("read input string") {
    testScenario(scenario) { console =>
      Example.process(console)
    }
  }

  property("parse scenario from text") {
    val text =
      s"""# first line comment (should be ignored)
       |Enter line 1> ::input line 1;
       |# second line comment
       |Enter line 2> ::input line 2;
       |You entered: input line 1input line 2${'\n'}::;
       |""".stripMargin

     val parsed = parseScenario(text)
     parsed shouldBe scenario

    testScenario(parsed) { console =>
      Example.process(console)
    }
  }
}

object Example {
  def main(args: Array[String]): Unit = {
    val in = new BufferedReader(new InputStreamReader(System.in))
    val out = new PrintStream(System.out)
    process(new TestConsole(in, out))
  }

  def process(console: Console) = {
    console.print("Enter line 1> ")
    val line1 = console.readLine()
    console.print("Enter line 2> ")
    val line2 = console.readLine()
    val res = line1 + line2
    console.println(s"You entered: $res")
  }
}




