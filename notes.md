
prepare polyglot jar file

`sbt assembly`

Running JavaScript file

`js --jvm --vm.cp=target/scala-2.12/ergo-polyglot-3.0.0.jar prove.js`

Generate native image

`native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar --report-unsupported-elements-at-runtime --no-fallback org.ergoplatform.polyglot.ni.Prove prove`

Generate native lib

`native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar --report-unsupported-elements-at-runtime --no-fallback --shared -H:Name=libprove`

`otool -L libprove.dylib`

native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar\
 --report-unsupported-elements-at-runtime\
  --no-fallback -H:+TraceClassInitialization -H:+ReportExceptionStackTraces\
   -H:+AddAllCharsets -H:+AllowVMInspection -H:-RuntimeAssertions\
    --enable-url-protocols=http,https org.ergoplatform.polyglot.ni.Prove prove
    
./prove -Djava.library.path=/Users/slesarenko/Applications/graal/graalvm-ce-19.2.0.1/Contents/Home/jre/lib

Generate reflection config files

`java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image org.ergoplatform.polyglot.ni.Prove`

```
clang -I. -L. -lprove prove.c -o provesign
otool -L provesign
```

Use 
--initialize-at-build-time=org.ergoplatform.api.client.InfoApi
-H:+AddAllCharsets


https://github.com/graalvm/graalvm-demos

Graal SDK in build.sbt

`libraryDependencies ++= Seq("org.graalvm" % "graal-sdk" % "0.30")`

### References

- [Assisted Configuration of Native Image Builds](https://github.com/oracle/graal/blob/master/substratevm/CONFIGURE.md)
- [Proof-of-concept: Configuration to get native-image to generate a bin](https://github.com/cloudstateio/cloudstate/pull/56)
- [Dynamic proxies on Substrate VM](https://github.com/oracle/graal/blob/master/substratevm/DYNAMIC_PROXY.md)

### Issues
- [okhttp parse error with GraalVM CE 19.0.0](https://github.com/oracle/graal/issues/1521)
- [Kotlin interfaces cause native-image failures](https://github.com/oracle/graal/issues/1549)
- [issue with okhttp with graalvm-ce-19.0.0](https://github.com/oracle/graal/issues/1294)
- [Micronaut + GraalVM - no compile](https://github.com/flowable/flowable-engine/issues/1974)
- [com.oracle.graal.pointsto.constraints.UnsupportedFeatureException creating Micronaut native image with Redis Lettuce](https://github.com/oracle/graal/issues/1036)
