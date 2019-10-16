
prepare polyglot jar file

`sbt assembly`

Running JavaScript file

`js --jvm --vm.cp=target/scala-2.12/ergo-polyglot-3.0.0.jar prove.js`

Generate native image

`native-image --no-server -cp target/scala-2.12/ergo-polyglot-3.0.0.jar --report-unsupported-elements-at-runtime --no-fallback org.ergoplatform.polyglot.ni.Prove prove`

https://github.com/graalvm/graalvm-demos

Graal SDK in build.sbt

`libraryDependencies ++= Seq("org.graalvm" % "graal-sdk" % "0.30")`

