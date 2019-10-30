package org.ergoplatform.polyglot.impl

import _root_.org.ergoplatform.restapi.client.BlockHeader
import java.util

import special.sigma.Header
import scorex.crypto.authds.ADDigest
import org.ergoplatform.wallet.interpreter.ErgoInterpreter
import sigmastate.eval.{CAvlTree, CHeader, Colls}
import special.collection.Coll

object ScalaBridge {
  import org.ergoplatform.polyglot.JavaHelpers._

  def toHeaders(headers: util.List[BlockHeader]): Coll[Header] = {
    val hs = headers.toArray(new Array[BlockHeader](0)).map { h =>
      CHeader(
        id = h.getId().toColl,
        version = h.getVersion().toByte,
        parentId = h.getParentId().toColl,
        ADProofsRoot = h.getAdProofsRoot.toColl,
        stateRoot = CAvlTree(ErgoInterpreter.avlTreeFromDigest(ADDigest @@ h.getStateRoot().toBytes)),
        transactionsRoot = h.getTransactionsRoot().toColl,
        timestamp = h.getTimestamp(),
        nBits = h.getNBits(),
        height = h.getHeight,
        extensionRoot = h.getExtensionHash().toColl,
        minerPk = h.getPowSolutions.getPk().toGroupElement,
        powOnetimePk = h.getPowSolutions().getW().toGroupElement,
        powNonce = h.getPowSolutions().getN().toColl,
        powDistance = SigmaDsl.BigInt(h.getPowSolutions().getD().toBigIntegerExact),
        votes = h.getVotes().toColl
      ): Header
    }
    Colls.fromArray(hs)
  }

}
