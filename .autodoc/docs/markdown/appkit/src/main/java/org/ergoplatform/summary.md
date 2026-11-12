[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/appkit/src/main/java/org/ergoplatform)

The `RestApiErgoClient` class in the `org.ergoplatform.appkit` package is a crucial component of the ergo-appkit project, as it provides an implementation of the `ErgoClient` interface that communicates with an Ergo node using its REST API. This class simplifies the process of creating and configuring instances of the `ErgoClient` interface, allowing developers to easily interact with the Ergo blockchain and perform various operations.

For instance, to create an `ErgoClient` instance connected to a Testnet Ergo node, you can use the following code snippet:

```java
ApiConfig apiConfig = new ApiConfig("http://localhost:9053");
WalletConfig walletConfig = new WalletConfig("testnet", "password");
ErgoNodeConfig nodeConfig = new ErgoNodeConfig(apiConfig, walletConfig, NetworkType.TESTNET);
ErgoClient client = RestApiErgoClient.create(nodeConfig);
```

The `RestApiErgoClient` class also offers several static factory methods, such as `createWithoutExplorer`, `create`, and `createWithHttpClientBuilder`, enabling developers to create `ErgoClient` instances with different configurations, like connecting to a node without an explorer connection or using a custom HTTP client builder.

To perform operations on the Ergo blockchain, the `execute` method in the `RestApiErgoClient` class is used. This method accepts a `Function` that operates on a `BlockchainContext` and returns a result of type `T`. For example, to retrieve the current block height, you can use the following code:

```java
int currentHeight = ergoClient.execute(ctx -> ctx.getHeaders().getHeight());
```

Additionally, the `org.ergoplatform.appkit.config` package contains classes responsible for managing the configuration of the Ergo node, wallet, and tool parameters. These classes are essential for setting up and interacting with the Ergo blockchain using the `ErgoClient` class. For example, the `ApiConfig` class stores the connection parameters for the Ergo node API, while the `ErgoNodeConfig` class defines the parameters of an Ergo node, including the API configuration, wallet configuration, and network type.

In conclusion, the `RestApiErgoClient` class and the classes in the `org.ergoplatform.appkit.config` package play a vital role in the ergo-appkit project by providing a convenient way to create instances of the `ErgoClient` interface and manage the configuration of the Ergo node, wallet, and tool parameters. They enable developers to easily interact with the Ergo blockchain and perform various operations, such as retrieving the current block height or sending transactions.
