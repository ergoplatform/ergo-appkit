package org.ergoplatform.appkit.impl

import _root_.org.ergoplatform.restapi.client._
import org.ergoplatform.ErgoBox.{AdditionalRegisters, NonMandatoryRegisterId, Token, TokenId}
import org.ergoplatform.explorer.client.model.{AdditionalRegister, AssetInstanceInfo, OutputInfo, AdditionalRegisters => ERegisters, AssetInfo => EAsset}
import org.ergoplatform.sdk.ErgoToken
import org.ergoplatform.settings.ErgoAlgos
import org.ergoplatform.wallet.interpreter.ErgoInterpreter
import org.ergoplatform.{ErgoLikeTransaction, _}
import scorex.crypto.authds.{ADDigest, ADKey}
import scorex.util.ModifierId
import sigma.ast.{ErgoTree, EvaluatedValue, SType}
import sigmastate.eval.Extensions.ArrayByteOps
import sigma.data.{CAvlTree, CHeader, Iso}
import sigma.eval.SigmaDsl
import sigma.interpreter.{ContextExtension, ProverResult}
import sigma.serialization.ErgoTreeSerializer.{DefaultSerializer => TreeSerializer}
import sigma.serialization.ValueSerializer
import sigma.Coll
import sigma.Header

import java.lang.{Byte => JByte}
import java.util
import java.util.{List => JList}
import scala.collection.JavaConverters._

object ScalaBridge {
  import org.ergoplatform.sdk.JavaHelpers.StringExtensions

  def txDataInputsAsJava(tx: ErgoLikeTransaction): JList[DataInput] =
    tx.dataInputs.toList.asJava

  def txInputsAsJava(tx: ErgoLikeTransaction): JList[Input] =
    tx.inputs.toList.asJava

  def txOutputsAsJava(tx: ErgoLikeTransaction): JList[ErgoBox] =
    tx.outputs.toList.asJava

  def contextExtensionValuesAsJava(input: Input): java.util.Map[Object, Object] =
    input.spendingProof.extension.values.asInstanceOf[scala.collection.Map[Object, Object]].asJava

