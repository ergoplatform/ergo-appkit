[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/appkit/src/main/scala)

The `.autodoc/docs/json/appkit/src/main/scala` folder contains the `org.ergoplatform.appkit` package, which provides a set of Scala classes and utilities for interacting with the Ergo blockchain. The main class in this package is `ColdErgoClient`, which allows developers to interact with the Ergo blockchain in a cold environment, where private keys are stored offline. This is useful for performing transactions and other operations on the blockchain without exposing sensitive information to the internet.

Example usage of `ColdErgoClient`:

```scala
val client = new ColdErgoClient(NetworkType.MAINNET, new BlockchainParameters("localhost", 9052))
val result = client.execute(ctx => {
  val wallet = ctx.getWallet
  val balance = wallet.getBalance
  balance
})
println(result)
```

The `cli` subfolder contains a set of Scala files for creating command-line interface (CLI) tools for the Ergo platform. These files provide a framework for creating CLI applications, parsing command-line arguments, handling console interactions, and defining CLI options. An example of how to create a custom CLI application using these classes is shown below:

```scala
import org.ergoplatform.appkit.cli._

object MyApp extends CliApplication {
  def main(args: Array[String]): Unit = {
    val console = new MainConsole
    val clientFactory = () => new ErgoClient(...)
    run(args, console, clientFactory)
  }

  override def commands: Array[CmdDescriptor] = Array(
    HelpCmd.descriptor,
    // other command descriptors
  )
}
```

The `commands` subfolder contains the `Commands.scala` file, which provides a framework for creating and executing commands in the ErgoTool application. It offers a set of classes and traits that can be extended or implemented to create custom commands for interacting with the Ergo blockchain, handling command parameters, parsing command line arguments, and managing exceptions. An example of how to create a custom command using the provided classes and traits is shown below:

```scala
class MyCommand extends Cmd with RunWithErgoClient {
  override def name: String = "my-command"
  override def ergoClient: ErgoClient = ...
  // Implement the runWithClient method
  override def runWithClient(ergoClient: ErgoClient, appCtx: AppContext): Unit = {
    // Your command logic here
  }
}

object MyCommand extends CmdDescriptor {
  override val name: String = "my-command"
  override val description: String = "A custom command for ErgoTool"
  override val syntax: String = "my-command <param1> <param2>"

  override def createCmd(appCtx: AppContext): Cmd = new MyCommand
  override def parseArgs(args: Seq[String]): Seq[CmdParameter] = ...
}
```

In summary, the `org.ergoplatform.appkit` package and its subfolders provide a set of Scala classes and utilities for interacting with the Ergo blockchain, creating CLI tools, and defining custom commands. These classes can be used in larger projects to perform transactions and other operations on the Ergo blockchain, as well as to create custom CLI applications and commands for interacting with the blockchain.
