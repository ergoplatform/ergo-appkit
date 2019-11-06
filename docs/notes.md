
Suggested environment variables:

```
export GRAAL_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-19.1.1/Contents/Home
export PATH=$PATH:${GRAAL_HOME}/bin
```

Generate reflection config files
```
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image org.ergoplatform.polyglot.ni.Prove
```

Update reflection config files
```
java -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image org.ergoplatform.polyglot.ni.Prove
```

prepare polyglot jar file
```
sbt assembly
```

### Using native-image

Generate native image for a class
```
native-image --no-server \
 -cp target/scala-2.12/ergo-polyglot-3.0.0.jar\
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
 -cp target/scala-2.12/ergo-polyglot-3.0.0.jar\
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
js --jvm --vm.cp=target/scala-2.12/ergo-polyglot-3.0.0.jar \
    examples/src/main/java/org/ergoplatform/example/PrepareBox.js
```
Using `node`
```
node --jvm --vm.cp=target/scala-2.12/ergo-polyglot-3.0.0.jar examples/src/main/java/org/ergoplatform/example/ErgoTool.js  1000000000
```
Start session for debugging
```
js --jvm --inspect --vm.cp=target/scala-2.12/ergo-polyglot-3.0.0.jar \
    examples/src/main/java/org/ergoplatform/example/PrepareBox.js
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

### Issues
- [okhttp parse error with GraalVM CE 19.0.0](https://github.com/oracle/graal/issues/1521)
- [Kotlin interfaces cause native-image failures](https://github.com/oracle/graal/issues/1549)
- [issue with okhttp with graalvm-ce-19.0.0](https://github.com/oracle/graal/issues/1294)
- [Micronaut + GraalVM - no compile](https://github.com/flowable/flowable-engine/issues/1974)
- [com.oracle.graal.pointsto.constraints.UnsupportedFeatureException creating Micronaut native image with Redis Lettuce](https://github.com/oracle/graal/issues/1036)
- [On security provider and Bouncy Castle with NI](https://github.com/oracle/graal/issues/951)

