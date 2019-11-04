package org.ergoplatform.polyglot.impl

import _root_.org.ergoplatform.restapi.client._
import java.util
import java.util.stream.Collectors
import java.util.{List, ArrayList}

import org.ergoplatform.ErgoBox.{NonMandatoryRegisterId, TokenId}
import org.ergoplatform.{DataInput, ErgoBox, Input}
import org.ergoplatform.polyglot.{Iso, ErgoToken, JavaHelpers}
import org.ergoplatform.settings.ErgoAlgos
import special.sigma.Header
import scorex.crypto.authds.{ADDigest, ADKey}
import org.ergoplatform.wallet.interpreter.ErgoInterpreter
import scorex.crypto.hash.Digest32
import scorex.util.ModifierId
import sigmastate.SType
import sigmastate.Values.{ErgoTree, EvaluatedValue}
import sigmastate.eval.{CAvlTree, CHeader, Colls}
import sigmastate.interpreter.{ProverResult, ContextExtension}
import sigmastate.serialization.ErgoTreeSerializer.{DefaultSerializer => TreeSerializer}
import sigmastate.serialization.ValueSerializer
import special.collection.Coll

import scala.collection.JavaConverters

object ScalaBridge {
  import org.ergoplatform.polyglot.JavaHelpers._

  implicit val isoSpendingProof: Iso[SpendingProof, ProverResult] = new Iso[SpendingProof, ProverResult] {
    override def to(spendingProof: SpendingProof): ProverResult = {
      val proof = ErgoAlgos.decodeUnsafe(spendingProof.getProofBytes)
      new ProverResult(proof, ContextExtension.empty)
    }

    override def from(proverResult: ProverResult): SpendingProof = {
      val proof = ErgoAlgos.encode(proverResult.proof)
      new SpendingProof().proofBytes(proof)
    }
  }

  implicit val isoErgoTransactionDataInput: Iso[ErgoTransactionDataInput, DataInput] = new Iso[ErgoTransactionDataInput, DataInput] {
    override def to(ergoTransactionDataInput: ErgoTransactionDataInput) =
      new DataInput(ADKey @@ ErgoAlgos.decodeUnsafe(ergoTransactionDataInput.getBoxId))

    override def from(dataInput: DataInput): ErgoTransactionDataInput =
      new ErgoTransactionDataInput().boxId(ErgoAlgos.encode(dataInput.boxId))
  }

  implicit val isoErgoTransactionInput: Iso[ErgoTransactionInput, Input] = new Iso[ErgoTransactionInput, Input] {
    override def to(ergoTransactionInput: ErgoTransactionInput) =
      new Input(
        ADKey @@ ErgoAlgos.decodeUnsafe(ergoTransactionInput.getBoxId),
        ScalaBridge.isoSpendingProof.to(ergoTransactionInput.getSpendingProof))

    override def from(input: Input): ErgoTransactionInput =
      new ErgoTransactionInput()
          .boxId(ErgoAlgos.encode(input.boxId))
          .spendingProof(ScalaBridge.isoSpendingProof.from(input.spendingProof))
  }

  implicit val isoAssetToErgoToken: Iso[Asset, ErgoToken] = new Iso[Asset, ErgoToken] {
    override def to(a: Asset): ErgoToken = new ErgoToken(a.getTokenId, a.getAmount)
    override def from(t: ErgoToken): Asset = new Asset().tokenId(t.getId.toString).amount(t.getValue)
  }

  implicit val isoAssetToPair: Iso[Asset, (TokenId, Long)] = new Iso[Asset, (TokenId, Long)] {
    override def to(a: Asset) = (Digest32 @@ a.getTokenId.toBytes, a.getAmount)
    override def from(t: (TokenId, Long)): Asset = new Asset().tokenId(ErgoAlgos.encode(t._1)).amount(t._2)
  }


  implicit val isoStringToErgoTree: Iso[String, ErgoTree] = new Iso[String, ErgoTree] {
    override def to(treeStr: String): ErgoTree = {
      val treeBytes = ErgoAlgos.decodeUnsafe(treeStr)
      TreeSerializer.deserializeErgoTree(treeBytes)
    }
    override def from(tree: ErgoTree): String = {
      ErgoAlgos.encode(TreeSerializer.serializeErgoTree(tree))
    }
  }

  type AdditionalRegisters = Map[NonMandatoryRegisterId, _ <: EvaluatedValue[_ <: SType]]

  implicit val isoRegistersToMap: Iso[Registers, AdditionalRegisters] = new Iso[Registers, AdditionalRegisters] {
    override def to(regs: Registers): AdditionalRegisters = {
      JavaConverters.mapAsScalaMap(regs).map { r =>
        val id = ErgoBox.registerByName(r._1).asInstanceOf[NonMandatoryRegisterId]
        val v = ValueSerializer.deserialize(ErgoAlgos.decodeUnsafe(r._2))
        (id, v.asInstanceOf[EvaluatedValue[_ <: SType]])
      }.toMap
    }
    override def from(b: AdditionalRegisters): Registers = ???
  }

  implicit val isoErgoTransactionOutput: Iso[ErgoTransactionOutput, ErgoBox] = new Iso[ErgoTransactionOutput, ErgoBox] {
    override def to(boxData: ErgoTransactionOutput): ErgoBox = {
      val tree= boxData.getErgoTree.convertTo[ErgoTree]
      val tokens = boxData.getAssets.convertTo[Coll[(TokenId, Long)]]
      val regs = boxData.getAdditionalRegisters().convertTo[AdditionalRegisters]
      new ErgoBox(boxData.getValue, tree,
        tokens, regs,
        ModifierId @@ boxData.getTransactionId,
        boxData.getIndex.shortValue,
        boxData.getCreationHeight)
    }

    override def from(box: ErgoBox): ErgoTransactionOutput = {
      val assets = box.additionalTokens.convertTo[List[Asset]]
      val regs = isoRegistersToMap.from(box.additionalRegisters)
      val out = new ErgoTransactionOutput()
          .boxId(ErgoAlgos.encode(box.id))
          .value(box.value)
          .ergoTree(ErgoAlgos.encode(TreeSerializer.serializeErgoTree(box.ergoTree)))
          .assets(assets)
          .additionalRegisters(regs)
          .creationHeight(box.creationHeight)
          .transactionId(box.transactionId)
          .index(box.index)
      out
    }
  }

  implicit val isoBlockHeader: Iso[BlockHeader, Header] = new Iso[BlockHeader, Header] {
    override def to(h: BlockHeader): Header =
      CHeader(
        id = h.getId.toColl,
        version = h.getVersion.toByte,
        parentId = h.getParentId.toColl,
        ADProofsRoot = h.getAdProofsRoot.toColl,
        stateRoot = CAvlTree(ErgoInterpreter.avlTreeFromDigest(ADDigest @@ h.getStateRoot.toBytes)),
        transactionsRoot = h.getTransactionsRoot.toColl,
        timestamp = h.getTimestamp(),
        nBits = h.getNBits(),
        height = h.getHeight,
        extensionRoot = h.getExtensionHash.toColl,
        minerPk = h.getPowSolutions.getPk.toGroupElement,
        powOnetimePk = h.getPowSolutions.getW.toGroupElement,
        powNonce = h.getPowSolutions.getN.toColl,
        powDistance = SigmaDsl.BigInt(h.getPowSolutions.getD.toBigIntegerExact),
        votes = h.getVotes.toColl
      )

    override def from(a: Header): BlockHeader = ???
  }
}
