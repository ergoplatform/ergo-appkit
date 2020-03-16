package org.ergoplatform.appkit.cli

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{ErgoClient, NetworkType}

/** Application execution context. Contains all the data necessary to parse and execute command. */
case class AppContext(
     /** CLI application which has created this context */
     cliApp: CliApplication,
     /** Arguments of command line passed to ErgoTool.main */
     commandLineArgs: Seq[String],
     /** Console interface to be used during command execution */
     console: Console,
     /** Options parsed from command line */
     cmdOptions: Map[String, String],
     /** Command name to execute (passed in command line). */
     cmdName: String,
     /** Command args taken from the command line (excluding command name) */
     cmdArgs: Seq[String],
     /** Tool configuration read from the file (either default or specified by --conf option */
     toolConf: ErgoToolConfig,
     /** Factory method which is used to create ErgoClient instance if and when it is needed */
     clientFactory: AppContext => ErgoClient,
     /** Parsed and instantiated command parameters (instances of the corresponding type) */
     cmdParameters: Seq[Any] = Nil
 ) {
  /** Url of the Ergo node API end point
    */
  def apiUrl: String = toolConf.getNode.getNodeApi.getApiUrl

  /** ApiKey which is used for Ergo node API authentication.
    * This is a secret key whose hash was used in Ergo node config.
    */
  def apiKey: String = toolConf.getNode.getNodeApi.getApiKey

  /** Returns expected network type (Mainnet or Testnet)
    */
  def networkType: NetworkType = toolConf.getNode.getNetworkType

  /** Returns true if [[DryRunOption]] is defined in command line. */
  def isDryRun: Boolean = cmdOptions.contains(DryRunOption.name)

  /** Returns true if [[PrintJsonOption]] is defined in command line. */
  def isPrintJson: Boolean = cmdOptions.contains(PrintJsonOption.name)

  /** Attach parameters to this context. */
  def withCmdParameters(params: Seq[Any]): AppContext = this.copy(cmdParameters = params)
}
