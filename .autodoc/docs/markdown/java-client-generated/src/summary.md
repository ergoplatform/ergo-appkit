[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/java-client-generated/src)

The `.autodoc/docs/json/java-client-generated/src` folder contains essential components for interacting with the Ergo blockchain explorer API and the Ergo Platform REST API. These components handle JSON serialization and deserialization, formatting collections of strings, adding authentication information to HTTP requests, and managing communication with the Ergo REST API.

### Ergo Blockchain Explorer API

The `explorer` subfolder provides components for interacting with the Ergo blockchain explorer API. The `DefaultApi.java` interface offers methods for fetching balances, transactions, blocks, and tokens. For example:

```java
DefaultApi api = new DefaultApi();
String address = "9f4QF8AD1nQ3nJahQVkM6c5jmBvG4Df6MAzvp8uss6R4XNt8vGt";
ApiResponse<ConfirmedBalance> response = api.getApiV1AddressesP1BalanceConfirmed(address, 10);
```

The `ExplorerApiClient.java` class is a wrapper around the Retrofit library, providing a convenient way to interact with a RESTful API. The `JSON.java` class is responsible for creating and configuring a `Gson` object for JSON serialization and deserialization. The `StringUtil.java` class provides utility methods for working with arrays of strings.

The `auth` subfolder contains classes for adding authentication information to HTTP requests made by the Ergo Explorer client, such as `ApiKeyAuth` and `HttpBasicAuth`.

### Ergo Platform REST API

The `restapi` subfolder contains Java classes generated from the Ergo Platform REST API specification. These classes are used to interact with the Ergo blockchain through the REST API, allowing developers to easily integrate Ergo functionality into their Java applications.

The `ApiClient.java` class manages communication with the Ergo REST API, handling HTTP requests, authentication, and JSON serialization/deserialization. The `Configuration.java` class is used to configure the `ApiClient`. For example:

```java
Configuration config = new Configuration();
config.setApiKey("your_api_key");
config.setBasePath("https://api.ergoplatform.com");

ApiClient apiClient = new ApiClient(config);
```

To make API calls, you would use the `ApiClient` instance. For example, to get the balance of an address:

```java
String address = "9f4QF8AD1nQ3nJahQVkM6c8qiuyhM1i8Kgh8Dt6hP8Xf8gsgg5u";
ApiResponse<Balance> response = apiClient.invokeAPI("/addresses/" + address + "/balance", "GET", null, null, null, null, null, "application/json", null, new TypeToken<Balance>(){}.getType());

Balance balance = response.getData();
System.out.println("Balance: " + balance.getConfirmed().getNanoErgs());
```

This code snippet demonstrates how to configure the `ApiClient`, make an API call to get the balance of an address, and extract the balance information from the response.

Overall, this folder provides essential components for interacting with the Ergo blockchain explorer API and the Ergo Platform REST API, handling JSON serialization and deserialization, formatting collections of strings, adding authentication information to HTTP requests, and managing communication with the Ergo REST API.
