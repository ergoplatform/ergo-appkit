package org.ergoplatform.appkit.cli

import java.io.{BufferedReader, PrintStream}

import org.ergoplatform.appkit.SecretString
import org.ergoplatform.appkit.commands.UsageException

/** Abstract interface for Console interactions (print and read operations).
 * Console read operations consume from input stream, and print operations
 * send data to output stream.
 */
abstract class Console {
  /** Print the given string to the output stream. */
  def print(s: String): Console

  /** Print the given string to the output stream and append new line character '\n'. */
  def println(s: String): Console

  /** Read a line (sequence of characters up to '\n') from input stream.
   * The ending '\n' character is consumed, but not included in the result.
   */
  def readLine(): String
  def readLine(prompt: String): String
  def readPassword(): SecretString
  def readPassword(prompt: String): SecretString
}
object Console {
  /** The console which should be used in application's main method. */
  lazy val instance: Console = new MainConsole()

  /** Secure double entry of the new password giving the user many attempts.
    *
    * @param nAttemps number of attempts before failing with exception
    * @param block  code block which can request the user to enter a new password twice
    * @return password returned by `block` as `Array[Char]` instead of `String`. This allows
    *        the password to be erased as fast as possible and avoid leaking to GC.
    * @throws UsageException
    */
  def readNewPassword(nAttemps: Int, console: Console)(block: => (SecretString, SecretString)): SecretString = {
    var i = 0
    do {
      val (p1, p2) = block
      i += 1
      if (p1.equals(p2)) {
        p2.erase() // cleanup duplicate copy
        return p1
      }
      else {
        p1.erase() // cleanup sensitive data
        p2.erase()
        if (i < nAttemps) {
          console.println(s"Passwords are different, try again [${i + 1}/$nAttemps]")
          // and loop
        } else
          error(s"Cannot continue without providing valid password")
      }
    } while (true)
    error("should never go here due to exhaustive `if` above")
  }

  def readNewPassword(prompt: String, secondPrompt: String)(implicit ctx: AppContext): SecretString = {
    val console = ctx.console
    readNewPassword(3, console) {
      val p1 = console.readPassword(prompt)
      val p2 = console.readPassword(secondPrompt)
      (p1, p2)
    }
  }

  def error(msg: String) = throw ConsoleException(msg)
}

/** Wrapper around system console to be used in `Application.main` method. */
class MainConsole() extends Console {
  val sysConsole = System.console()

  override def print(s: String): Console = { sysConsole.printf("%s", s); this }

  override def println(s: String): Console = { sysConsole.printf(s"%s\n", s); this }

  override def readLine(): String = sysConsole.readLine()

  override def readLine(prompt: String): String = sysConsole.readLine("%s", prompt)

  override def readPassword(): SecretString = SecretString.create(sysConsole.readPassword())

  override def readPassword(prompt: String): SecretString = SecretString.create(sysConsole.readPassword("%s", prompt))
}

/** Console implementation to be used in tests */
class TestConsole(in: BufferedReader, out: PrintStream) extends Console {
  override def print(s: String): Console = { out.print(s); this }

  override def println(s: String): Console = { out.println(s); this }

  override def readLine(): String = { in.readLine() }

  override def readLine(msg: String): String = {
    print(msg).readLine()
  }

  // TODO security: these methods should be reimplemented without using String (See java.io.Console)
  override def readPassword(): SecretString = {
    val line = readLine()
    SecretString.create(line)
  }

  override def readPassword(msg: String): SecretString = {
    print(msg).readPassword()
  }
}

/** Exception thrown by Console when incorrect usage is detected.
  * @param message error message
  */
case class ConsoleException(message: String) extends RuntimeException(message)

