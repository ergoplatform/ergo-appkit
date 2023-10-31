package org.ergoplatform.appkit

import sigma.{Header, Box, GroupElement, AvlTree}

import java.lang.{Boolean => JBoolean, Short => JShort, Integer => JInt, Long => JLong, Byte => JByte}

package object scalaapi {

  /** Global instances of ErgoType which are used whenever implicit parameter is needed in
    * Scala code.
    *
    * For Java types.
    */
  implicit val byteType: ErgoType[JByte] = ErgoType.byteType()
  implicit val shortType: ErgoType[JShort] = ErgoType.shortType()
  implicit val intType: ErgoType[JInt] = ErgoType.integerType()
  implicit val longType: ErgoType[JLong] = ErgoType.longType()
  implicit val booleanType: ErgoType[JBoolean] = ErgoType.booleanType()

  /** For Scala types. */
  implicit val scalaByteType: ErgoType[Byte] = ErgoType.ofRType(sigma.ByteType)
  implicit val scalaShortType: ErgoType[Short] = ErgoType.ofRType(sigma.ShortType)
  implicit val scalaIntType: ErgoType[Int] = ErgoType.ofRType(sigma.IntType)
  implicit val scalaLongType: ErgoType[Long] = ErgoType.ofRType(sigma.LongType)
  implicit val scalaBooleanType: ErgoType[Boolean] = ErgoType.ofRType(sigma.BooleanType)

  /** For type which are the same in Scala and Java */
  implicit val unitType: ErgoType[Unit] = ErgoType.unitType()
  implicit val bigIntType: ErgoType[sigma.BigInt] = ErgoType.bigIntType()
  implicit val groupElementType: ErgoType[GroupElement] = ErgoType.groupElementType()
  implicit val sigmaPropType: ErgoType[sigma.SigmaProp] = ErgoType.sigmaPropType()
  implicit val avlTreeType: ErgoType[AvlTree] = ErgoType.avlTreeType()
  implicit val boxType: ErgoType[Box] = ErgoType.boxType()
  implicit val headerType: ErgoType[Header] = ErgoType.headerType()
  implicit val preHeaderType: ErgoType[sigma.PreHeader] = ErgoType.preHeaderType()

}
