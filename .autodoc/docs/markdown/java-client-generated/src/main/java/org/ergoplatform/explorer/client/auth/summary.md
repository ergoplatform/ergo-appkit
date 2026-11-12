[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/java-client-generated/src/main/java/org/ergoplatform/explorer/client/auth)

The `org.ergoplatform.explorer.client.auth` package contains two classes, `ApiKeyAuth` and `HttpBasicAuth`, which are responsible for adding authentication information to HTTP requests made by the Ergo Explorer client. Both classes implement the `Interceptor` interface from the OkHttp library, allowing them to intercept and modify HTTP requests and responses.

`ApiKeyAuth` is used to add an API key to requests. It takes two parameters in its constructor: `location` and `paramName`. `location` specifies where the API key should be added - either as a query parameter or a header. `paramName` specifies the name of the query parameter or header that should contain the API key. The API key itself is stored in the `apiKey` field, which can be set using the `setApiKey` method. The `intercept` method adds the API key to the request based on the `location` value.

Example usage:

```java
ApiKeyAuth apiKeyAuth = new ApiKeyAuth("header", "X-Api-Key");
apiKeyAuth.setApiKey("my-api-key");

OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(apiKeyAuth)
        .build();

Request request = new Request.Builder()
        .url("https://api.ergoplatform.com")
        .build();

Response response = client.newCall(request).execute();
```

`HttpBasicAuth` is used to add HTTP Basic Authentication headers to requests. It has three instance variables: `username`, `password`, and `credentials`. The `intercept` method retrieves the original request and checks if it already has an Authorization header. If not, it creates a new request with the Authorization header added using the `Credentials.basic` method from OkHttp, which takes the `username` and `password` instance variables and returns a string in the format "Basic [base64-encoded username:password]". The new request is then returned using the `chain.proceed` method.

Example usage:

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new HttpBasicAuth())
        .build();

Request request = new Request.Builder()
        .url("https://example.com/api")
        .build();

Response response = client.newCall(request).execute();
```

In summary, the `org.ergoplatform.explorer.client.auth` package provides two classes for adding authentication information to HTTP requests made by the Ergo Explorer client. These classes can be used in conjunction with an `OkHttpClient` instance to ensure that all requests include the necessary authentication information.
