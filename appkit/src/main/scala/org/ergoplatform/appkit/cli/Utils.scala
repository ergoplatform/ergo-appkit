package org.ergoplatform.appkit.cli

/** Collection of all sorts of utility methods. */
object Utils {
  /** Prints the step description to the console, performs the step and then finishes the
    * description with step status.
    */
  def loggedStep[T](msg: String, console: Console)(step: => T): T = {
    console.print(msg + "...")
    val res = step
    val status = if (res != null) "Ok" else "Error"
    console.println(s" $status")
    res
  }
}
