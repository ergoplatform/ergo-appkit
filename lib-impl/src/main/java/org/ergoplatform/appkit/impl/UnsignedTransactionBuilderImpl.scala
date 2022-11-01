package org.ergoplatform.appkit.impl

import org.ergoplatform._
import org.ergoplatform.appkit.JavaHelpers._
import org.ergoplatform.appkit.Parameters.{MinChangeValue, MinFee}
import org.ergoplatform.appkit._
import org.ergoplatform.wallet.protocol.context.ErgoLikeStateContext
import org.ergoplatform.wallet.transactions.TransactionBuilder
import scorex.crypto.authds.ADDigest
import sigmastate.eval.Colls
import special.collection.Coll
import special.sigma.Header

import java.util
import java.util._
import java.util.stream.Collectors
import scala.collection.JavaConversions

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

  override def sendChangeTo(changeAddress: Address): UnsignedTransactionBuilder = {
    sendChangeTo(changeAddress.getErgoAddress)
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
    val boxesToSpendSeq = JavaHelpers.toIndexedSeq(boxesToSpend)
    val inputBoxesSeq = boxesToSpendSeq.map(eb => eb.box)
    val requestedToBurn = _tokensToBurn.getOrElse(new ArrayList[ErgoToken])
    val burnTokens = JavaHelpers.createTokensMap(
      Iso.isoJListErgoTokenToMapPair.to(requestedToBurn))
    val rewardDelay = if (_ctx.getNetworkType == NetworkType.MAINNET)
      Parameters.MinerRewardDelay_Mainnet
    else
      Parameters.MinerRewardDelay_Testnet

    val tx = TransactionBuilder.buildUnsignedTx(
      inputs = inputBoxesSeq, dataInputs = dataInputs, outputCandidates = outputCandidatesSeq,
      currentHeight = _ctx.getHeight, createFeeOutput = _feeAmount,
      changeAddress = changeAddress, minChangeValue = MinChangeValue,
      minerRewardDelay = rewardDelay,
      burnTokens = burnTokens,
      boxSelector = new InputBoxesValidator()).get

    // the method above don't accept ContextExtension along with inputs, thus, after the
    // transaction has been built we need to zip with the extensions that have been
    // attached to the inputBoxes
    val txWithExtensions = new UnsignedErgoLikeTransaction(
      inputs = boxesToSpendSeq.map(_.toUnsignedInput),
      tx.dataInputs, tx.outputCandidates
    )

    val stateContext = createErgoLikeStateContext
    new UnsignedTransactionImpl(txWithExtensions, boxesToSpend, dataInputBoxes, changeAddress, stateContext, _ctx, requestedToBurn)
  }

  private[appkit] def createErgoLikeStateContext: ErgoLikeStateContext = new ErgoLikeStateContext() {
    private val _allHeaders = Colls.fromArray(JavaConversions.asScalaIterator(
      _ctx.getHeaders.iterator).map(h => ScalaBridge.toSigmaHeader(h)).toArray)

    private val _headers = _allHeaders.slice(1, _allHeaders.length)

    private val _preHeader = _ph match {
      case Some(ph) => ph._ph
      case _ => JavaHelpers.toPreHeader(_allHeaders.apply(0))
    }

    override def sigmaLastHeaders: Coll[Header] = _headers

    override def previousStateDigest: ADDigest =
      ADDigest @@ JavaHelpers.getStateDigest(_headers.apply(0).stateRoot)

    override def sigmaPreHeader: special.sigma.PreHeader = _preHeader
  }

  override def getCtx: BlockchainContext = _ctx

  override def getPreHeader: PreHeader = _ph.getOrElse(_ctx.getHeaders.get(0))

  override def outBoxBuilder = new OutBoxBuilderImpl(this)

  override def getNetworkType: NetworkType = _ctx.getNetworkType

  private def getInputBoxesImpl: List[InputBoxImpl] =
    getNonEmpty(_inputBoxes, "Input boxes are not specified")

  override def getInputBoxes: List[InputBox] =
    getInputBoxesImpl.stream.collect(Collectors.toList[InputBox])
}

