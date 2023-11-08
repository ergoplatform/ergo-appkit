[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/appkit/src/main/java/org/ergoplatform/appkit)

The `RestApiErgoClient` class in the `org.ergoplatform.appkit` package is an implementation of the `ErgoClient` interface that uses the REST API of an Ergo node for communication. It provides a convenient way to create instances of the `ErgoClient` interface that are connected to a given node of the Ergo network, with or without an explorer connection. This class abstracts away the details of creating and configuring the necessary API clients and provides a simple interface for executing operations on the Ergo blockchain.

For example, to create an instance of `ErgoClient` connected to a Testnet Ergo node:

```java
ApiConfig apiConfig = new ApiConfig("http://localhost:9053");
WalletConfig walletConfig = new WalletConfig("testnet", "password");
ErgoNodeConfig nodeConfig = new ErgoNodeConfig(apiConfig, walletConfig, NetworkType.TESTNET);
ErgoClient client = RestApiErgoClient.create(nodeConfig);
```

The `RestApiErgoClient` class also provides several static factory methods for creating instances of the `ErgoClient` interface, such as `createWithoutExplorer`, `create`, and `createWithHttpClientBuilder`. These methods allow developers to create `ErgoClient` instances with different configurations, such as connecting to a node without an explorer connection or using a custom HTTP client builder.

The `execute` method in the `RestApiErgoClient` class takes in a `Function` that operates on a `BlockchainContext` and returns a result of type `T`. This method is used to perform operations on the Ergo blockchain, such as retrieving the current block height:

```java
int currentHeight = ergoClient.execute(ctx -> ctx.getHeaders().getHeight());
```

The `org.ergoplatform.appkit.config` package contains classes responsible for managing the configuration of the Ergo node, wallet, and tool parameters. These classes are essential for setting up and interacting with the Ergo blockchain using the `ErgoClient` class. For example, the `ApiConfig` class stores the connection parameters for the Ergo node API, while the `ErgoNodeConfig` class defines the parameters of an Ergo node, including the API configuration, wallet configuration, and network type.

In summary, the `RestApiErgoClient` class and the classes in the `org.ergoplatform.appkit.config` package provide a convenient way to create instances of the `ErgoClient` interface and manage the configuration of the Ergo node, wallet, and tool parameters. They enable developers to easily interact with the Ergo blockchain and perform various operations, such as retrieving the current block height or sending transactions.
