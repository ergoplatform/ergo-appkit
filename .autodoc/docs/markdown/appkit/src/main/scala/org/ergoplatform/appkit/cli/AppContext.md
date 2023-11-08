[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/appkit/src/main/scala/org/ergoplatform/appkit/cli/AppContext.scala)

The `AppContext` class is a data class that represents the context in which a command is executed in the Ergo platform. It contains all the necessary data to parse and execute a command. The purpose of this class is to provide a convenient way to pass around the context data between different parts of the application.

The class has several properties that represent different aspects of the context. The `cliApp` property represents the CLI application that created the context. The `commandLineArgs` property contains the arguments passed to the `ErgoTool.main` method. The `console` property represents the console interface to be used during command execution. The `cmdOptions` property contains the options parsed from the command line. The `cmdName` property represents the name of the command to be executed. The `cmdArgs` property contains the arguments taken from the command line (excluding the command name). The `toolConf` property represents the tool configuration read from the file. The `clientFactory` property is a factory method used to create an `ErgoClient` instance if and when it is needed. The `cmdParameters` property represents the parsed and instantiated command parameters.

The class also has several methods that provide convenient access to different aspects of the context. The `apiUrl` method returns the URL of the Ergo node API endpoint. The `apiKey` method returns the API key used for Ergo node API authentication. The `networkType` method returns the expected network type (Mainnet or Testnet). The `isDryRun` method returns true if the `DryRunOption` is defined in the command line. The `isPrintJson` method returns true if the `PrintJsonOption` is defined in the command line. The `withCmdParameters` method is used to attach parameters to the context.

Overall, the `AppContext` class provides a convenient way to pass around the context data between different parts of the application. It encapsulates all the necessary data and provides convenient methods to access and modify it. This class is an important part of the Ergo platform and is used extensively throughout the application. Here is an example of how the `AppContext` class can be used:

```scala
val appContext = AppContext(
  cliApp = myCliApp,
  commandLineArgs = Seq("--option1", "value1", "--option2", "value2"),
  console = myConsole,
  cmdOptions = Map("option1" -> "value1", "option2" -> "value2"),
  cmdName = "myCommand",
  cmdArgs = Seq("arg1", "arg2"),
  toolConf = myToolConfig,
  clientFactory = myClientFactory
)

val apiUrl = appContext.apiUrl
val apiKey = appContext.apiKey
val networkType = appContext.networkType
val isDryRun = appContext.isDryRun
val isPrintJson = appContext.isPrintJson

val newContext = appContext.withCmdParameters(Seq("param1", "param2"))
```
## Questions: 
 1. What is the purpose of the `AppContext` class?
- The `AppContext` class is an application execution context that contains all the data necessary to parse and execute a command.

2. What are the parameters of the `AppContext` class?
- The parameters of the `AppContext` class include the CLI application that created the context, the command line arguments passed to `ErgoTool.main`, a console interface to be used during command execution, options parsed from the command line, the command name to execute, command args taken from the command line (excluding command name), tool configuration read from the file, a factory method used to create an `ErgoClient` instance, and parsed and instantiated command parameters.

3. What are some of the methods available in the `AppContext` class?
- Some of the methods available in the `AppContext` class include `apiUrl`, which returns the URL of the Ergo node API endpoint, `apiKey`, which returns the API key used for Ergo node API authentication, `networkType`, which returns the expected network type (Mainnet or Testnet), `isDryRun`, which returns true if the `DryRunOption` is defined in the command line, and `isPrintJson`, which returns true if the `PrintJsonOption` is defined in the command line.