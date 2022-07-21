package org.ergoplatform.appkit.scalaapi

import org.ergoplatform.appkit.{ErgoValue, ErgoType}
import java.lang.{Byte => JByte, Short => JShort, Integer => JInt, Long => JLong}

object ErgoTypeApi {
  implicit val byteType: ErgoType[JByte] = ErgoType.byteType()
  implicit val shortType: ErgoType[JShort] = ErgoType.shortType()
  implicit val intType: ErgoType[JInt] = ErgoType.integerType()
  implicit val longType: ErgoType[JLong] = ErgoType.longType()
}

abstract class SJIso[S, J] {
  def ergoType: ErgoType[J]
  def toJava(x: S): J
  def toScala(y: J): S
}

object SJIso {
  import ErgoTypeApi._

  class PrimJSIso[S, J](implicit to: S => J, from: J => S, val ergoType: ErgoType[J]) extends SJIso[S, J] {
    override def toJava(x: S): J = x
    override def toScala(y: J): S = y
  }

  class PairJSIso[SA, SB, JA, JB](isoA: SJIso[SA, JA], isoB: SJIso[SB, JB]) extends SJIso[(SA, SB), (JA, JB)] {
    override val ergoType: ErgoType[(JA, JB)] = ErgoType.pairType(isoA.ergoType, isoB.ergoType)

    override def toJava(x: (SA, SB)): (JA, JB) = (isoA.toJava(x._1), isoB.toJava(x._2))

    override def toScala(y: (JA, JB)): (SA, SB) = (isoA.toScala(y._1), isoB.toScala(y._2))
  }

  implicit val sjByte: SJIso[scala.Byte, java.lang.Byte] = new PrimJSIso[scala.Byte, java.lang.Byte]()
  implicit val sjShort: SJIso[scala.Short, java.lang.Short] = new PrimJSIso[scala.Short, java.lang.Short]()
  implicit val sjInt: SJIso[scala.Int, java.lang.Integer] = new PrimJSIso[scala.Int, java.lang.Integer]()
  implicit val sjLong: SJIso[scala.Long, java.lang.Long] = new PrimJSIso[scala.Long, java.lang.Long]()

  implicit def pairJSIso[SA, SB, JA, JB]
                        (implicit isoA: SJIso[SA, JA], isoB: SJIso[SB, JB]): SJIso[(SA, SB), (JA, JB)] =
    new PairJSIso[SA, SB, JA, JB](isoA, isoB)
}

object ErgoValueBuilder {
  def buildFor[S, J](value: S)(implicit iso: SJIso[S, J]): ErgoValue[J] = {
    val jvalue = iso.toJava(value)
    ErgoValue.of(jvalue, iso.ergoType)
  }
}