  implicit val isoSpendingProof: Iso[SpendingProof, ProverResult] = new Iso[SpendingProof, ProverResult] {
    override def to(spendingProof: SpendingProof): ProverResult = {
      val proof = ErgoAlgos.decodeUnsafe(spendingProof.getProofBytes)
      val vars = spendingProof.getExtension.asScala.map { case (k, v) =>
        val id = JByte.parseByte(k, 10)
        val value = ValueSerializer.deserialize(ErgoAlgos.decodeUnsafe(v))
        (id, value.asInstanceOf[EvaluatedValue[_ <: SType]])
      }
      new ProverResult(proof, ContextExtension(vars.toMap))
    }

    override def from(proverResult: ProverResult): SpendingProof = {
      val proof = ErgoAlgos.encode(proverResult.proof)
      val vars = proverResult.extension.values
      val extension = new util.HashMap[String, String](vars.size)
      vars.foreach { case (varId, value) =>
        val name = varId.toString
        val v = ErgoAlgos.encode(ValueSerializer.serialize(value))
        extension.put(name, v)
      }
      new SpendingProof()
        .proofBytes(proof)
        .extension(extension)
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

  implicit val isoErgoTransactionUnsignedInput: Iso[ErgoTransactionUnsignedInput, UnsignedInput] = new Iso[ErgoTransactionUnsignedInput, UnsignedInput] {
    override def to(ergoTransactionInput: ErgoTransactionUnsignedInput) =
      new UnsignedInput(
        ADKey @@ ErgoAlgos.decodeUnsafe(ergoTransactionInput.getBoxId))

    override def from(input: UnsignedInput): ErgoTransactionUnsignedInput =
      new ErgoTransactionUnsignedInput()
          .boxId(ErgoAlgos.encode(input.boxId))
  }

  implicit val isoAssetToErgoToken: Iso[Asset, ErgoToken] = new Iso[Asset, ErgoToken] {
    override def to(a: Asset): ErgoToken = new ErgoToken(a.getTokenId, a.getAmount)
    override def from(t: ErgoToken): Asset = new Asset().tokenId(t.getId.toString).amount(t.getValue)
  }

  implicit val isoAssetToPair: Iso[Asset, (TokenId, Long)] = new Iso[Asset, (TokenId, Long)] {
    override def to(a: Asset) = (a.getTokenId.toBytes.toTokenId, a.getAmount)
    override def from(t: (TokenId, Long)): Asset = new Asset().tokenId(ErgoAlgos.encode(t._1)).amount(t._2)
  }

  implicit val isoExplorerAssetToPair: Iso[EAsset, (TokenId, Long)] = new Iso[EAsset, (TokenId, Long)] {
    override def to(a: EAsset) = (a.getTokenId.toBytes.toTokenId, a.getAmount)
    override def from(t: (TokenId, Long)): EAsset = new EAsset().tokenId(ErgoAlgos.encode(t._1)).amount(t._2)
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

  implicit val isoRegistersToMap: Iso[Registers, AdditionalRegisters] = new Iso[Registers, AdditionalRegisters] {
    override def to(regs: Registers): AdditionalRegisters = {
      regs.asScala.map { r =>
        val id = ErgoBox.registerByName(r._1).asInstanceOf[NonMandatoryRegisterId]
        val v = ValueSerializer.deserialize(ErgoAlgos.decodeUnsafe(r._2))
        (id, v.asInstanceOf[EvaluatedValue[_ <: SType]])
      }.toMap
    }
    override def from(ergoRegs: AdditionalRegisters): Registers = {
      val res = new Registers()
      ergoRegs.foreach { case (id, value) =>
        val name = id.toString()
        val v = ErgoAlgos.encode(ValueSerializer.serialize(value))
        res.put(name, v)
      }
      res
    }
  }

  implicit val isoExplRegistersToMap: Iso[ERegisters, AdditionalRegisters] = new Iso[ERegisters, AdditionalRegisters] {
    override def to(regs: ERegisters): AdditionalRegisters = {
      regs.asScala.map { r =>
        val id = ErgoBox.registerByName(r._1).asInstanceOf[NonMandatoryRegisterId]
        val v = ValueSerializer.deserialize(ErgoAlgos.decodeUnsafe(r._2.serializedValue))
        (id, v.asInstanceOf[EvaluatedValue[_ <: SType]])
      }.toMap
    }
    override def from(ergoRegs: AdditionalRegisters): ERegisters = {
      val res = new ERegisters()
      ergoRegs.foreach { case (id, value) =>
        val name = id.toString()
        val v = ErgoAlgos.encode(ValueSerializer.serialize(value))
        val reg = new AdditionalRegister
        reg.serializedValue = v
        res.put(name, reg)
      }
      res
    }
  }

  implicit val isoErgoTransactionOutput: Iso[ErgoTransactionOutput, ErgoBox] = new Iso[ErgoTransactionOutput, ErgoBox] {
    override def to(boxData: ErgoTransactionOutput): ErgoBox = {
      val tree = isoStringToErgoTree.to(boxData.getErgoTree)
      val tokenPairs = boxData.getAssets.asScala.map(isoAssetToPair.to(_)).toIndexedSeq
      val tokens = sigma.Colls.fromItems(tokenPairs:_*)
      val regs = isoRegistersToMap.to(boxData.getAdditionalRegisters)
      new ErgoBox(boxData.getValue, tree,
        tokens, regs,
        ModifierId @@ boxData.getTransactionId,
        boxData.getIndex.shortValue,
        boxData.getCreationHeight)
    }

    override def from(box: ErgoBox): ErgoTransactionOutput = {
      val assets = box.additionalTokens.toArray.map { case (id, value) =>
        new Asset().tokenId(ErgoAlgos.encode(id)).amount(value)
      }.toList.asJava
      val regs = isoRegistersToMap.from(box.additionalRegisters)
      val out = new ErgoTransactionOutput()
          .boxId(ErgoAlgos.encode(box.id))
          .value(box.value)
          .ergoTree(ErgoAlgos.encode(TreeSerializer.serializeErgoTree(box.ergoTree)))
          .assets(assets)
          .additionalRegisters(regs)
          .creationHeight(box.creationHeight)
          .transactionId(box.transactionId)
          .index(Integer.valueOf(box.index))
      out
    }
  }

  implicit val isoExplTransactionOutput: Iso[OutputInfo, ErgoBox] = new Iso[OutputInfo, ErgoBox] {
    override def to(boxData: OutputInfo): ErgoBox = {
      val tree = isoStringToErgoTree.to(boxData.getErgoTree)
      val tokenPairs = boxData.getAssets.asScala.toIndexedSeq.sortBy(_.getIndex)
        .map(asset => new ErgoToken(asset.getTokenId, asset.getAmount))
        .map(t => (sigma.Colls.fromArray(t.getId.getBytes).asInstanceOf[TokenId], t.getValue))
      val tokens = sigma.Colls.fromItems(tokenPairs:_*)
      val regs = isoExplRegistersToMap.to(boxData.getAdditionalRegisters)
      new ErgoBox(boxData.getValue, tree,
        tokens, regs,
        ModifierId @@ boxData.getTransactionId,
        boxData.getIndex.shortValue,
        boxData.getCreationHeight)
    }

    override def from(box: ErgoBox): OutputInfo = {
      val assets = box.additionalTokens.toArray.zipWithIndex.map { case ((id, value), idx) =>
        new AssetInstanceInfo().tokenId(ErgoAlgos.encode(id)).amount(value).index(idx)
      }.toList.asJava
      val regs = isoExplRegistersToMap.from(box.additionalRegisters)
      val out = new OutputInfo()
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
        h.getVersion.toByte,
        h.getParentId.toColl,
        h.getAdProofsRoot.toColl,
        h.getStateRoot.toColl,
        h.getTransactionsRoot.toColl,
        h.getTimestamp(),
        h.getNBits(),
        h.getHeight,
        h.getExtensionHash.toColl,
        h.getPowSolutions.getPk.toGroupElement,
        h.getPowSolutions.getW.toGroupElement,
        h.getPowSolutions.getN.toColl,
        SigmaDsl.BigInt(h.getPowSolutions.getD),
        h.getVotes.toColl,
        sigma.Colls.fromArray(Array.emptyByteArray)
      )

    override def from(a: Header): BlockHeader = ???
  }

  def toSigmaHeader(h: org.ergoplatform.appkit.BlockHeader): Header =
      CHeader(
        h.getVersion,
        h.getParentId.map(b => b.toByte),
        h.getAdProofsRoot.map(b => b.toByte),
        h.getStateRoot.digest,
        h.getTransactionsRoot.map(b => b.toByte),
        h.getTimestamp,
        h.getNBits,
        h.getHeight,
        h.getExtensionHash.map(b => b.toByte),
        h.getPowSolutionsPk,
        h.getPowSolutionsW,
        h.getPowSolutionsN.map(b => b.toByte),
        SigmaDsl.BigInt(h.getPowSolutionsD),
        h.getVotes.map(b => b.toByte),
        sigma.Colls.fromArray(Array.emptyByteArray)
      )

  implicit val isoErgoTransaction: Iso[ErgoTransaction, ErgoLikeTransaction] = new Iso[ErgoTransaction, ErgoLikeTransaction] {
    override def to(apiTx: ErgoTransaction): ErgoLikeTransaction =
      new ErgoLikeTransaction(
        apiTx.getInputs.asScala.map(isoErgoTransactionInput.to(_)).toIndexedSeq,
        apiTx.getDataInputs.asScala.map(isoErgoTransactionDataInput.to(_)).toIndexedSeq,
        apiTx.getOutputs.asScala.map(isoErgoTransactionOutput.to(_)).toIndexedSeq
      )

    override def from(tx: ErgoLikeTransaction): ErgoTransaction =
      new ErgoTransaction()
        .id(tx.id)
        .inputs(tx.inputs.map(isoErgoTransactionInput.from(_)).toList.asJava)
        .dataInputs(tx.dataInputs.map(isoErgoTransactionDataInput.from(_)).toList.asJava)
        .outputs(tx.outputs.map(isoErgoTransactionOutput.from(_)).toList.asJava)
  }

  implicit val isoUnsignedErgoTransaction: Iso[UnsignedErgoTransaction, UnsignedErgoLikeTransaction] = new Iso[UnsignedErgoTransaction, UnsignedErgoLikeTransaction] {
    override def to(apiTx: UnsignedErgoTransaction): UnsignedErgoLikeTransaction =
      new UnsignedErgoLikeTransaction(
        apiTx.getInputs.asScala.map(isoErgoTransactionUnsignedInput.to(_)).toIndexedSeq,
        apiTx.getDataInputs.asScala.map(isoErgoTransactionDataInput.to(_)).toIndexedSeq,
        apiTx.getOutputs.asScala.map(isoErgoTransactionOutput.to(_)).toIndexedSeq
      )

    override def from(tx: UnsignedErgoLikeTransaction): UnsignedErgoTransaction =
      new UnsignedErgoTransaction()
        .id(tx.id)
        .inputs(tx.inputs.map(isoErgoTransactionUnsignedInput.from(_)).toList.asJava)
        .dataInputs(tx.dataInputs.map(isoErgoTransactionDataInput.from(_)).toList.asJava)
        .outputs(tx.outputs.map(isoErgoTransactionOutput.from(_)).toList.asJava)
  }
}
