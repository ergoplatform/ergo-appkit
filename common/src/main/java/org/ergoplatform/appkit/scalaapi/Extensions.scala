package org.ergoplatform.appkit.scalaapi

import debox.cfor
import org.ergoplatform.sdk.Extensions.CollBuilderOps
import scalan.RType
import special.collection.Coll
import scalan.rtypeToClassTag  // don't remove it is used

import scala.collection.immutable
import scala.reflect.ClassTag

// TODO refactor: removed and reuse from Sigma SDK module when available
object Extensions {

  // TODO remove and reuse from Sigma SDK DoubleOps
  implicit class IntOps(val i: Double) extends AnyVal {
    def erg: Long = (i * 1000000000L).toLong
  }

  implicit class CollOps[A](val coll: Coll[A]) extends AnyVal {
    /** Partitions this $coll in two $colls according to a predicate.
      *
      * @param pred the predicate on which to partition.
      * @return a pair of $colls: the first $coll consists of all elements that
      *         satisfy the predicate `p` and the second $coll consists of all elements
      *         that don't. The relative order of the elements in the resulting ${coll}s
      *         will BE preserved (this is different from Scala's version of this method).
      */
    def partition(pred: A => Boolean): (Coll[A], Coll[A]) = {
      val (ls, rs) = coll.toArray.partition(pred)
      val b = coll.builder
      implicit val tA: RType[A] = coll.tItem
      (b.fromArray(ls), b.fromArray(rs))
    }

    def toMap[T, U](implicit ev: A <:< (T, U)): immutable.Map[T, U] = {
      var b = immutable.Map.empty[T, U]
      val len = coll.length
      cfor(0)(_ < len, _ + 1) { i =>
        val kv = coll(i)
        if (b.contains(kv._1))
          throw new IllegalArgumentException(s"Cannot transform collection $this to Map: duplicate key in entry $kv")
        b = b + kv
      }
      b
    }

    /** Sums elements of this collection using given Numeric.
      *
      * @return sum of elements or Numeric.zero if coll is empty
      */
    def sum(implicit n: Numeric[A]): A = {
      var sum = n.zero
      val len = coll.length
      cfor(0)(_ < len, _ + 1) { i =>
        sum = n.plus(sum, coll(i))
      }
      sum
    }

    /** Apply m for each element of this collection, group by key and reduce each group using r.
      *
      * @returns one item for each group in a new collection of (K,V) pairs.
      */
    def mapReduce[K: RType, V: RType](
      m: A => (K, V),
      r: ((V, V)) => V): Coll[(K, V)] = {
      val b = coll.builder
      val (keys, values) = Utils.mapReduce(coll.toArray, m, r)
      b.pairCollFromArrays(keys, values)
    }

    /** Partitions this collection into a map of collections according to some discriminator function.
      *
      * @param key the discriminator function.
      * @tparam K the type of keys returned by the discriminator function.
      * @return A map from keys to ${coll}s such that the following invariant holds:
      * {{{
      *  (xs groupBy key)(k) = xs filter (x => key(x) == k)
      * }}}
      *         That is, every key `k` is bound to a $coll of those elements `x`
      *         for which `key(x)` equa  `k`.
      */
    def groupBy[K: RType](key: A => K): Coll[(K, Coll[A])] = {
      val b = coll.builder
      implicit val tA = coll.tItem
      val res = coll.toArray.groupBy(key).mapValues(b.fromArray(_))
      b.fromMap(res.toMap)
    }

    /** Partitions this collection into a map of collections according to some
      * discriminator function. Additionally projecting each element to a new value.
      *
      * @param key the discriminator fu tion.
      * @param proj projection function to produce new value for each element of this  $coll
      * @tparam K the type of keys returned by the discriminator function.
      * @tparam V the type of values returned by the projection function.
      * @return A map from keys to ${coll}s such that the following invariant holds:
      * {{{
      *    (xs groupByProjecting (key, proj))(k) = xs filter (x => key(x) == k).map(proj)
      *   }}}
      *  That is, every key `k` is bound to projections of those elements `x`
      *  for which `key(x)` eq ls `k`.
      */
    def groupByProjecting[K: RType, V: RType
      ](key: A => K
      , proj: A => V): Coll[(K, Coll[V])] = {
      implicit val ctV: ClassTag[V] = RType[V].classTag
      val b = coll.builder
      val res = coll.toArray.groupBy(key).mapValues(arr => b.fromArray(arr.map(proj)))
      b.fromMap(res.toMap)
    }

  }

  // TODO remove and reuse from Sigma
  implicit class AnyOps[A](val source: A) extends AnyVal {
    /** Performs a specified action on the source value and returns the result. */
    def perform(action: A => A): A = action(source)
  }
}
