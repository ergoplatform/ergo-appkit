package org.ergoplatform.appkit.scalaapi

import org.ergoplatform.appkit.{ErgoValue, ErgoType}
import scalan.RType
import special.collection.Coll

import java.lang.{Integer => JInt, Byte => JByte, Long => JLong, Short => JShort}

object ErgoTypeApi {
  implicit val byteType: ErgoType[JByte] = ErgoType.byteType()
  implicit val shortType: ErgoType[JShort] = ErgoType.shortType()
  implicit val intType: ErgoType[JInt] = ErgoType.integerType()
  implicit val longType: ErgoType[JLong] = ErgoType.longType()

  implicit val scalaByteType: ErgoType[Byte] = ErgoType.ofRType(RType.ByteType)
  implicit val scalaShortType: ErgoType[Short] = ErgoType.ofRType(RType.ShortType)
  implicit val scalaIntType: ErgoType[Int] = ErgoType.ofRType(RType.IntType)
  implicit val scalaLongType: ErgoType[Long] = ErgoType.ofRType(RType.LongType)
}

abstract class SJIso[S, J] {
  def scalaType: ErgoType[S]
  def javaType: ErgoType[J]
  def toJava(x: S): J
  def toScala(y: J): S
}

object SJIso {
  import ErgoTypeApi._

  class PrimJSIso[S, J](implicit to: S => J, from: J => S, val scalaType: ErgoType[S], val javaType: ErgoType[J]) extends SJIso[S, J] {
    override def toJava(x: S): J = x
    override def toScala(y: J): S = y
  }

  class PairJSIso[SA, SB, JA, JB](isoA: SJIso[SA, JA], isoB: SJIso[SB, JB]) extends SJIso[(SA, SB), (JA, JB)] {
    override val scalaType: ErgoType[(SA, SB)] = ErgoType.pairType(isoA.scalaType, isoB.scalaType)
    override val javaType: ErgoType[(JA, JB)] = ErgoType.pairType(isoA.javaType, isoB.javaType)

    override def toJava(x: (SA, SB)): (JA, JB) = x.asInstanceOf[(JA, JB)]

    override def toScala(y: (JA, JB)): (SA, SB) = y.asInstanceOf[(SA, SB)]
  }

  class CollJSIso[S, J](isoElem: SJIso[S, J]) extends SJIso[Coll[S], Coll[J]] {
    override val scalaType: ErgoType[Coll[S]] = ErgoType.collType(isoElem.scalaType)
    override val javaType: ErgoType[Coll[J]] = ErgoType.collType(isoElem.javaType)

    override def toJava(x: Coll[S]): Coll[J] = x.asInstanceOf[Coll[J]]

    override def toScala(y: Coll[J]): Coll[S] = y.asInstanceOf[Coll[S]]
  }

  implicit val isoByte: SJIso[scala.Byte, java.lang.Byte] = new PrimJSIso[scala.Byte, java.lang.Byte]()
  implicit val isoShort: SJIso[scala.Short, java.lang.Short] = new PrimJSIso[scala.Short, java.lang.Short]()
  implicit val isoInt: SJIso[scala.Int, java.lang.Integer] = new PrimJSIso[scala.Int, java.lang.Integer]()
  implicit val isoLong: SJIso[scala.Long, java.lang.Long] = new PrimJSIso[scala.Long, java.lang.Long]()

  implicit def isoPair[SA, SB, JA, JB]
                        (implicit isoA: SJIso[SA, JA], isoB: SJIso[SB, JB]): SJIso[(SA, SB), (JA, JB)] =
    new PairJSIso[SA, SB, JA, JB](isoA, isoB)

  implicit def isoColl[S, J](implicit isoElem: SJIso[S, J]): SJIso[Coll[S], Coll[J]] =
    new CollJSIso[S, J](isoElem)
}

object ErgoValueBuilder {
  def buildFor[S, J](value: S)(implicit iso: SJIso[S, J]): ErgoValue[J] = {
    val jvalue = iso.toJava(value)
    ErgoValue.of(jvalue, iso.javaType)
  }
}
