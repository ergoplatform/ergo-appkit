[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/src/main/resources)

The `.autodoc/docs/json/src/main/resources` folder contains JSON configuration files that are crucial for the ergo-appkit project. These files define various resources, classes, and methods that are exposed and used throughout the project. The folder has a subfolder named `META-INF`, which contains another subfolder called `native-image`.

The `native-image` subfolder contains the following JSON configuration files:

- `jni-config.json`: This file defines the `java.lang.InternalError` class and its constructor method with a single parameter of type `java.lang.String`. This configuration ensures that any instances of `java.lang.InternalError` are properly handled and logged with the appropriate message. For example:

```java
try {
  // some code that may throw an InternalError
} catch (InternalError e) {
  // log the error message using the defined constructor
  String errorMessage = "An internal error occurred: " + e.getMessage();
  Logger.log(new InternalError(errorMessage));
}
```

- `proxy-config.json`: This file is an empty list, likely serving as a placeholder or starting point for future code development. Although it currently has no functionality, it is important to maintain a clear and organized file structure for efficient development.

- `reflect-config.json`: This JSON configuration file specifies Java classes and their methods to be exposed for use in the project. It includes classes from the `java.lang`, `java.util`, and `org.ergoplatform` packages. For example, the `org.ergoplatform.restapi.client.BlocksApi` class is represented with its `getLastHeaders` method exposed:

```java
import org.ergoplatform.restapi.client.BlocksApi;

BlocksApi blocksApi = new BlocksApi();
BigDecimal headersCount = new BigDecimal(10);
List<BlockHeader> lastHeaders = blocksApi.getLastHeaders(headersCount);
```

- `resource-config.json`: This file defines a list of resources for the project, such as configuration files and library properties files. These resources are specified by patterns, which indicate their locations. For instance, to access the location of the `reference.conf` file, the following code could be used:

```java
String referenceConfLocation = resources.get(0).get("pattern");
```

In summary, this folder contains JSON configuration files that define resources, classes, and methods for the ergo-appkit project. These files are essential for organizing and accessing various components of the project, ensuring efficient development and usage.
