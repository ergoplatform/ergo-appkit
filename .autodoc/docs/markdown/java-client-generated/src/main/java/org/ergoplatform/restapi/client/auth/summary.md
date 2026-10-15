[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/java-client-generated/src/main/java/org/ergoplatform/restapi/client/auth)

The `org.ergoplatform.restapi.client.auth` package contains two classes, `ApiKeyAuth` and `HttpBasicAuth`, which are responsible for adding authentication information to outgoing HTTP requests made by the `OkHttpClient` instance. Both classes implement the `Interceptor` interface from the `okhttp3` library, allowing them to intercept and modify requests before they are sent to the server.

The `ApiKeyAuth` class is used for APIs that require an API key for authentication. It takes two arguments in its constructor: `location` and `paramName`. `location` specifies where the API key should be added - either as a query parameter or a header. `paramName` specifies the name of the query parameter or header that should be used to send the API key. The API key itself is stored as a private field in the class and can be set using the `setApiKey` method. The `intercept` method modifies the request by adding the API key to the specified location.

Example usage of `ApiKeyAuth`:

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new ApiKeyAuth("header", "X-Api-Key"))
        .build();

Request request = new Request.Builder()
        .url("https://api.example.com/some-endpoint")
        .build();

Response response = client.newCall(request).execute();
```

The `HttpBasicAuth` class is used for APIs that require HTTP Basic Authentication. It has three instance variables: `username`, `password`, and `credentials`. The `intercept` method retrieves the original request from the interceptor chain and checks if it already has an Authorization header. If it does not, it creates a new request with the Authorization header set to the Basic Authentication credentials using the `Credentials.basic` method from the `okhttp3` library.

Example usage of `HttpBasicAuth`:

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new HttpBasicAuth())
        .build();

Request request = new Request.Builder()
        .url("https://example.com/api")
        .build();

Response response = client.newCall(request).execute();
```

In both examples, we create a new `OkHttpClient` instance and add an instance of the respective authentication interceptor. We then create a new `Request` object with the desired URL and send it using the `OkHttpClient`. The authentication interceptor will automatically add the required authentication information to the request before it is sent.
