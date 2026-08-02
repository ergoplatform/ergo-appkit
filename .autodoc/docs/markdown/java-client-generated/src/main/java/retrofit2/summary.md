[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/java-client-generated/src/main/java/retrofit2)

The `RetrofitUtil` class in the `ergo-appkit` project is a utility class that provides a way to bypass the usual proxy generation in Retrofit, which doesn't work under Graal native-image. Retrofit is a type-safe HTTP client for Android and Java, and this class allows the invocation of service methods in Retrofit.

The main functionality of this class is provided by the `invokeServiceMethod` method, which takes in a `Retrofit` instance, a `Method` object representing the service method to be invoked, and an array of arguments to be passed to the method. It then calls the `loadServiceMethod` method on the `Retrofit` instance to obtain a `ServiceMethod` object, which is used to create a `Call` object that can be used to make the HTTP request.

This class is likely used in the larger project to facilitate communication between the ErgoNodeFacade in the `lib-impl` module and the Ergo blockchain network. The `invokeServiceMethod` method can be used to make HTTP requests to the Ergo blockchain network using Retrofit, which provides a convenient and type-safe way to interact with the network.

Here is an example of how this class might be used in the larger project:

```java
// create a Retrofit instance
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://api.ergoplatform.com")
    .build();

// get a reference to the ErgoNodeFacade service interface
ErgoNodeFacade ergoNodeFacade = retrofit.create(ErgoNodeFacade.class);

// invoke a service method using RetrofitUtil
Call<BlockHeader> call = RetrofitUtil.invokeServiceMethod(retrofit, 
    ErgoNodeFacade.class.getMethod("getBlockHeaderById", String.class), 
    new Object[] { "12345" });

// execute the HTTP request and get the response
Response<BlockHeader> response = call.execute();
BlockHeader blockHeader = response.body();
```

In this example, we create a `Retrofit` instance with a base URL of `https://api.ergoplatform.com`. We then use the `create` method to obtain a reference to the `ErgoNodeFacade` service interface, which defines methods for interacting with the Ergo blockchain network. We then use `RetrofitUtil.invokeServiceMethod` to invoke the `getBlockHeaderById` method on the `ErgoNodeFacade` interface, passing in the ID of the block header we want to retrieve. Finally, we execute the HTTP request and get the response, which contains the block header.
