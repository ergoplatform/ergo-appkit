package org.ergoplatform.appkit.cli

import org.apache.commons.io.FileUtils
import scalan.util.FileUtil
import org.ergoplatform.appkit.JavaHelpers._

import java.util.{List => JList}
import java.lang.{String => JString}
import org.ergoplatform.appkit.FileMockedErgoClient
import org.ergoplatform.sdk.JavaHelpers.UniversalConverter
import org.scalatest.matchers.should.Matchers

import java.nio.charset.Charset

trait CommandsTesting extends ConsoleTesting { self: Matchers =>

  def responsesDir: String

  def loadNodeResponse(name: String) = {
    val file = FileUtil.file(s"$responsesDir/node_responses/$name")
    FileUtils.readFileToString(file, Charset.defaultCharset())
  }
  def loadExplorerResponse(name: String) = {
    val file = FileUtil.file(s"$responsesDir/explorer_responses/$name")
    FileUtils.readFileToString(file, Charset.defaultCharset())
  }

  def testConfigFile: String

  case class MockData(nodeResponses: Seq[String] = Nil, explorerResponses: Seq[String] = Nil)
  object MockData {
    def empty = MockData()
  }

  def runCliApp(cliApp: CliApplication,
                console: Console,
                name: String,
                args: Seq[String],
                data: MockData = MockData.empty) = {
    cliApp.run(name +: (Seq(ConfigOption.cmdText, testConfigFile) ++ args), console, {
      ctx => {
        val nrs = IndexedSeq(
          loadNodeResponse("response_NodeInfo.json"),
          loadNodeResponse("response_LastHeaders.json")) ++ data.nodeResponses
        val ers: IndexedSeq[String] = data.explorerResponses.toIndexedSeq
        new FileMockedErgoClient(
          nrs.convertTo[JList[JString]],
          ers.convertTo[JList[JString]],
          false)
      }
    })
  }

  /** Run the given command with expected console scenario (print and read operations)
    * @param name the command
    * @param args arguments of command line
    * @param expectedConsoleScenario input and output operations with the console (see parseScenario)
    */
  def runCommand(cliApp: CliApplication,
                 name: String,
                 args: Seq[String],
                 expectedConsoleScenario: String,
                 data: MockData = MockData.empty): String = {
    val consoleOps = parseScenario(expectedConsoleScenario)
    runScenario(consoleOps) { console =>
      runCliApp(cliApp, console, name, args, data)
    }
  }

//  def runCommandWithCtxStubber(cliApp: CliApplication,
//                               name: String,
//                               args: Seq[String],
//                               expectedConsoleScenario: String,
//                               data: MockData = MockData.empty,
//                               ctxStubber: BlockchainContext => Unit): String = {
//    val consoleOps = parseScenario(expectedConsoleScenario)
//    runScenario(consoleOps) { console =>
//      cliApp.run(name +: (Seq(ConfigOption.cmdText, testConfigFile) ++ args), console, {
//        ctx => {
//          val nrs = IndexedSeq(
//            loadNodeResponse("response_NodeInfo.json"),
//            loadNodeResponse("response_LastHeaders.json")) ++ data.nodeResponses
//          val ers: IndexedSeq[String] = data.explorerResponses.toIndexedSeq
//          new FileMockedErgoClientWithStubbedCtx(nrs.convertTo[JList[JString]],
//            ers.convertTo[JList[JString]],
//            ctx => { val spiedCtx = spy(ctx); ctxStubber(spiedCtx); spiedCtx })
//        }
//      })
//    }
//  }

  def testCommand(cliApp: CliApplication,
                  name: String,
                  args: Seq[String],
                  expectedConsoleScenario: String,
                  data: MockData = MockData.empty): Unit = {
    val consoleOps = parseScenario(expectedConsoleScenario)
    testScenario(consoleOps) { console =>
      runCliApp(cliApp, console, name, args, data)
    }
    ()
  }

}
