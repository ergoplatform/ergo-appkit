package org.ergoplatform.appkit.impl

import org.ergoplatform._
import org.ergoplatform.appkit.{Iso, _}
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext
import org.ergoplatform.wallet.transactions.TransactionBuilder
import org.ergoplatform.wallet.boxes.{BoxSelector, DefaultBoxSelector}

import special.collection.Coll
import special.sigma.Header
import java.util._
import java.util.stream.Collectors

import org.ergoplatform.appkit.Parameters.MinChangeValue
import org.ergoplatform.appkit.Parameters.MinFee
import scorex.crypto.authds.ADDigest

import scala.collection.JavaConversions
import JavaHelpers._

class UnsignedTransactionBuilderImpl(val _ctx: BlockchainContextImpl) extends UnsignedTransactionBuilder {
  private[impl] val _inputs: List[UnsignedInput] = new ArrayList[UnsignedInput]
  private[impl] val _dataInputs: List[DataInput] = new ArrayList[DataInput]
  private[impl] var _outputCandidates: List[ErgoBoxCandidate] = new ArrayList[ErgoBoxCandidate]
  private var _inputBoxes: List[InputBoxImpl] = null
  private var _dataInputBoxes: List[InputBoxImpl] = new ArrayList[InputBoxImpl]
  private val _tokensToBurn: List[ErgoToken] = new ArrayList[ErgoToken]
  private var _feeAmount = 0L
  private var _changeAddress: ErgoAddress = null
  private var _ph: PreHeaderImpl = null

  override def preHeader(ph: PreHeader): UnsignedTransactionBuilder = {
    require(_ph == null, "PreHeader is already specified")
    _ph = ph.asInstanceOf[PreHeaderImpl]
    this
  }

  override def boxesToSpend(inputBoxes: List[InputBox]): UnsignedTransactionBuilder = {
    val items = inputBoxes
      .map(box => JavaHelpers.createUnsignedInput(box.getId.getBytes))
    _inputs.addAll(items)
    _inputBoxes = inputBoxes.map(b => b.asInstanceOf[InputBoxImpl])
    this
  }

  override def withDataInputs(inputBoxes: List[InputBox]): UnsignedTransactionBuilder = {
    val items = inputBoxes
      .map(box => JavaHelpers.createDataInput(box.getId.getBytes))
    _dataInputs.addAll(items)
    _dataInputBoxes = inputBoxes.map(_.asInstanceOf[InputBoxImpl])
    this
  }

  override def outputs(outputs: OutBox*): UnsignedTransactionBuilder = {
    require(_outputCandidates.isEmpty, "Outputs already specified.")
    _outputCandidates = new ArrayList[ErgoBoxCandidate]
    appendOutputs(outputs:_*)
    this
  }

  override def fee(feeAmount: Long): UnsignedTransactionBuilder = {
    require(_feeAmount == 0, "Fee already defined")
    _feeAmount = feeAmount
    this
  }

  override def tokensToBurn(tokens: ErgoToken*): UnsignedTransactionBuilder = {
    Collections.addAll(_tokensToBurn, tokens:_*)
    this
  }

  private def appendOutputs(outputs: OutBox*): Unit = {
    val boxes = outputs.map(c => c.asInstanceOf[OutBoxImpl].getErgoBoxCandidate)
    Collections.addAll(_outputCandidates, boxes:_*)
  }

  override def sendChangeTo(changeAddress: ErgoAddress): UnsignedTransactionBuilder = {
    require(_changeAddress == null, "Change address is already specified")
    _changeAddress = changeAddress
    this
  }

  override def build: UnsignedTransaction = {
    val boxesToSpend = _inputBoxes
      .map(b => ExtendedInputBox(b.getErgoBox, b.getExtension))
    val dataInputBoxes = _dataInputBoxes.map(b => b.getErgoBox)
    val dataInputs = JavaHelpers.toIndexedSeq(_dataInputs)
    require(_feeAmount > 0, "Fee amount should be defined (using fee() method).")
    require(_feeAmount >= MinFee, "Fee amount should be >= " + MinFee + ", got " + _feeAmount)
    require(_changeAddress != null, "Change address is not defined")
    val outputCandidates = JavaHelpers.toIndexedSeq(_outputCandidates)
    val inputBoxes = JavaHelpers.toIndexedSeq(boxesToSpend.map(eb => eb.box))
    val burnTokens = JavaHelpers.createTokensMap(Iso.isoJListErgoTokenToMapPair.to(_tokensToBurn))
    val tx = TransactionBuilder.buildUnsignedTx(
      inputs = inputBoxes, dataInputs = dataInputs, outputCandidates = outputCandidates,
      currentHeight = _ctx.getHeight, feeAmount = _feeAmount,
      changeAddress = _changeAddress, minChangeValue = MinChangeValue,
      minerRewardDelay = Parameters.MinerRewardDelay, burnTokens = burnTokens,
      boxSelector = DefaultBoxSelector).get
    val stateContext = createErgoLikeStateContext
    new UnsignedTransactionImpl(tx, boxesToSpend, dataInputBoxes, stateContext)
  }

  private def createErgoLikeStateContext: ErgoLikeStateContext = new ErgoLikeStateContext() {
    private val _allHeaders = Iso.JListToColl(
      ScalaBridge.isoBlockHeader,
      ErgoType.headerType.getRType).to(_ctx.getHeaders)

    private val _headers = _allHeaders.slice(1, _allHeaders.length)

    private val _preHeader = if (_ph == null) _ctx._preHeader._ph else _ph._ph

    override def sigmaLastHeaders: Coll[Header] = _headers

    override def previousStateDigest: ADDigest =
      ADDigest @@ JavaHelpers.getStateDigest(_headers.apply(0).stateRoot)

    override def sigmaPreHeader: special.sigma.PreHeader = _preHeader
  }

  override def getCtx: BlockchainContext = _ctx

  override def getPreHeader: PreHeader =
    if (_ph == null) _ctx.getPreHeader else _ph

  override def outBoxBuilder = new OutBoxBuilderImpl(this)

  override def getNetworkType: NetworkType = _ctx.getNetworkType

  override def getInputBoxes: List[InputBox] =
    _inputBoxes.stream.collect(Collectors.toList[InputBox])
}

