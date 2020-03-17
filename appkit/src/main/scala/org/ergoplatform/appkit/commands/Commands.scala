package org.ergoplatform.appkit.commands

import java.io.File

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.cli.{Console, AppContext, CmdLineParser}
import org.ergoplatform.appkit.cli.Console.readNewPassword

/** Base class for all commands which can be executed by ErgoTool.
  * Inherit this class to implement a new command.
  * @see [[RunWithErgoClient]] if your command need to communicate with blockchain.
  */
abstract class Cmd {
  /** Returns current tool configuration parameters */
  def toolConf: ErgoToolConfig

  /** Returns the name of this command (Example: `send`, `mnemonic` etc.) */
  def name: String

  /** Returns the url of the Ergo blockchain node used to communicate with the network. */
  def apiUrl: String = toolConf.getNode.getNodeApi.getApiUrl

  /** ApiKey which is used for Ergo node API authentication.
    * This is a secret key whose hash was used in Ergo node config.
    * This is only necessary to communicate to the protected parts of node API.
    */
  def apiKey: String = toolConf.getNode.getNodeApi.getApiKey

  /** Returns the network type (MAINNET or TESTNET) [[ErgoTool]] is expected to communicate.
    * This parameter should correspond to the real network type of the node pointed to by [[apiUrl]].
    */
  def networkType: NetworkType = toolConf.getNode.getNetworkType

  /** Runs this command using given [[AppContext]].
 *
    * @param ctx context information of this command execution collected from command line,
    * configuration file etc.
    * @throws CmdException when command execution fails
    */
  def run(ctx: AppContext): Unit

  /** Helper method to throw a new [[CmdException]] from this command. */
  def error(msg: String) = throw CmdException(msg, this)

  /** Helper method to log the result of the step execution to a console */
  def loggedStep[T](msg: String, console: Console)(step: => T): T = {
    console.print(msg + "...")
    val res = step
    val status = if (res != null) "Ok" else "Error"
    console.println(s" $status")
    res
  }
}

/** This trait can be used to implement commands which need to communicate with Ergo blockchain.
  * The default [[Cmd.run]] method is implemented and the new method with additional `ErgoClient`
  * parameter is declared, which is called from the default implementation.
  * To implement new command mix-in this train and implement [[RunWithErgoClient.runWithClient]] method.
  */
trait RunWithErgoClient extends Cmd {
  override def run(ctx: AppContext): Unit = {
    val ergoClient = ctx.clientFactory(ctx)
    runWithClient(ergoClient, ctx)
  }

  /** Called from [[run]] method with ErgoClient instance ready for Ergo blockchain communication. */
  def runWithClient(ergoClient: ErgoClient, ctx: AppContext): Unit
}

sealed abstract class PType {
}
case object NanoErgPType extends PType {
}

case object FilePType extends PType {
}

case object FilePathPType extends PType {
}

case object DirPathPType extends PType {
}

case object AddressPType extends PType {
}

case object ErgoIdPType extends PType {
}

case object NetworkPType extends PType {
}

case object CommandNamePType extends PType {
}

case object SecretStringPType extends PType {
}

case object IntPType extends PType {
}

case object LongPType extends PType {
}

case object BooleanPType extends PType {
}

case object StringPType extends PType {
}

/** List of possible enum values (name and the value itself). */
case class EnumPType(values: Seq[(String, Any)]) extends PType {
  def getByName(name: String): Option[Any] = values.collectFirst { case (n, v) if n == name => v }
}

/** Command parameter descriptor.
  *
  * @param name            parameter name
  * @param displayName     parameter name suitable for UI (e.g. name: "ergAmount", displayName: "Amount of ERGs")
  * @param tpe             type of the object which should be created from command line parameter string
  * @param description     description of the command parameter
  * @param defaultValue    the string value which will be used when parameter is missing in the command line
  * @param interactiveInput Some(producer) when parameter is entered interactively, i.e. it is not parsed from the command line
  * @param argParser       Optional custom parser of the parameter, if defined should be used instead
  *                        of the default parser defined for the type `tpe`.
  */
case class CmdParameter(
    name: String,
    displayName: String,
    tpe: PType,
    description: String,
    defaultValue: Option[String],
    interactiveInput: Option[CmdArgInput],
    argParser: Option[CmdArgParser]) {
}
object CmdParameter {
  /** Construct parameter with default `displayName` which is equal `name`. */
  def apply(name: String,
            tpe: PType,
            description: String,
            defaultValue: Option[String] = None,
            interactiveInput: Option[CmdArgInput] = None): CmdParameter =
    CmdParameter(name, name, tpe, description, defaultValue, interactiveInput, None)
}

/** Base class for all Cmd descriptors (usually companion objects)
 */
