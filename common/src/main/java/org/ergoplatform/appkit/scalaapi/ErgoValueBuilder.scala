package org.ergoplatform.appkit.scalaapi

import org.ergoplatform.appkit.ErgoValue

object ErgoValueBuilder {
  def buildFor[S, J](value: S)(implicit iso: Iso[S, J]): ErgoValue[J] = {
    val jvalue = iso.toJava(value)
    ErgoValue.of(jvalue, iso.javaType)
  }
}
