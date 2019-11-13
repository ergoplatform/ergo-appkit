# Top 5 Things To Do With Ergo Appkit

[Ergo Appkit](https://github.com/aslesarenko/ergo-appkit) is a library for
polyglot development of Ergo Applications based on
[GraalVM](https://www.graalvm.org/). Many [useful
things](https://medium.com/graalvm/graalvm-ten-things-12d9111f307d) can be done
with GraalVM. Relying on that, in this article we’ll list some of the Appkit
features inherited from GraalVM and explain how you can use them.

- [1. Develop Ergo Applications in Java]()
- [2. Low-footprint, fast-startup Ergo Applications]()
- [3. Develop Ergo Applications in JavaScript, Python, Ruby, and R]()
- [4. Ergo Application as native library]()
- [5. Debug your polyglot Ergo Application]()

## 1. Develop Ergo Applications in Java

Appkit aims to provide a set of Java interfaces which can be used idiomatically
in Java applications. If you use Java, you will feel right at home using Appkit.

Please follow the [setup
instructions](https://github.com/aslesarenko/ergo-appkit#setup) for GraalVM and
Appkit if you want to reproduce the examples below.

Appkit needs to connect with Ergo nodes via REST API. 
For the discussion below we assume [the full
node is set up](https://github.com/ergoplatform/ergo/wiki/Set-up-a-full-node) and started
using the following commands.
```shell
$ java -jar -Xmx4G target/scala-2.12/ergo-3.1.3.jar --testnet -c ergo-testnet.conf
```


If that seems too simple please look at [a more advanced
example](https://github.com/aslesarenko/ergo-appkit#using-from-java) of Java
application.

## 2. Low-footprint, fast-startup Ergo Applications

Using Java for short-running processes can suffer from longer startup time and
relatively high memory usage.
