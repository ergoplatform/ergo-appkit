
prepare polyglot jar file

`sbt assembly`

Running JavaScript file

`js --jvm --vm.cp=target/scala-2.12/ergo-polyglot-3.0.0.jar prove.js`

Generate native image

`native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar --report-unsupported-elements-at-runtime --no-fallback org.ergoplatform.polyglot.ni.Prove prove`

Generate native lib

`native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar --report-unsupported-elements-at-runtime --no-fallback --shared -H:Name=libprove`

`otool -L libprove.dylib`

Generate reflection config files

`java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image org.ergoplatform.polyglot.ni.Prove`

```
clang -I. -L. -lprove prove.c -o provesign
otool -L provesign
```



https://github.com/graalvm/graalvm-demos

Graal SDK in build.sbt

`libraryDependencies ++= Seq("org.graalvm" % "graal-sdk" % "0.30")`

