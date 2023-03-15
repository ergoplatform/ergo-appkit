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
import scala.collection.JavaConverters
import scala.collection.JavaConverters.iterableAsScalaIterable

class UnsignedTransactionBuilderImpl(val _ctx: BlockchainContextImpl) extends UnsignedTransactionBuilder {
  private[impl] val _inputs: List[InputBoxImpl] = new ArrayList[InputBoxImpl]()
  private[impl] val _outputs: List[OutBoxImpl] = new ArrayList[OutBoxImpl]()
  private[impl] val _dataInputs: List[InputBoxImpl] = new ArrayList[InputBoxImpl]()

  private var _tokensToBurn: Option[List[ErgoToken]] = None
  private var _feeAmount: Option[Long] = None
  private var _changeAddress: Option[ErgoAddress] = None
  private var _ph: Option[PreHeaderImpl] = None

  override def preHeader(ph: PreHeader): UnsignedTransactionBuilder = {
    require(_ph.isEmpty, "PreHeader is already specified")
    _ph = Some(ph.asInstanceOf[PreHeaderImpl])
    this
  }

  override def addInputs(boxes: InputBox*): UnsignedTransactionBuilder = {
    boxes.foreach { case b: InputBoxImpl =>
      _inputs.add(b)
    }
    this
  }

  override def boxesToSpend(inputBoxes: List[InputBox]): UnsignedTransactionBuilder = {
    require(_inputs.isEmpty, "inputs already specified")
    addInputs(JavaHelpers.toIndexedSeq(inputBoxes): _*)
    this
  }

  override def addDataInputs(boxes: InputBox*): UnsignedTransactionBuilder = {
    boxes.foreach { case b: InputBoxImpl =>
      _dataInputs.add(b)
    }
    this
  }

  override def withDataInputs(inputBoxes: List[InputBox]): UnsignedTransactionBuilder = {
    require(_dataInputs.isEmpty, "dataInputs list is already specified")
    addDataInputs(JavaHelpers.toIndexedSeq(inputBoxes): _*)
    this
  }

  override def addOutputs(outBoxes: OutBox*): UnsignedTransactionBuilder = {
    outBoxes.foreach { case b: OutBoxImpl =>
      _outputs.add(b)
    }
    this
  }

  override def outputs(outputs: OutBox*): UnsignedTransactionBuilder = {
    require(_outputs.isEmpty, "Outputs already specified.")
    addOutputs(outputs: _*)
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
      Collections.addAll(res, tokens: _*)
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
        throw new IllegalArgumentException("requirement failed: " + msg)
    }
  }

  def getDefined[T](opt: Option[T], msg: => String): T = {
    opt match {
      case Some(x) => x
      case _ =>
        throw new IllegalArgumentException("requirement failed: " + msg)
    }
  }

  override def build: UnsignedTransaction = {
    val inputBoxes = _inputs
    val outputCandidates = _outputs.map(c => c.getErgoBoxCandidate)
    require(!outputCandidates.isEmpty, "Output boxes are not specified")
    val boxesToSpend = inputBoxes
      .map(b => ExtendedInputBox(b.getErgoBox, b.getExtension))
    val dataInputBoxes = _dataInputs.map(b => b.getErgoBox)
    val dataInputs = JavaHelpers.toIndexedSeq(_dataInputs)
      .map(box => JavaHelpers.createDataInput(box.getId.getBytes))
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
    private val _allHeaders = Colls.fromArray(JavaConverters.asScalaIterator(
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

  override def getInputBoxes: List[InputBox] =
    _inputs.map(b => b.asInstanceOf[InputBox])

  override def getOutputBoxes: util.List[OutBox] =
    _outputs.map(b => b.asInstanceOf[OutBox])
}

