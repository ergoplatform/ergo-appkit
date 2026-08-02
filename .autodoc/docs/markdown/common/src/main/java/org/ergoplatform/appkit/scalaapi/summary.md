[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/.autodoc/docs/json/common/src/main/java/org/ergoplatform/appkit/scalaapi)

The `.autodoc/docs/json/common/src/main/java/org/ergoplatform/appkit/scalaapi` folder contains utility classes and objects that facilitate the interaction between Scala and Java types in the Ergo Appkit project. These utilities are particularly useful when working with the Ergo blockchain, which uses a Java-based scripting language called Sigma.

`ErgoValueBuilder.scala` provides a helper builder for constructing `ErgoValue` instances, which represent values in the Ergo blockchain. The `buildFor` method simplifies the creation of `ErgoValue` instances by taking a Scala value, an isomorphism, and converting the value to its corresponding Java type. For example:

```scala
val ergoValue: ErgoValue[java.util.List[java.util.List[java.lang.Integer]]] = ErgoValueBuilder.buildFor(myValue)
```

`Extensions.scala` offers extension methods for the `Coll` and `CollBuilder` classes, simplifying operations like partitioning, grouping, and reducing. For instance, the `groupBy` method can be used to group UTXOs by their ErgoTree, which is useful for building transactions.

`Iso.scala` defines an isomorphism between Scala and Java types, allowing developers to work with Scala types in their code and convert them to Java types when interacting with the Ergo blockchain. The `Iso` class has several subclasses that implement specific isomorphisms, such as `IdentityIso`, `PrimIso`, `PairIso`, and `CollIso`.

`Utils.scala` provides utility functions that can be used across the Ergo Appkit project. The `outerJoin` function performs an outer join operation between two maps, while the `mapReduce` function is a performance-optimized deterministic mapReduce primitive. The `mapToArrays` function converts a `Map` to a tuple of arrays. The `IntegralFromExactIntegral` class adapts an `ExactIntegral` instance to be used where `Integral` is required.

In summary, this folder contains utility classes and objects that simplify the interaction between Scala and Java types, as well as provide useful functions for working with collections and maps. These utilities are essential for developers working with the Ergo blockchain and the Ergo Appkit project.
