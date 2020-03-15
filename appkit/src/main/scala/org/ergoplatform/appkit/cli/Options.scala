package org.ergoplatform.appkit.cli

import org.ergoplatform.appkit.commands.{IntPType, FilePType, PType, BooleanPType}

/** Represents CLI option description.
  *
  * Options can be used in command line to specify parameters to be used by the command during its operation.
  * The command line passed to CLI application is split into parts (args) by whitespaces, so that option name and
  * value are represented by two consecutive parts.
  *
  * In the command line an option is given using the following syntax `--optionName optionValue`,
  * where the option name should be prefixed with `--` in the command line, and option value is given by
  * the next part of the command line.
  *
  * If a CmdOption instance has [[CmdOption.isFlag]] set to `true` then such option doesn't have `optionValue`
  * part and the option is interpreted as Boolean value (`true` if it is present, `false` otherwise)
  *
  * @param name The name of this option excluding prefix `--` (should not contain whitespaces) (Examples: `conf` for [[ConfigOption]] and `dry-run` for [[DryRunOption]])
  * @param description is a user readable description of the option
  * @param isFlag is set to true if the option is a <i>flag option</i> which doesn't have `optionValue`
  *               part (e.g. `--ni` or `--dry-run`)
  */
case class CmdOption(name: String, tpe: PType, description: String, isFlag: Boolean = false) {
  import CmdOption._
  /** The text of the command line with the name of this option. */
  def cmdText: String = Prefix + name
  /** The string printed for this option in the Usage Help output. */
  def helpString: String = s"  $cmdText\n\t$description"
}
object CmdOption {
  /** Prefix used in command line to denote option name. Every command line part which starts with
    * Prefix is interpreted as an option name.
    */
  final val Prefix: String = "--"
  
  /** Options supported by this application */
  lazy val options: Seq[CmdOption] = Array(ConfigOption, DryRunOption, PrintJsonOption, LimitListOption)
}

/** String option to specify path to a configuration file. The path is relative to current working directory.
  * The file has json content corresponding to `ErgoToolConfig` class.
  */
object ConfigOption extends CmdOption(
  "conf", FilePType,
  "configuration file path relative to the local directory (Example: `--conf ergo_tool.json`")

/** Flag option to prevent the command to perform actual operation and instead forces it to report
  * planned actions. It is useful for commands which perform some ''real world'' effects such as sending a
  * transaction to the blockchain (see [[SendCmd]]).
  *
  * The reported output of a command under this option depends on the command implementation.
  * The commands that do real world changes are required to respect this option and guarantee that no-change
  * execution when this option is included in command line.
  */
object DryRunOption extends CmdOption(
  "dry-run", BooleanPType,
  "Forces the command to report what will be done by the operation without performing the actual operation.",
  true)

/** Flag option to force commands to print objects as formatted json instead of rows in table.
  *
  * The reported output of a command under this option depends on the command implementation.
  */
object PrintJsonOption extends CmdOption(
  "print-json", BooleanPType,
  "Forces the commands to print json instead of table rows.",
  true)

/** Specifies a number of items in the output list.
  *
  * Usage of this option depends on the command (see command help for details).
  */
object LimitListOption extends CmdOption(
  "limit-list", IntPType,
  "Specifies a number of items in the output list.")

