package org.ergoplatform.polyglot

import org.ergoplatform.ErgoBox
import org.ergoplatform.wallet.serialization.JsonCodecsWrapper
import scalan.util.FileUtil.{file, read}
import JsonCodecsWrapper._

object PrepareBox  {
  def main(args: Array[String]) = {
    val templateFile = "src/main/resources/org/ergoplatform/polyglot/response_Box.json"
    val boxTemplate = read(file(templateFile))
    val boxJson = io.circe.parser.parse(boxTemplate).toOption.get
    val box = boxJson.as[ErgoBox]
      .getOrElse(sys.error(s"Cannot read box template from $templateFile"))
//    JsonCodecsWrapper.ergoBoxDecoder()
  }
}
