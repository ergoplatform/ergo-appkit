package org.ergoplatform.appkit.impl

import java.util

import org.ergoplatform._
import org.ergoplatform.appkit.{Iso, _}
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext
import org.ergoplatform.wallet.transactions.TransactionBuilder
import org.ergoplatform.wallet.boxes.DefaultBoxSelector
import special.collection.Coll
import special.sigma.Header
import java.util._
import java.util.stream.Collectors

import org.ergoplatform.appkit.Parameters.{MinChangeValue, MinFee}
import scorex.crypto.authds.ADDigest
import org.ergoplatform.appkit.JavaHelpers._

class UnsignedTransactionBuilderImpl(val _ctx: BlockchainContextImpl) extends UnsignedTransactionBuilder {
  private[impl] var _inputs: List[UnsignedInput] = _
  private var _inputBoxes: Option[List[InputBoxImpl]] = None

  private[impl] var _dataInputs: List[DataInput] = new ArrayList[DataInput]()
  private var _dataInputBoxes: Option[List[InputBoxImpl]] = None

  private[impl] var _outputCandidates: Option[List[ErgoBoxCandidate]] = None
  private var _tokensToBurn: Option[List[ErgoToken]] = None
  private var _feeAmount: Option[Long] = None
  private var _changeAddress: Option[ErgoAddress] = None
  private var _ph: Option[PreHeaderImpl] = None

  override def preHeader(ph: PreHeader): UnsignedTransactionBuilder = {
    require(_ph.isEmpty, "PreHeader is already specified")
    _ph = Some(ph.asInstanceOf[PreHeaderImpl])
    this
  }

  override def boxesToSpend(inputBoxes: List[InputBox]): UnsignedTransactionBuilder = {
    require(_inputBoxes.isEmpty, "boxesToSpend list is already specified")
    _inputs = inputBoxes
      .map(box => JavaHelpers.createUnsignedInput(box.getId.getBytes))
    _inputBoxes = Some(inputBoxes.map(b => b.asInstanceOf[InputBoxImpl]))
    this
  }

  override def withDataInputs(inputBoxes: List[InputBox]): UnsignedTransactionBuilder = {
    require(_dataInputBoxes.isEmpty, "dataInputs list is already specified")
    _dataInputs = inputBoxes
      .map(box => JavaHelpers.createDataInput(box.getId.getBytes))
    _dataInputBoxes = Some(inputBoxes.map(_.asInstanceOf[InputBoxImpl]))
    this
  }

  override def outputs(outputs: OutBox*): UnsignedTransactionBuilder = {
    require(_outputCandidates.isEmpty, "Outputs already specified.")
    val candidates = outputs
      .map(c => c.asInstanceOf[OutBoxImpl].getErgoBoxCandidate)
      .toIndexedSeq.asInstanceOf[IndexedSeq[ErgoBoxCandidate]]
      .convertTo[List[ErgoBoxCandidate]]
    _outputCandidates = Some(candidates)
    this
  }

  override def fee(feeAmount: Long): UnsignedTransactionBuilder = {
    require(_feeAmount.isEmpty, "Fee already defined")
    _feeAmount = Some(feeAmount)
    this
  }

  override def tokensToBurn(tokens: ErgoToken*): UnsignedTransactionBuilder = {
    require(_tokensToBurn.isEmpty, "Tokens to burn already specified.")
    _tokensToBurn = Some({
      val res = new util.ArrayList[ErgoToken]()
      Collections.addAll(res, tokens:_*)
      res
    })
    this
  }

  override def sendChangeTo(changeAddress: ErgoAddress): UnsignedTransactionBuilder = {
    require(_changeAddress.isEmpty, "Change address is already specified")
    _changeAddress = Some(changeAddress)
    this
  }

  def getNonEmpty[T](list: Option[List[T]], msg: => String): List[T] = {
    list match {
      case Some(list) if !list.isEmpty => list
      case _ =>
        throw new IllegalArgumentException("requirement failed: "+ msg)
    }
  }

  def getDefined[T](opt: Option[T], msg: => String): T = {
    opt match {
      case Some(x) => x
      case _ =>
        throw new IllegalArgumentException("requirement failed: "+ msg)
    }
  }

  override def build: UnsignedTransaction = {
    val inputBoxes = getInputBoxesImpl
    val outputCandidates = getNonEmpty(_outputCandidates, "Output boxes are not specified")
    val boxesToSpend = inputBoxes
      .map(b => ExtendedInputBox(b.getErgoBox, b.getExtension))
    val dataInputBoxes = _dataInputBoxes
       .getOrElse(new util.ArrayList[InputBoxImpl]())
       .map(b => b.getErgoBox)
    val dataInputs = JavaHelpers.toIndexedSeq(_dataInputs)
    require(_feeAmount.isEmpty || _feeAmount.get >= MinFee,
      s"When fee amount is defined it should be >= $MinFee, got ${_feeAmount.get}")
    val changeAddress = getDefined(_changeAddress, "Change address is not defined")
    val outputCandidatesSeq = JavaHelpers.toIndexedSeq(outputCandidates)
    val inputBoxesSeq = JavaHelpers.toIndexedSeq(boxesToSpend.map(eb => eb.box))
    val burnTokens = JavaHelpers.createTokensMap(
      Iso.isoJListErgoTokenToMapPair.to(_tokensToBurn.getOrElse(new ArrayList[ErgoToken]))
    )
    val tx = TransactionBuilder.buildUnsignedTx(
      inputs = inputBoxesSeq, dataInputs = dataInputs, outputCandidates = outputCandidatesSeq,
      currentHeight = _ctx.getHeight, createFeeOutput = _feeAmount,
      changeAddress = changeAddress, minChangeValue = MinChangeValue,
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

    private val _preHeader = _ph match {
      case Some(ph) => ph._ph
      case _ => _ctx._preHeader._ph
    }

    override def sigmaLastHeaders: Coll[Header] = _headers

    override def previousStateDigest: ADDigest =
      ADDigest @@ JavaHelpers.getStateDigest(_headers.apply(0).stateRoot)

    override def sigmaPreHeader: special.sigma.PreHeader = _preHeader
  }

  override def getCtx: BlockchainContext = _ctx

  override def getPreHeader: PreHeader = _ph.getOrElse(_ctx.getPreHeader)

  override def outBoxBuilder = new OutBoxBuilderImpl(this)

  override def getNetworkType: NetworkType = _ctx.getNetworkType

  private def getInputBoxesImpl: List[InputBoxImpl] =
    getNonEmpty(_inputBoxes, "Input boxes are not specified")

  override def getInputBoxes: List[InputBox] =
    getInputBoxesImpl.stream.collect(Collectors.toList[InputBox])
}

