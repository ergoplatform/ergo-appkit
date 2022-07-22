package org.ergoplatform.appkit.scalaapi

import org.ergoplatform.appkit.ErgoType
import org.ergoplatform.appkit.scalaapi.Iso.IdentityIso
import special.collection.Coll
import special.sigma
import special.sigma.{Header, Box, SigmaProp, GroupElement, AvlTree, PreHeader}

/** Isomorphism between Scala type S and Java type J.
  * Note, each conversion function is actually type cast of the argument to the resulting
  * type, thus, no conversion actually done. This is true even for primitive types where
  * implicit conversion from scala.Predef are used (see Predef.byte2Byte).
  */
abstract class Iso[S, J] {
  /** Scala type descriptor */
  def scalaType: ErgoType[S]

  /** Java type descriptor */
  def javaType: ErgoType[J]

  /** Conversion from Scala to Java value. */
  def toJava(x: S): J

  /** Conversion from Java to Scala value. */
  def toScala(y: J): S
}

abstract class IsoLowPriority {
  /** This is fallback implicits which are used only when other more specific iso cannot be
    * used in implicit search. (the trick used across Scala libraries)
    */
  implicit val unitIso: Iso[Unit, Unit] = new IdentityIso[Unit]()
  implicit val bigIntIso: Iso[sigma.BigInt, sigma.BigInt] = new IdentityIso[sigma.BigInt]()
  implicit val groupElementIso: Iso[GroupElement, GroupElement] = new IdentityIso[GroupElement]()
  implicit val SigmaPropIso: Iso[SigmaProp, SigmaProp] = new IdentityIso[SigmaProp]()
  implicit val AvlTreeIso: Iso[AvlTree, AvlTree] = new IdentityIso[AvlTree]()
  implicit val isoIso: Iso[Box, Box] = new IdentityIso[Box]()
  implicit val HeaderIso: Iso[Header, Header] = new IdentityIso[Header]()
  implicit val PreHeaderIso: Iso[PreHeader, PreHeader] = new IdentityIso[PreHeader]()(preHeaderType)
}

object Iso extends IsoLowPriority {

  /** Any type is isomorphic to itself, provided there is ErgoType descriptor.
    * Given descriptor of type A, constructs identity Iso.
    */
  class IdentityIso[A](implicit tA: ErgoType[A]) extends Iso[A, A] {
    override def scalaType: ErgoType[A] = tA
    override def javaType: ErgoType[A] = tA
    override def toJava(x: A): A = x
    override def toScala(y: A): A = y
  }

  /** Iso implementation for primitive types like Byte, Short, etc. (see below) */
  class PrimIso[S, J](
      implicit to: S => J, from: J => S,
      val scalaType: ErgoType[S], val javaType: ErgoType[J]
  ) extends Iso[S, J] {
    override def toJava(x: S): J = to(x)
    override def toScala(y: J): S = from(y)
  }

  /** Iso implementation for pair (scala.Tuple2) type
    * @param isoA Iso instance between first components
    * @param isoB Iso instance between second components
    */
  class PairIso[SA, SB, JA, JB](
    isoA: Iso[SA, JA],
    isoB: Iso[SB, JB]
  ) extends Iso[(SA, SB), (JA, JB)] {
    override val scalaType: ErgoType[(SA, SB)] = ErgoType.pairType(isoA.scalaType, isoB.scalaType)

    override val javaType: ErgoType[(JA, JB)] = ErgoType.pairType(isoA.javaType, isoB.javaType)

    /** The implementation is based on the fact that SA and SB have exactly the same
      * runtime JVM types, but different in Java and Scala languages, so compilers treat
      * them as different types.
      */
    override def toJava(x: (SA, SB)): (JA, JB) = x.asInstanceOf[(JA, JB)]

    override def toScala(y: (JA, JB)): (SA, SB) = y.asInstanceOf[(SA, SB)]
  }

  class CollIso[S, J](isoElem: Iso[S, J]) extends Iso[Coll[S], Coll[J]] {
    override val scalaType: ErgoType[Coll[S]] = ErgoType.collType(isoElem.scalaType)

    override val javaType: ErgoType[Coll[J]] = ErgoType.collType(isoElem.javaType)

    /** The implementation is based on the fact that SA and SB have exactly the same
      * runtime JVM types, but different in Java and Scala languages, so compilers treat
      * them as different types.
      */
    override def toJava(x: Coll[S]): Coll[J] = x.asInstanceOf[Coll[J]]

    override def toScala(y: Coll[J]): Coll[S] = y.asInstanceOf[Coll[S]]
  }

  /** Iso instance between scala.Byte and java.lang.Byte */
  implicit val isoByte: Iso[scala.Byte, java.lang.Byte] = new PrimIso[scala.Byte, java.lang.Byte]()

  /** Iso instance between scala.Short and java.lang.Short */
  implicit val isoShort: Iso[scala.Short, java.lang.Short] = new PrimIso[scala.Short, java.lang.Short]()

  /** Iso instance between scala.Int and java.lang.Int */
  implicit val isoInt: Iso[scala.Int, java.lang.Integer] = new PrimIso[scala.Int, java.lang.Integer]()

  /** Iso instance between scala.Long and java.lang.Long */
  implicit val isoLong: Iso[scala.Long, java.lang.Long] = new PrimIso[scala.Long, java.lang.Long]()

  /** Iso instance between scala.Boolean and java.lang.Boolean */
  implicit val isoBoolean: Iso[scala.Boolean, java.lang.Boolean] = new PrimIso[scala.Boolean, java.lang.Boolean]()

  /** Given a pair of isos constructs an Iso instance between Scala and Java pairs. */
  implicit def isoPair[SA, SB, JA, JB]
    (implicit isoA: Iso[SA, JA], isoB: Iso[SB, JB]): Iso[(SA, SB), (JA, JB)] =
    new PairIso[SA, SB, JA, JB](isoA, isoB)

  /** Given an iso for elements constructs an Iso instance between Coll of Scala and Coll of Java types. */
  implicit def isoColl[S, J](implicit isoElem: Iso[S, J]): Iso[Coll[S], Coll[J]] =
    new CollIso[S, J](isoElem)
}
