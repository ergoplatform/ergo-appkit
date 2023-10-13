package org.ergoplatform.appkit.multisig

import org.ergoplatform.P2PKAddress
import org.ergoplatform.appkit.{SigmaProp, ReducedErgoLikeTransaction, AppkitProvingInterpreter, ErgoProver, ReducedTransaction}
import scorex.util.ModifierId
import sigmastate.interpreter.Hint

import scala.collection.mutable

/** Participant of a multisig workflow. */
case class Participant(address: P2PKAddress) {

}

case class Signature(hints: Seq[Hint]) {
}

/**
  * The SigningSession class represents a signing session for a specific transaction.
  * It keeps track of collected signatures and provides methods for adding signatures and
  * checking the completeness of the session.
  *
  * @param reducedTx  the transaction being signed.
  */
class SigningSession(reducedTx: ReducedErgoLikeTransaction) {
  /** Participants who can co-sign the transaction to provide enough signatures for all
    * the reducedInputs.
    */
  def participants: Seq[Participant] = ???

  private var signatures: List[Signature] = List.empty

  def addSignature(signature: Signature): Unit = {
    signatures = signature :: signatures
  }

  def getSignatures: List[Signature] = signatures

  def isComplete: Boolean = ???
}

/** The SigningServer class represents a server that manages multiple signing sessions.
  * It provides methods for creating and retrieving signing sessions.
  */
class SigningServer {
  private val sessions = mutable.HashMap.empty[ModifierId, SigningSession]

  /** Creates a new signing session for the given transaction.
    *
    * @param reducedTx transaction to sign
    */
  def createSession(
    reducedTx: ReducedErgoLikeTransaction): SigningSession = {
    val session = new SigningSession(reducedTx)
    sessions.put(reducedTx.unsignedTx.id, session)
    session
  }

  /** Retrieves a signing session for the given transaction.
    *
    * @param modifierId id of the transaction to sign
    */
  def getSession(modifierId: ModifierId): Option[SigningSession] = sessions.get(modifierId)
}

/** Represents a participant in a multisig signing process.
  * It provides methods for interacting with the SigningServer, such as initializing multisig sessions,
  * adding partial signatures, listing collected signatures, and checking session completeness.
  *
  * @param server The SigningServer instance that the client interacts with.
  */
class Client(server: SigningServer, I: AppkitProvingInterpreter) {
  /** Adds secrets from the given prover */
  def addSecrets(prover: ErgoProver): Unit = ???

  def initMultisigSession(tx: ReducedErgoLikeTransaction): SigningSession = {
    server.createSession(tx)
  }

  def addPartialSignature(tx: ReducedErgoLikeTransaction): Unit = {
    val signature: Signature = Signature(Seq.empty)
    server.getSession(tx.unsignedTx.id).foreach(_.addSignature(signature))
  }

  def listSignatures(modifierId: ModifierId): List[Signature] = {
    server.getSession(modifierId).map(_.getSignatures).getOrElse(List.empty)
  }

  def isComplete(modifierId: ModifierId): Boolean = {
    server.getSession(modifierId).exists(_.isComplete)
  }
}

