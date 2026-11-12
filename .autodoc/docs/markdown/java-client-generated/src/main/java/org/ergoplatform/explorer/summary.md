[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/java-client-generated/src/main/java/org/ergoplatform/explorer)

The `.autodoc/docs/json/java-client-generated/src/main/java/org/ergoplatform/explorer` folder contains essential components for interacting with the Ergo blockchain explorer API, handling JSON serialization and deserialization, formatting collections of strings, and adding authentication information to HTTP requests.

`CollectionFormats.java` provides classes for formatting collections of strings into CSV, SSV, TSV, and PIPES formats. These classes can be used to format data for different parts of the application. For example:

```java
List<String> params = Arrays.asList("param1", "param2", "param3");
CSVParams csvParams = new CSVParams(params);
String formattedParams = csvParams.toString();
```

`DefaultApi.java` is an interface that provides methods for interacting with the Ergo blockchain explorer API, such as fetching balances, transactions, blocks, and tokens. It serves as a bridge between the Ergo blockchain explorer API and the ergo-appkit project. For example:

```java
DefaultApi api = new DefaultApi();
String address = "9f4QF8AD1nQ3nJahQVkM6c5jmBvG4Df6MAzvp8uss6R4XNt8vGt";
ApiResponse<ConfirmedBalance> response = api.getApiV1AddressesP1BalanceConfirmed(address, 10);
```

`ExplorerApiClient.java` is a wrapper around the Retrofit library, providing a convenient way to interact with a RESTful API. It offers methods to configure the Retrofit instance and handle deserialization failures. For example:

```java
ExplorerApiClient client = new ExplorerApiClient();
client.createDefaultAdapter();
DefaultApi api = client.createService(DefaultApi.class);
```

`JSON.java` is responsible for creating and configuring a `Gson` object, which is used to serialize and deserialize Java objects to and from JSON. It contains nested classes that extend `TypeAdapter` to customize the serialization and deserialization of specific Java types. For example:

```java
Gson gson = new JSON().createGson();
String jsonString = "{\"name\":\"John\",\"age\":30}";
Person person = gson.fromJson(jsonString, Person.class);
```

`StringUtil.java` provides utility methods for working with arrays of strings, such as `containsIgnoreCase` and `join`. These methods can be used throughout the project to simplify string manipulation tasks. For example:

```java
String[] names = {"Alice", "Bob", "Charlie"};
String commaSeparatedNames = StringUtil.join(names, ", ");
```

The `auth` subfolder contains classes for adding authentication information to HTTP requests made by the Ergo Explorer client. `ApiKeyAuth` adds an API key to requests, while `HttpBasicAuth` adds HTTP Basic Authentication headers. These classes can be used with an `OkHttpClient` instance to ensure that all requests include the necessary authentication information. For example:

```java
ApiKeyAuth apiKeyAuth = new ApiKeyAuth("header", "X-Api-Key");
apiKeyAuth.setApiKey("my-api-key");

OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(apiKeyAuth)
        .build();
```

Overall, this folder provides essential components for interacting with the Ergo blockchain explorer API, handling JSON serialization and deserialization, formatting collections of strings, and adding authentication information to HTTP requests.
