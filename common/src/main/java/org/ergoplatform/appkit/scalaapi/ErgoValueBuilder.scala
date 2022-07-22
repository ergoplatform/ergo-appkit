package org.ergoplatform.appkit.scalaapi

import org.ergoplatform.appkit.ErgoValue

/** Helper builder to easily construct ErgoValues. */
object ErgoValueBuilder {
  /** Given a value of Scala type supported by ErgoScript (and ErgoTree) this method
    * constructs ErgoValue of the required Java type.
    *
    * @param value a value of Scala type such as `Coll[(Byte, Coll[Int])]`
    * @param iso   an isomorphism that projects he given Scala type S to the
    *              corresponding Java type J. When type S is known to the compiler, then
    *              iso instance can be synthesized by the Scala compiler and the type J is
    *              inferred.
    * @return ErgoValue instance of the Java type J which corresponds to the
    *         Scala type S.
    */
  def buildFor[S, J](value: S)(implicit iso: Iso[S, J]): ErgoValue[J] = {
    val jvalue = iso.toJava(value)
    ErgoValue.of(jvalue, iso.javaType)
  }
}
