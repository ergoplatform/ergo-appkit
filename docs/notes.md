
Suggested environment variables:

```
export GRAAL_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-19.1.1/Contents/Home
export PATH=$PATH:${GRAAL_HOME}/bin
```

Generate reflection config files
```
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image org.ergoplatform.appkit.ni.Prove
```

Update reflection config files
```
java -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image org.ergoplatform.appkit.ni.Prove
```

prepare appkit jar file
```
sbt assembly
```

### Using native-image

Generate native image for a class
```
native-image --no-server \
 -cp target/scala-2.12/ergo-appkit-3.0.0.jar\
 --report-unsupported-elements-at-runtime\
  --no-fallback -H:+TraceClassInitialization -H:+ReportExceptionStackTraces\
   -H:+AddAllCharsets -H:+AllowVMInspection -H:-RuntimeAssertions\
   --allow-incomplete-classpath \
    --enable-url-protocols=http,https org.ergoplatform.example.PrepareBoxJava preparebox
    
otool -L preparebox
DYLD_LIBRARY_PATH=$GRAAL_HOME/jre/lib ./preparebox "{ sigmaProp(CONTEXT.headers.size == 9) }"
```
    
Generate shared library
```
native-image --no-server \
 -cp target/scala-2.12/ergo-appkit-3.0.0.jar\
 --report-unsupported-elements-at-runtime\
  --no-fallback -H:+TraceClassInitialization -H:+ReportExceptionStackTraces\
   -H:+AddAllCharsets -H:+AllowVMInspection -H:-RuntimeAssertions\
   --allow-incomplete-classpath \
    --enable-url-protocols=http,https --shared -H:Name=libpreparebox
    
otool -L libpreparebox.dylib
```

Compile C application 
```
clang -I. -L. -lpreparebox call_preparebox.c -o call_preparebox
otool -L call_preparebox
DYLD_LIBRARY_PATH=$GRAAL_HOME/jre/lib ./call_preparebox "{ sigmaProp(CONTEXT.headers.size == 9) }"
```

### Using GraalVM JS

Using  `js`
```
js --jvm --vm.cp=target/scala-2.12/ergo-appkit-3.0.0.jar \
    examples/src/main/java/org/ergoplatform/example/PrepareBox.js
```
Using `node`
```
node --jvm --vm.cp=target/scala-2.12/ergo-appkit-3.0.0.jar examples/src/main/java/org/ergoplatform/example/ErgoTool.js  1000000000
```
Start session for debugging
```
js --jvm --inspect --vm.cp=target/scala-2.12/ergo-appkit-3.0.0.jar \
    examples/src/main/java/org/ergoplatform/example/PrepareBox.js
```

### Usign GraalVM Python
```
graalpython --jvm --polyglot --vm.cp=target/scala-2.12/ergo-appkit-3.1.0.jar examples/src/main/java/org/ergoplatform/example/ErgoTool.py 1900000000
```

### Usign GraalVM Ruby
```
ruby --polyglot --jvm --vm.cp=target/scala-2.12/ergo-appkit-3.1.0.jar examples/src/main/java/org/ergoplatform/example/ErgoTool.rb 1900000000
```

### Creating Idea Project

go to `substratevm` sub-directory and run
```
mx ideinit
```
if suggested run 
```
mx build --dependencies=TRUFFLE_NFI
```
and then repeat
```
mx ideinit
```

### References

