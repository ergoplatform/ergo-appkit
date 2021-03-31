package org.ergoplatform.appkit.impl

import com.google.common.base.Preconditions
import java.nio.charset.StandardCharsets

import com.google.common.base.Preconditions.checkState
import org.ergoplatform.appkit._

import scala.collection.mutable.ArrayBuffer

class OutBoxBuilderImpl(val _ctx: BlockchainContextImpl,
                        val _txB: UnsignedTransactionBuilderImpl) extends OutBoxBuilder {
  private var _value: Long = 0
  private var _contract: ErgoContract = _
  private val _tokens = ArrayBuffer.empty[ErgoToken]
  private val _registers = ArrayBuffer.empty[ErgoValue[_]]

  override def value(value: Long): OutBoxBuilderImpl = {
    _value = value
    this
  }

  override def contract(contract: ErgoContract): OutBoxBuilderImpl = {
    _contract = contract
    this
  }

  override def tokens(tokens: ErgoToken*): OutBoxBuilderImpl = {
    Preconditions.checkArgument(tokens.nonEmpty,
      "At least one token should be specified": Any)
    _tokens ++= tokens
    this
  }

  override def mintToken(token: ErgoToken,
                         tokenName: String,
                         tokenDescription: String,
                         tokenNumberOfDecimals: Int): OutBoxBuilder = {
    val utf8 = StandardCharsets.UTF_8
    val tokenNameVal = ErgoValue.of(tokenName.getBytes(utf8))
    val tokenDescVal = ErgoValue.of(tokenDescription.getBytes(utf8))
    val tokenNumOfDecVal = ErgoValue.of(Integer.toString(tokenNumberOfDecimals).getBytes(utf8))
    _registers ++= Array(tokenNameVal, tokenDescVal, tokenNumOfDecVal)
    _tokens += token
    this
  }

  override def registers(registers: ErgoValue[_]*): OutBoxBuilderImpl = {
    Preconditions.checkArgument(registers.nonEmpty,
      "At least one register should be specified": Any)
    _registers ++= registers
    this
  }

  override def build: OutBox = {
    checkState(_contract != null, "Contract is not defined": Any)
    val tree = _contract.getErgoTree
    // TODO pass user specified creationHeight
    val ergoBoxCandidate = JavaHelpers.createBoxCandidate(_value, tree, _tokens, _registers, _txB.getCtx.getHeight)
    new OutBoxImpl(_ctx, ergoBoxCandidate)
  }
}

