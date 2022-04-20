package org.ergoplatform.appkit

import sigmastate.eval.IRContext
import sigmastate.interpreter.Interpreter.error
import sigmastate.interpreter.{PrecompiledScriptProcessor, Interpreter, InterpreterContext}

/** Simple light-weight interpreter that don't require IRContext and hence cannot perform
  * script reduction, but it can however verify sigma-protocol propositions [[SigmaProp]].
  */
object SigmaPropInterpreter extends Interpreter {
  override type CTX = InterpreterContext
  override val IR: IRContext = null
  override def precompiledScriptProcessor: PrecompiledScriptProcessor =
    error("Script reduction is not supported")
}
