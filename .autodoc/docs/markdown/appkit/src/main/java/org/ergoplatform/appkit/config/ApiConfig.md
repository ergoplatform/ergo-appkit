[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/appkit/src/main/java/org/ergoplatform/appkit/config/ApiConfig.java)

The `ApiConfig` class in the `org.ergoplatform.appkit.config` package is responsible for storing the connection parameters for the Ergo node API. It has two private instance variables, `apiUrl` and `apiKey`, which are accessed through public getter methods.

The `getApiUrl()` method returns the URL of the Ergo node API endpoint. This URL is used to connect to the Ergo node and send requests to it. An example usage of this method would be to retrieve the current block height of the Ergo blockchain:

```java
ApiConfig apiConfig = new ApiConfig();
apiConfig.setApiUrl("http://localhost:9052");
ErgoClient ergoClient = RestApiErgoClient.create(apiConfig);
int currentHeight = ergoClient.execute(ctx -> ctx.getHeaders().getHeight());
```

In this example, an instance of `ApiConfig` is created and its `apiUrl` variable is set to the URL of the Ergo node API endpoint. An `ErgoClient` is then created using the `RestApiErgoClient.create()` method, which takes an instance of `ApiConfig` as an argument. Finally, the `execute()` method is called on the `ErgoClient` instance to retrieve the current block height of the Ergo blockchain.

The `getApiKey()` method returns the API key used for authentication with the Ergo node API. This key is a secret key whose hash was used in the Ergo node configuration. An example usage of this method would be to authenticate with the Ergo node API:

```java
ApiConfig apiConfig = new ApiConfig();
apiConfig.setApiUrl("http://localhost:9052");
apiConfig.setApiKey("mySecretApiKey");
ErgoClient ergoClient = RestApiErgoClient.create(apiConfig);
ergoClient.execute(ctx -> ctx.getBoxesUnspent());
```

In this example, an instance of `ApiConfig` is created and its `apiUrl` and `apiKey` variables are set to the URL of the Ergo node API endpoint and the secret API key, respectively. An `ErgoClient` is then created using the `RestApiErgoClient.create()` method, which takes an instance of `ApiConfig` as an argument. Finally, the `execute()` method is called on the `ErgoClient` instance to retrieve the unspent boxes on the Ergo blockchain, using the authenticated API key.
## Questions: 
 1. What is the purpose of this class?
   This class defines the connection parameters for the Ergo node API, including the API URL and API key for authentication.

2. How are the API URL and API key set?
   The values for the API URL and API key are set through the private instance variables `apiUrl` and `apiKey`, respectively.

3. Can the API URL and API key be modified?
   It is not clear from this code whether the API URL and API key can be modified after they are initially set. The class only provides getter methods for these values, indicating that they may be read-only.