abstract class CmdDescriptor(
     /** Command name used in command line. */
     val name: String,
     /** Specifies parameters syntax for this command. */
     val cmdParamSyntax: String,
     /** Human readable description of the command. Used in Usage Help output. */
     val description: String
     ) {

  /** Returns the descriptors of parameters of the command. */
  def parameters: Seq[CmdParameter] = Nil

  val BaseDocUrl = "https://ergoplatform.github.io/ergo-tool/api"

  /** Url of the ScalaDoc for this command. */
  def docUrl: String = {
    val classPath = this.getClass.getName.replace('.', '/').stripSuffix("$")
    s"$BaseDocUrl/$classPath.html"
  }

  /** Creates a new command instance based on the given [[AppContext]]
    * @throws UsageException when the command cannot be parsed or the usage is not correct
    */
  def createCmd(ctx: AppContext): Cmd

  /** Called during command line parsing and instantiation of [[Cmd]] for execution.
    * This is the prefered method to throw an exception.
    */
  def error(msg: String) = {
    sys.error(s"Error executing command `$name`: $msg")
  }

  /** Can be used by concrete command descriptors to report usage errors. */
  protected def usageError(msg: String) = throw UsageException(msg, Some(this))

  def parseArgs(ctx: AppContext, args: Seq[String]): Seq[Any] = {
    var iArg = 0
    val rawParams = parameters.map { p =>
      p.interactiveInput match {
        case Some(producer) =>
          (p, producer.input(ctx, this, p))
        case _ =>
          if (iArg >= args.length)
            usageError(s"parameter '${p.name}' is not specified (run 'ergo-tool help ${this.name}' for usage help)")
          val arg = args(iArg)
          iArg += 1 // step to the next non-interactive parameter in command line
          (p, arg)
      }
    }
    rawParams.map {
      case (p, param) if p.interactiveInput.isDefined => param  // this is final value
      case (p, rawArg: String) if p.argParser.isDefined =>
        p.argParser.get.parse(this, p, rawArg)
      case (p, rawArg: String) =>
        // command line string needs further parsing according to the parameter descriptor
        // using default parser
        DefaultCmdArgParser.parse(this, p, rawArg)
    }
  }

  /** Outputs the usage help for this command to the given console */
  def printUsage(console: Console) = {
    val msg =
      s"""
        |Command Name:\t$name
        |Usage Syntax:\tergo-tool $name ${cmdParamSyntax}
        |Description:\t${description}
        |Doc page:\t$docUrl
        |""".stripMargin
    console.println(msg)
  }

}

/** Parser of the command line string. */
abstract class CmdArgParser {
  /** Parses the given raw string into a value of the parameter type.*/
  def parse(cmd: CmdDescriptor, p: CmdParameter, rawArg: String): Any
}

object DefaultCmdArgParser extends CmdArgParser {
  override def parse(cmd: CmdDescriptor, p: CmdParameter, rawArg: String): Any = {
    p.tpe match {
      case CommandNamePType | StringPType => rawArg
      case NetworkPType =>
        val networkType = CmdLineParser.parseNetwork(rawArg)
        networkType
      case SecretStringPType =>
        SecretString.create(rawArg)
      case IntPType => rawArg.toInt
      case LongPType => rawArg.toLong
      case AddressPType =>
        Address.create(rawArg)
      case ErgoIdPType =>
        ErgoId.create(rawArg)
      case FilePType =>
        val storageFile = new File(rawArg)
        if (!storageFile.exists())
          usageError(s"Specified file is not found: $storageFile", Some(cmd))
        storageFile
      case DirPathPType =>
        val file = new File(rawArg)
        if (!file.exists())
          usageError(s"Invalid parameter '${p.name}': directory '$file' doesn't exists.", Some(cmd))
        if (!file.isDirectory)
          usageError(s"Invalid parameter '${p.name}': '$file' is not directory.", Some(cmd))
        file.toPath
      case et: EnumPType =>
        val parser = new EnumParser(et)
        parser.parse(cmd, p, rawArg)
      case _ =>
        usageError(s"Unsupported parameter type: ${p.tpe}", Some(cmd))
    }
  }
}

class EnumParser(enum: EnumPType) extends CmdArgParser {
  override def parse(cmd: CmdDescriptor, p: CmdParameter, rawArg: String): Any =
    enum.getByName(rawArg) match {
      case Some(value) => value
      case _ => propErrorMsg
    }

  private def propErrorMsg = {
    usageError(
      s"Please specify one of the supported properties: ${enum.values.map(k => s"`${k._1}`").mkString(",")}",
      None)
  }
}

/** Input handler of [[CmdParameter]]. */
abstract class CmdArgInput {
  /** Called to input the given parameter `param`.
    * @param ctx       context of the running command invocation
    * @param parameter descriptor of the parameter
    * @return  the valued of the parameter which doesn't require further parsing
    */
  def input(ctx: AppContext, cmd: CmdDescriptor, parameter: CmdParameter): Any
}

object DefaultParameterInput extends CmdArgInput {
  override def input(ctx: AppContext, cmd: CmdDescriptor, parameter: CmdParameter): Any = {
    val rawStr = ctx.console.readLine(s"Enter ${parameter.displayName}>")
    parameter.tpe match {
      case SecretStringPType =>
        SecretString.create(rawStr)
      case _ =>
        DefaultCmdArgParser.parse(cmd, parameter, rawStr)
    }
  }
}

object PasswordInput extends CmdArgInput {
  override def input(ctx: AppContext, cmd: CmdDescriptor, param: CmdParameter): Any = {
    ctx.console.readPassword(s"${param.displayName}>")
  }
}

object NewPasswordInput extends CmdArgInput {
  override def input(ctx: AppContext, cmd: CmdDescriptor, param: CmdParameter): Any = {
    readNewPassword(s"${param.displayName}>", s"Repeat ${param.displayName}>")(ctx)
  }
}

/** Exception thrown by ErgoTool application when incorrect usage is detected.
  * @param message error message
  * @param cmdDescOpt  optional descriptor of the command which was incorrectly used
  */
case class UsageException(message: String, cmdDescOpt: Option[CmdDescriptor]) extends RuntimeException(message)

/** Exception thrown by ErgoTool application before or after command execution.
  * @see CmdException which should be thrown by commands during execution
  */
case class ErgoToolException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

/** Exception thrown by executing [[Cmd.run]], wrapping the cause if needed.
  */
case class CmdException(message: String, cmd: Cmd, cause: Throwable = null) extends RuntimeException(message, cause)

