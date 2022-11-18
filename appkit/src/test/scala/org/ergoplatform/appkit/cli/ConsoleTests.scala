package org.ergoplatform.appkit.cli

import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.io.{PrintStream, InputStreamReader, BufferedReader}

class ConsoleTests extends AnyPropSpec with Matchers with ScalaCheckPropertyChecks with ConsoleTesting {
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




