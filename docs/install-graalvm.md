

### Openssl dependency 
```
❯ brew install openssl
==> Downloading https://homebrew.bintray.com/bottles/openssl@1.1-1.1.1d.catalina.bottle.tar.gz
==> Downloading from https://akamai.bintray.com/d7/d7f992ebfd78f80828051f6dc6a1a99aed405f86b0f39ea651fd0afeadd1b0f4?__gda__=exp=1576016972~hmac=8b03380
######################################################################## 100.0%
==> Pouring openssl@1.1-1.1.1d.catalina.bottle.tar.gz
==> Caveats
A CA file has been bootstrapped using certificates from the system
keychain. To add additional certificates, place .pem files in
  /usr/local/etc/openssl@1.1/certs

and run
  /usr/local/opt/openssl@1.1/bin/c_rehash

openssl@1.1 is keg-only, which means it was not symlinked into /usr/local,
because openssl/libressl is provided by macOS so don't link an incompatible version.

If you need to have openssl@1.1 first in your PATH run:
  echo 'export PATH="/usr/local/opt/openssl@1.1/bin:$PATH"' >> ~/.zshrc

For compilers to find openssl@1.1 you may need to set:
  export LDFLAGS="-L/usr/local/opt/openssl@1.1/lib"
  export CPPFLAGS="-I/usr/local/opt/openssl@1.1/include"

==> Summary
🍺  /usr/local/Cellar/openssl@1.1/1.1.1d: 7,983 files, 17.9MB
```