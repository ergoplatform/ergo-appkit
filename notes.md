
Suggested environment variables:

```
export GRAAL_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-19.1.1/Contents/Home
export PATH=$PATH:${GRAAL_HOME}/bin
```

prepare polyglot jar file
```
sbt assembly
```

Running JavaScript file
```
js --jvm --vm.cp=target/scala-2.12/ergo-polyglot-3.0.0.jar prove.js
```

Generate native image
```
native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar --report-unsupported-elements-at-runtime --no-fallback org.ergoplatform.polyglot.ni.Prove prove
```

Generate native lib
```
native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar --report-unsupported-elements-at-runtime --no-fallback --shared -H:Name=libprove
otool -L libprove.dylib

native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar\
 --report-unsupported-elements-at-runtime\
  --no-fallback -H:+TraceClassInitialization -H:+ReportExceptionStackTraces\
   -H:+AddAllCharsets -H:+AllowVMInspection -H:-RuntimeAssertions\
    --enable-url-protocols=http,https org.ergoplatform.polyglot.ni.Prove prove
    
./prove -Djava.library.path=/Users/slesarenko/Applications/graal/graalvm-ce-19.2.0.1/Contents/Home/jre/lib
```

Generate reflection config files
```
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image org.ergoplatform.polyglot.ni.Prove
```

```
clang -I. -L. -lprove prove.c -o provesign
otool -L provesign
```

Usefull options 
```
--initialize-at-build-time=org.ergoplatform.api.client.InfoApi
-H:+AddAllCharsets
```

#### Retrofit failed

```
native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar --report-unsupported-elements-at-runtime --no-fallback -H:+TraceClassInitialization -H:+ReportExceptionStackTraces -H:+AddAllCharsets -H:+AllowVMInspection -H:-RuntimeAssertions --enable-url-protocols=http,https --initialize-at-build-time=org.ergoplatform.api.client.InfoApi org.ergoplatform.polyglot.ni.Prove prove
[prove:1742]    classlist:  32,133.40 ms
[prove:1742]        (cap):   2,177.66 ms
[prove:1742]        setup:   4,336.99 ms
[prove:1742]     analysis:  43,829.21 ms
Error: Unsupported features in 5 methods
Detailed message:
Error: com.oracle.graal.pointsto.constraints.UnsupportedFeatureException: Invoke with MethodHandle argument could not be reduced to at most a single call: java.lang.invoke.MethodHandle.bindTo(Object)
Trace:
	at parsing java.lang.invoke.MethodHandleImpl.makePairwiseConvertByEditor(MethodHandleImpl.java:221)
Call path from entry point to java.lang.invoke.MethodHandleImpl.makePairwiseConvertByEditor(MethodHandle, MethodType, boolean, boolean):
	at java.lang.invoke.MethodHandleImpl.makePairwiseConvertByEditor(MethodHandleImpl.java:207)
	at java.lang.invoke.MethodHandleImpl.makePairwiseConvert(MethodHandleImpl.java:194)
	at java.lang.invoke.MethodHandleImpl.makePairwiseConvert(MethodHandleImpl.java:380)
	at java.lang.invoke.MethodHandle.asTypeUncached(MethodHandle.java:776)
	at java.lang.invoke.MethodHandle.asType(MethodHandle.java:761)
	at java.lang.invoke.MethodHandle.invokeWithArguments(MethodHandle.java:627)
	at retrofit2.Platform$Java8.invokeDefaultMethod(Platform.java:104)
	at retrofit2.Retrofit$1.invoke(Retrofit.java:147)
	at com.sun.proxy.$Proxy160.getNodeInfo(Unknown Source)
	at org.ergoplatform.polyglot.ni.Runner.request(Runner.java:49)
	at org.ergoplatform.polyglot.ni.Prove.sign(Prove.java:22)
	at com.oracle.svm.core.code.IsolateEnterStub.Prove_sign_7170c23805bf4c1edd7b2739186087c5f31f1aec(generated:0)
```

Graal SDK in build.sbt
```
libraryDependencies ++= Seq("org.graalvm" % "graal-sdk" % "0.30")
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
