package org.ergoplatform.appkit

import scala.reflect.ClassTag

trait AppkitTesting {
  val addrStr = "3WzR39tWQ5cxxWWX6ys7wNdJKLijPeyaKgx72uqg9FJRBCdZPovL"

  /** Creates an assertion which checks the given type and message contents.
   *
   * @tparam E expected type of exception
   * @param msgParts expected parts of the exception message
   * @return the assertion which can be used in assertExceptionThrown method
   */
  def exceptionLike[E <: Throwable : ClassTag]
                   (msgParts: String*): Throwable => Boolean = {
    case t: E => msgParts.forall(t.getMessage.contains(_))
    case _ => false
  }

}
