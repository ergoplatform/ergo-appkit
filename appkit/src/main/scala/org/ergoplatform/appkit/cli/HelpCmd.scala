package org.ergoplatform.appkit.ergotool

import org.ergoplatform.appkit.cli.AppContext
import org.ergoplatform.appkit.commands.{CmdParameter, CmdException, CommandNamePType, Cmd, CmdDescriptor}
import org.ergoplatform.appkit.config.ErgoToolConfig

/** Prints usage help for the given command name.
  *
  * @param askedCmd command name which usage help should be printed
  * @throws CmdException if `askedCmd` is not valid command name
  * @see [[HelpCmd$]] descriptor of the `help` command
  */
case class HelpCmd(toolConf: ErgoToolConfig, name: String, askedCmd: String) extends Cmd {
  override def run(ctx: AppContext): Unit = {
    ctx.cliApp.commandsMap.get(askedCmd) match {
      case Some(cmd) => cmd.printUsage(ctx.console)
      case _ => error(s"Help not found. Unknown command: $askedCmd")
    }
  }
}
/** Descriptor and parser of the `help` command. */
object HelpCmd extends CmdDescriptor(
  name = "help", cmdParamSyntax = "<commandName>",
  description = "prints usage help for a command") {

  override val parameters: Seq[CmdParameter] = Array(
    CmdParameter("commandName", CommandNamePType, "command name which usage help should be printed")
  )

  override def createCmd(ctx: AppContext): Cmd = {
    val Seq(askedCmd: String) = ctx.cmdParameters
    HelpCmd(ctx.toolConf, name, askedCmd)
  }
}
