package org.ergoplatform.appkit

import scalan.RType
import java.lang.{Integer => JInt, Byte => JByte, Long => JLong, Short => JShort, Boolean => JBoolean}

package object scalaapi {

  /** Global instances of ErgoType which are used whenever implicit parameter is needed in
    * Scala code. */

  implicit val byteType: ErgoType[JByte] = ErgoType.byteType()
  implicit val shortType: ErgoType[JShort] = ErgoType.shortType()
  implicit val intType: ErgoType[JInt] = ErgoType.integerType()
  implicit val longType: ErgoType[JLong] = ErgoType.longType()
  implicit val booleanType: ErgoType[JBoolean] = ErgoType.booleanType()

  implicit val scalaByteType: ErgoType[Byte] = ErgoType.ofRType(RType.ByteType)
  implicit val scalaShortType: ErgoType[Short] = ErgoType.ofRType(RType.ShortType)
  implicit val scalaIntType: ErgoType[Int] = ErgoType.ofRType(RType.IntType)
  implicit val scalaLongType: ErgoType[Long] = ErgoType.ofRType(RType.LongType)
  implicit val scalaBooleanType: ErgoType[Boolean] = ErgoType.ofRType(RType.BooleanType)
}
