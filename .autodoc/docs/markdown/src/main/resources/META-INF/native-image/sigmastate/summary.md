[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/src/main/resources/META-INF/native-image/sigmastate)

The `.autodoc/docs/json/src/main/resources/META-INF/native-image/sigmastate` folder contains JSON configuration files that are crucial for the ergo-appkit project. These files define various resources, classes, and methods that are exposed and used throughout the project.

`proxy-config.json` is an empty list, likely serving as a placeholder or starting point for future code development. Although it currently has no functionality, it is important to maintain a clear and organized file structure for efficient development.

`reflect-config.json` is a JSON configuration file that specifies Scala classes and their methods to be exposed for use in the project. It includes classes from the `scala`, `sigmastate`, and `special.collection` packages. For example, the `scala.collection.immutable.Vector` class is represented with its `iterator` method exposed:

```scala
import scala.collection.immutable.Vector

val myVector = Vector(1, 2, 3)
val iterator = myVector.iterator
while (iterator.hasNext) {
  println(iterator.next())
}
```

`resource-config.json` defines a list of resources for the project, such as configuration files, library properties files, and test suite definitions. These resources are specified by patterns, which indicate their locations. For instance, to access the location of the SLF4J service provider file, the following code could be used:

```java
String slf4jServiceProviderLocation = resources.get(0).get("pattern");
```

In summary, this folder contains JSON configuration files that define resources, classes, and methods for the ergo-appkit project. These files are essential for organizing and accessing various components of the project, ensuring efficient development and usage.
