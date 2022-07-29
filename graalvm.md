## How GraalVM can be used?

After many years of research and development GraalVM project has matured enough end
became [production ready](https://medium.com/graalvm/announcing-graalvm-19-4590cf354df8).

From Ergo perspective GraalVM platform opens many unique opportunities in the
nearest and the distant future. Here is a summary of some GraalVM related facts:

- Ergo node can run on GraalVM without any modifications, in fact GraalVM is
  fully compliant with JDK 1.8 (which is minimal requirement for [Ergo Protocol
  Client](https://github.com/ergoplatform/ergo/releases))

- Graal's
  [native-image](https://www.graalvm.org/docs/reference-manual/native-image/) tool
  can be used to compile JVM applications into native applications which can run
  without JVM. It can also produce shared libraries (`*.so` files on Linux and
  `*.dylib` files on MacOS) that can be used from C/C++ applications (and also
  from JavaScript, Python, etc using FFI).

- All cryptography classes [BouncyCastle Java
  library](https://bouncycastle.org/java.html) which are used in Ergo reference
  implementation can be compiled to shared library and used from non-JVM
  languages.

- All the code from [ErgoScript
  interpreter](https://github.com/ScorexFoundation/sigmastate-interpreter/pulls),
  [ergo-wallet](https://github.com/ergoplatform/ergo-wallet) and this Appkit
  library is compatible with `native-image` and can be compiled into either native
  application or shared library.

- Appkit API interfaces can be used from JavaScript and Python by design. They
  can also be [used from C/C++](https://github.com/aslesarenko/ergo-appkit-examples/tree/master/c-examples) .

- Appkit can be pre-compiled to native-image based launchers of both JavaScript
  and Python to enjoy fast startup and lower runtime memory
  overhead when running scripts

- Appkit native shared library can be used from Python and JavaScript through FFI