- [Awesome Graal](https://github.com/neomatrix369/awesome-graal)
- [Assisted Configuration of Native Image Builds](https://github.com/oracle/graal/blob/master/substratevm/CONFIGURE.md)
- [Proof-of-concept: Configuration to get native-image to generate a bin](https://github.com/cloudstateio/cloudstate/pull/56)
- [Akka HTTP + GraalVM native](https://github.com/vmencik/akka-graal-native/blob/master/README.md#logging)
- [Dynamic proxies on Substrate VM](https://github.com/oracle/graal/blob/master/substratevm/DYNAMIC_PROXY.md)
- [Simplifying native-image generation with Maven plugin and embeddable configuration](https://medium.com/graalvm/simplifying-native-image-generation-with-maven-plugin-and-embeddable-configuration-d5b283b92f57)
- [Updates on Class Initialization in GraalVM Native Image Generation](https://medium.com/graalvm/updates-on-class-initialization-in-graalvm-native-image-generation-c61faca461f7)
- [native-netty-plot](https://github.com/graalvm/graalvm-demos/tree/master/native-netty-plot)
- [A simple example language built using the Truffle API](https://github.com/graalvm/simplelanguage)
- [graalvm-demos](https://github.com/graalvm/graalvm-demos)
- [okhttp-client-mock example tests](https://github.com/gmazzo/okhttp-client-mock/blob/master/library/src/test/java/okhttp3/mock/MockInterceptorITTest.java)
- [Retrofit Mock Web Server](https://github.com/square/retrofit/tree/master/retrofit-mock)
- [Okhttp Mock Web Server](https://github.com/square/okhttp/tree/master/mockwebserver)
- [Using Mock Web Server](https://tech.okcupid.com/ui-tests-with-mockwebserver/)
- [GraalVM with native-image compilation in Travis CI](https://stackoverflow.com/questions/58465833/graalvm-with-native-image-compilation-in-travis-ci)
- [Quarkus and GraalVM: Booting Hibernate at Supersonic Speed, Subatomic Size](https://www.infoq.com/presentations/quarkus-graalvm-sao-paulo-2019/)
- [http4s + Graal](https://github.com/hhandoko/http4s-graal)
- [Building Serverless Scala Services with GraalVM](https://www.inner-product.com/posts/serverless-scala-services-with-graalvm/)

### Issues
- [okhttp parse error with GraalVM CE 19.0.0](https://github.com/oracle/graal/issues/1521)
- [Kotlin interfaces cause native-image failures](https://github.com/oracle/graal/issues/1549)
- [issue with okhttp with graalvm-ce-19.0.0](https://github.com/oracle/graal/issues/1294)
- [Micronaut + GraalVM - no compile](https://github.com/flowable/flowable-engine/issues/1974)
- [com.oracle.graal.pointsto.constraints.UnsupportedFeatureException creating Micronaut native image with Redis Lettuce](https://github.com/oracle/graal/issues/1036)
- [On security provider and Bouncy Castle with NI](https://github.com/oracle/graal/issues/951)
- [Error:java: javacTask: source release 8 requires target release 1.8](https://stackoverflow.com/questions/29888592/errorjava-javactask-source-release-8-requires-target-release-1-8?noredirect=1)
- [Kotlin coroutines with loops are 18 times slower under the Graal CI.(closed in 20.1)](https://github.com/oracle/graal/issues/1330)

## Ruby notes after `brew install ruby`
By default, binaries installed by gem will be placed into:
  /usr/local/lib/ruby/gems/2.6.0/bin

You may want to add this to your PATH.

ruby is keg-only, which means it was not symlinked into /usr/local,
because macOS already provides this software and installing another version in
parallel can cause all kinds of trouble.

If you need to have ruby first in your PATH run:
  echo 'export PATH="/usr/local/opt/ruby/bin:$PATH"' >> ~/.zshrc

For compilers to find ruby you may need to set:
  export LDFLAGS="-L/usr/local/opt/ruby/lib"
  export CPPFLAGS="-I/usr/local/opt/ruby/include"

For pkg-config to find ruby you may need to set:
  export PKG_CONFIG_PATH="/usr/local/opt/ruby/lib/pkgconfig"

## FlowCards vs ErgoScript?

The key point is to specify conditions like `bid ? R4 == bid.id` in the
boxes, rather than in the contracts as we used to think. 

This idea of attaching conditions to boxes is a key, because it shifts the focus from
ErgoScript contracts to the overall flow of values (hence FlowCard name), in such a way,
that ErgoScript is always generated from them. 

ErgoScript is a language of Ergo blockchain.
FlowCard is a declarative specification of both off-chain transaction construction and
on-chain box spending verifications. Since ErgoScript can always be generated for every
box of a FlowCard we will never need to look at the ErgoScript code. 
FlowCard is an off-chain component, that can emit transactions to the blockchain.
If we follow the semantics of the notation and the tooling is implemented correctly
ErgoScript is automatically generate behind the scene.

We can think of some Embedded DSLs, but I want to go one step further and specify FlowCard
Specification EIP and a standardized file format (Json/XML/Protobuf). And yes, visual
notation can be generated, but only as a workaround until we have graphical editing tools.

Having a diagram it is easy to write the text of `def dex(...) = ...` function, but
it is much harder to write the code first, without having a diagram on the screen. 

Whatever tools are used (DSL, Diagram Editor, etc) they all will produce a component which
we call FlowCard (e.g. file `*.flowcard` specification format) (or whatever storage is used)
which will be executed by FlowCard runtime.
