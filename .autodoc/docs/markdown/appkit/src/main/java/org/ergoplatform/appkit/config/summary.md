[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/appkit/src/main/java/org/ergoplatform/appkit/config)

The `org.ergoplatform.appkit.config` package contains classes responsible for managing the configuration of the Ergo node, wallet, and tool parameters. These classes are essential for setting up and interacting with the Ergo blockchain using the `ErgoClient` class.

`ApiConfig.java` stores the connection parameters for the Ergo node API, including the API URL and the API key. This class is used to configure the `ErgoClient` for connecting to the Ergo node and sending requests. For example, to retrieve the current block height of the Ergo blockchain:

```java
ApiConfig apiConfig = new ApiConfig();
apiConfig.setApiUrl("http://localhost:9052");
ErgoClient ergoClient = RestApiErgoClient.create(apiConfig);
int currentHeight = ergoClient.execute(ctx -> ctx.getHeaders().getHeight());
```

`ErgoNodeConfig.java` defines the parameters of an Ergo node, including the API configuration, wallet configuration, and network type (Mainnet or Testnet). This class is used to configure an instance of the `ErgoClient` class. For example, to create an `ErgoClient` that connects to a Testnet Ergo node:

```java
ApiConfig apiConfig = new ApiConfig("http://localhost:9053");
WalletConfig walletConfig = new WalletConfig("testnet", "password");
ErgoNodeConfig nodeConfig = new ErgoNodeConfig(apiConfig, walletConfig, NetworkType.TESTNET);
ErgoClient client = ErgoClient.create(nodeConfig);
```

`ErgoToolConfig.java` manages the configuration parameters of the ErgoTool utility, including the Ergo node configuration and tool parameters. This class provides methods for loading the configuration from a file and accessing the configuration parameters. For example, to load the configuration from a file and get the node configuration:

```java
ErgoToolConfig config = ErgoToolConfig.load("config.json");
ErgoNodeConfig nodeConfig = config.getNode();
```

`ToolParameters.java` is a subclass of `HashMap` used to store key-value pairs of tool parameters. This class provides methods for storing, comparing, and printing tool parameters. For example, to compare two instances of `ToolParameters`:

```java
if (params1.equals(params2)) {
    // do something
}
```

`WalletConfig.java` provides parameters for working with a wallet, including the mnemonic, password, and mnemonic password used for generating keys. This class is used to configure the wallet used by the Ergo node. For example, to set the wallet configuration for an `ErgoNodeConfig` instance:

```java
ErgoNodeConfig nodeConfig = new ErgoNodeConfig(...);
WalletConfig walletConfig = new WalletConfig();
walletConfig.setMnemonic("example mnemonic");
walletConfig.setPassword("example password");
walletConfig.setMnemonicPassword("example mnemonic password");
nodeConfig.setWalletConfig(walletConfig);
```

In summary, the classes in the `org.ergoplatform.appkit.config` package are essential for configuring and interacting with the Ergo blockchain using the `ErgoClient` class. They provide a convenient way to manage the configuration of the Ergo node, wallet, and tool parameters.
