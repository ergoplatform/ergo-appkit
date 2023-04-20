package org.ergoplatform.appkit

import org.apache.commons.io.FileUtils

import java.io.File
import java.nio.charset.Charset

object FileUtil {
  def read(file: File): String = FileUtils.readFileToString(file, Charset.defaultCharset())
}
