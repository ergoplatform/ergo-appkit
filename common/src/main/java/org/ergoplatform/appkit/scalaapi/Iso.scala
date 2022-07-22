package org.ergoplatform.appkit.scalaapi

import org.ergoplatform.appkit.ErgoType
import special.collection.Coll

abstract class Iso[S, J] {
  def scalaType: ErgoType[S]

  def javaType: ErgoType[J]

  def toJava(x: S): J

  def toScala(y: J): S
}

object Iso {
  class PrimIso[S, J](
    implicit to: S => J,
    from: J => S,
    val scalaType: ErgoType[S],
    val javaType: ErgoType[J]) extends Iso[S, J] {
    override def toJava(x: S): J = x

    override def toScala(y: J): S = y
  }

  class PairIso[SA, SB, JA, JB](
    isoA: Iso[SA, JA],
    isoB: Iso[SB, JB]) extends Iso[(SA, SB), (JA, JB)] {
    override val scalaType: ErgoType[(SA, SB)] = ErgoType.pairType(isoA.scalaType, isoB.scalaType)

    override val javaType: ErgoType[(JA, JB)] = ErgoType.pairType(isoA.javaType, isoB.javaType)

    override def toJava(x: (SA, SB)): (JA, JB) = x.asInstanceOf[(JA, JB)]

    override def toScala(y: (JA, JB)): (SA, SB) = y.asInstanceOf[(SA, SB)]
  }

  class CollIso[S, J](isoElem: Iso[S, J]) extends Iso[Coll[S], Coll[J]] {
    override val scalaType: ErgoType[Coll[S]] = ErgoType.collType(isoElem.scalaType)

    override val javaType: ErgoType[Coll[J]] = ErgoType.collType(isoElem.javaType)

    override def toJava(x: Coll[S]): Coll[J] = x.asInstanceOf[Coll[J]]

    override def toScala(y: Coll[J]): Coll[S] = y.asInstanceOf[Coll[S]]
  }

  implicit val isoByte: Iso[scala.Byte, java.lang.Byte] = new PrimIso[scala.Byte, java.lang.Byte]()

  implicit val isoShort: Iso[scala.Short, java.lang.Short] = new PrimIso[scala.Short, java.lang.Short]()

  implicit val isoInt: Iso[scala.Int, java.lang.Integer] = new PrimIso[scala.Int, java.lang.Integer]()

  implicit val isoLong: Iso[scala.Long, java.lang.Long] = new PrimIso[scala.Long, java.lang.Long]()

  implicit val isoBoolean: Iso[scala.Boolean, java.lang.Boolean] = new PrimIso[scala.Boolean, java.lang.Boolean]()

  implicit def isoPair[SA, SB, JA, JB]
    (implicit isoA: Iso[SA, JA], isoB: Iso[SB, JB]): Iso[(SA, SB), (JA, JB)] =
    new PairIso[SA, SB, JA, JB](isoA, isoB)

  implicit def isoColl[S, J](implicit isoElem: Iso[S, J]): Iso[Coll[S], Coll[J]] =
    new CollIso[S, J](isoElem)
}
