package org.ergoplatform.appkit.impl

import org.ergoplatform.appkit.scalaapi.Pay2SigmaProp
import org.ergoplatform.{Pay2SAddress, P2PKAddress}
import org.ergoplatform.appkit.{Address, NetworkType, MultisigAddress}
import sigmastate.CTHRESHOLD
import sigmastate.Values.ErgoTree
import sigmastate.utils.Helpers.TryOps

import java.util
import scala.collection.JavaConverters._
import scala.util.Try

case class MultisigAddressImpl(
  numRequiredSigners: Int, participants: Seq[P2PKAddress],
  networkType: NetworkType, treeHeaderFlags: Byte) extends MultisigAddress {
  val threshold = CTHRESHOLD(numRequiredSigners, participants.map(_.pubkey))

  override def getAddress: Address = {
    // create ErgoTree without segregated constants
    val tree = ErgoTree.fromSigmaBoolean(treeHeaderFlags, threshold)
    Address.fromErgoTree(tree, networkType)
  }

  override def getParticipants: util.List[Address] = participants.map(new Address(_)).asJava

  override def getSignersRequiredCount: Int = numRequiredSigners
}

object MultisigAddressImpl {
  /** Creates a multisignature address from a list of participant addresses.
    * @param numRequiredSigners the minimum number of signers required to spend funds from the address
    * @param participants       the list of participant addresses
    * @param networkType        the network type on which the address is used
    * @param treeHeaderFlags    optional flags to put in the header of the underlying ErgoTree (no flags by default)
    * @return a new multisignature address
    */
  def fromParticipants(
    numRequiredSigners: Int, participants: java.util.List[Address],
    networkType: NetworkType, treeHeaderFlags: Byte = MultisigAddress.DEFAULT_TREE_HEADER_FLAGS): MultisigAddress = {
    val p2pkAddresses = participants.asScala.map(_.asP2PK()).toSeq
    new MultisigAddressImpl(numRequiredSigners, p2pkAddresses, networkType, treeHeaderFlags)
  }

  /** Attempts to create a multi-signature address from an existing address.
    *
    * @param address the address to convert to a multisig address
    *
    * @return a [[Try]] that wraps a MultisigAddress if successful, or an exception if the address is not a valid multisig address
    */
  def fromAddressTry(address: Address): Try[MultisigAddress] = Try {
    address.getErgoAddress match {
      case p2s: Pay2SAddress =>
        p2s.script match {
          case Pay2SigmaProp(CTHRESHOLD(k, participants)) =>
            fromParticipants(
              numRequiredSigners = k,
              participants = participants.map(Address.fromSigmaBoolean(_, address.getNetworkType)).asJava,
              networkType = address.getNetworkType)

          case _ =>
            throw new IllegalArgumentException(
              s"P2S address $address is not a valid multisig address")
        }
      case _ =>
        throw new IllegalArgumentException(
          s"Address $address is not a P2S address and cannot be multisig address")
    }
  }

  /** Creates a multi-signature address from an existing address.
    * @param address the address to convert to a multisig address
    * @return a MultisigAddress object
    * @throws IllegalArgumentException if the address is not a valid multisig address
    */
  def fromAddress(address: Address): MultisigAddress = fromAddressTry(address).getOrThrow
}
