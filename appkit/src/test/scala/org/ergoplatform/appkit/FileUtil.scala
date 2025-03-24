package org.ergoplatform.appkit

import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

object FileUtil {
  def read(file: File): String = new String(Files.readAllBytes(file.toPath), Charset.defaultCharset())
}
