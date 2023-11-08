[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/java-client-generated/src/main/java/org/ergoplatform/restapi/client/ApiClient.java)

The `ApiClient` class is a utility class that provides methods for creating and configuring a Retrofit client. It is used to make HTTP requests to a RESTful API. The class is responsible for creating a `Retrofit` instance, which is used to create a service that can be used to make HTTP requests to the API. The `ApiClient` class provides methods for setting up authentication, adding interceptors, and configuring the `Retrofit` instance.

The `ApiClient` class has several constructors that allow for different types of authentication to be used. The `createDefaultAdapter()` method is used to create a default `Retrofit` instance with a `Gson` converter factory and a `Scalars` converter factory. The `createService()` method is used to create a service that can be used to make HTTP requests to the API.

The `GsonCustomConverterFactory` class is a custom converter factory that is used to handle deserialization errors. If the deserialization fails due to a `JsonParseException` and the expected type is a `String`, then the `GsonResponseBodyConverterToString` class is used to return the body string.

Overall, the `ApiClient` class is an important utility class that provides a simple and flexible way to make HTTP requests to a RESTful API. It is used extensively throughout the `ergo-appkit` project to interact with the Ergo blockchain. Below is an example of how the `ApiClient` class can be used to create a service that can be used to make HTTP requests to the Ergo blockchain API:

```
ApiClient apiClient = new ApiClient("https://api.ergoplatform.com");
MyApiService apiService = apiClient.createService(MyApiService.class);
```
## Questions: 
 1. What is the purpose of the `ApiClient` class?
- The `ApiClient` class is used to create a Retrofit client that can be used to make HTTP requests to a REST API.

2. What authentication methods are supported by the `ApiClient` class?
- The `ApiClient` class supports API key authentication and basic authentication.

3. What is the purpose of the `GsonCustomConverterFactory` class?
- The `GsonCustomConverterFactory` class is used to customize the Gson converter factory used by Retrofit to handle response bodies. It provides a custom implementation of the `responseBodyConverter` method that returns a `GsonResponseBodyConverterToString` instance if the expected type is `String`.