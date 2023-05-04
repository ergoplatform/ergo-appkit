package org.ergoplatform.appkit

import sigmastate.interpreter.{Interpreter, InterpreterContext}

/** Simple light-weight interpreter that don't require IRContext and hence cannot perform
  * script reduction, but it can however verify sigma-protocol propositions [[SigmaProp]].
  */
object SigmaPropInterpreter extends Interpreter {
  override type CTX = InterpreterContext
}
