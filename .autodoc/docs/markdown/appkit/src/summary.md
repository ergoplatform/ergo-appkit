[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/appkit/src)

The `.autodoc/docs/json/appkit/src` folder contains essential classes and utilities for interacting with the Ergo blockchain and creating command-line interface (CLI) tools. The folder is organized into two subfolders, `java` and `scala`, each providing implementations in their respective languages.

In the `java` subfolder, the `RestApiErgoClient` class is a key component, as it offers an implementation of the `ErgoClient` interface that communicates with an Ergo node using its REST API. This class simplifies the process of creating and configuring instances of the `ErgoClient` interface, allowing developers to easily interact with the Ergo blockchain and perform various operations. For example, to create an `ErgoClient` instance connected to a Testnet Ergo node, you can use the following code snippet:

```java
ApiConfig apiConfig = new ApiConfig("http://localhost:9053");
WalletConfig walletConfig = new WalletConfig("testnet", "password");
ErgoNodeConfig nodeConfig = new ErgoNodeConfig(apiConfig, walletConfig, NetworkType.TESTNET);
ErgoClient client = RestApiErgoClient.create(nodeConfig);
```

The `scala` subfolder contains the `org.ergoplatform.appkit` package, which provides a set of Scala classes and utilities for interacting with the Ergo blockchain. The main class in this package is `ColdErgoClient`, which allows developers to interact with the Ergo blockchain in a cold environment, where private keys are stored offline. This is useful for performing transactions and other operations on the blockchain without exposing sensitive information to the internet.

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

The `cli` subfolder in the `scala` folder contains a set of Scala files for creating command-line interface (CLI) tools for the Ergo platform. These files provide a framework for creating CLI applications, parsing command-line arguments, handling console interactions, and defining CLI options. An example of how to create a custom CLI application using these classes is shown below:

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

In summary, the `java` and `scala` subfolders in the `.autodoc/docs/json/appkit/src/main` folder provide essential classes and utilities for interacting with the Ergo blockchain, creating CLI tools, and defining custom commands. These classes can be used in larger projects to perform transactions and other operations on the Ergo blockchain, as well as to create custom CLI applications and commands for interacting with the blockchain.
