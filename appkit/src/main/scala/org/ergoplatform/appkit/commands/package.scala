package org.ergoplatform.appkit

package object commands {

  /** Should be used by ErgoTool to report usage errors */
  def usageError(msg: String, cmdDescOpt: Option[CmdDescriptor]) = throw UsageException(msg, cmdDescOpt)

}
