package org.ergoplatform.appkit

import org.ergoplatform.appkit.impl.{BlockchainContextImpl, InputBoxImpl, UnsignedTransactionBuilderImpl, UnsignedTransactionImpl}
import org.ergoplatform.sdk.{ErgoToken, ExtendedInputBox, JavaHelpers, SecretString, TokenBalanceException}
import org.ergoplatform.sdk.JavaHelpers._
import sigma.data.Iso._
import org.ergoplatform.settings.ErgoAlgos
import org.ergoplatform.{ErgoBox, ErgoTreePredef, UnsignedErgoLikeTransaction}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import sigmastate.TestsBase
import scorex.util.ModifierId
import sigmastate.helpers.TestingHelpers.createBox
import java.util
import java.util.{Collections, List => JList}
import scala.collection.JavaConverters._

class AppkitProvingInterpreterSpec extends AnyPropSpec
  with Matchers
  with ScalaCheckDrivenPropertyChecks
  with AppkitTestingCommon
  with HttpClientTesting
  with TestsBase {

  val oneErg = 1000L * 1000 * 1000

  def createBoxOps(ctx: BlockchainContext, prover: ErgoProver, inputs: IndexedSeq[ErgoBox]) = {
    val ops = new BoxOperations(ctx, Collections.singletonList(prover.getAddress), prover) {
      override def loadTop(): util.List[InputBox] = {
        val is: JList[InputBox] = inputs.map(b => new InputBoxImpl(b): InputBox).asJava
        is
      }
    }
    ops.withAmountToSpend(oneErg * 2)
  }

  /** This method creates an UnsignedTransaction instance directly bypassing builders and
    * their consistency logic. This allows to create invalid transactions for the tests
    * below.
    */
  def createUnsignedTransaction(
    ctx: BlockchainContext, prover: ErgoProver,
    inputs: IndexedSeq[ErgoBox],
    outputs: IndexedSeq[ErgoBox],
    tokensToBurn: IndexedSeq[ErgoToken]
  ) = {
    val txB = ctx.newTxBuilder().asInstanceOf[UnsignedTransactionBuilderImpl]
    val stateContext = txB.createErgoLikeStateContext
    val changeAddress = prover.getAddress.getErgoAddress

    val extendedInputs: IndexedSeq[ExtendedInputBox] =
        inputs
          .map(b => new InputBoxImpl(b))
          .map(b => ExtendedInputBox(b.getErgoBox, b.getExtension))

    val boxesToSpend: JList[ExtendedInputBox] = extendedInputs.asJava
    val boxesToSpendSeq = JavaHelpers.toIndexedSeq(boxesToSpend)
    val tx = new UnsignedErgoLikeTransaction(
      inputs = boxesToSpendSeq.map(_.toUnsignedInput),
      dataInputs = IndexedSeq(),
      outputCandidates = outputs
    )
    val unsigned = new UnsignedTransactionImpl(
      tx, boxesToSpend, new util.ArrayList[ErgoBox](), changeAddress, stateContext,
      ctx.asInstanceOf[BlockchainContextImpl], tokensToBurn.asJava)
    unsigned
  }

  property("rejecting transaction with unbalanced tokens") {
    val ergoClient = createMockedErgoClient(MockData(Nil, Nil))
    ergoClient.execute { ctx: BlockchainContext =>
      val prover = ctx.newProverBuilder()
        .withMnemonic(mnemonic, SecretString.empty(), false)
        .build()
      val tree1 = ErgoTreePredef.TrueProp(ergoTreeHeaderInTests)
      val tree2 = ErgoTreePredef.FalseProp(ergoTreeHeaderInTests)
      val id1Bytes = ErgoAlgos.hash("id1")
      val id2Bytes = ErgoAlgos.hash("id2")
      val token1: org.ergoplatform.ErgoBox.Token = (sigma.data.Digest32Coll @@ sigma.Colls.fromArray(id1Bytes), 10L)
      val token2: org.ergoplatform.ErgoBox.Token = (sigma.data.Digest32Coll @@ sigma.Colls.fromArray(id2Bytes), 20L)
      val ergoToken1 = new ErgoToken(id1Bytes, token1._2)
      val ergoToken2 = new ErgoToken(id2Bytes, token2._2)

      val input1 = createBox(oneErg + Parameters.MinFee, tree1, additionalTokens = Seq(token1))
      val input2 = createBox(oneErg, tree2, additionalTokens = Seq(token2))

      // successful reduction with balanced tokens
      {
        val ops = createBoxOps(ctx, prover, IndexedSeq(input1, input2))
        val tokens = new util.ArrayList[ErgoToken]()
        tokens.add(ergoToken1)
        tokens.add(ergoToken2)
        val unsigned = ops
          .withTokensToSpend(tokens)
          .putToContractTxUnsigned(address.toErgoContract)
        val reduced = prover.reduce(unsigned, 0)
        reduced.getInputBoxesIds.size() shouldBe 2
        reduced.getOutputs.size() shouldBe 2  // output + feeOut
      }

      // Transaction tries to burn tokens when no burning was requested
      {
        val output1 = createBox(oneErg * 2 + Parameters.MinFee, tree1, additionalTokens = Seq(token1))
        val unsigned = createUnsignedTransaction(ctx, prover, IndexedSeq(input1, input2), IndexedSeq(output1), IndexedSeq.empty)
        val ex = intercept[TokenBalanceException] {
          prover.reduce(unsigned, 0)
        }
        ex.getMessage should include("Transaction tries to burn tokens when no burning was requested")
        ex.tokensDiff.exists(t => t == (token2._1, -token2._2)) shouldBe true
      }

      // Transaction tries to burn tokens when no burning was requested
      {
        val output1 = createBox(oneErg * 2 + Parameters.MinFee, tree1, additionalTokens = Seq(token1, token2.copy(_2 = 10)))
        val unsigned = createUnsignedTransaction(ctx, prover, IndexedSeq(input1, input2), IndexedSeq(output1), IndexedSeq.empty)
        val ex = intercept[TokenBalanceException] {
          prover.reduce(unsigned, 0)
        }
        ex.getMessage should include("Transaction tries to burn tokens when no burning was requested")
        ex.tokensDiff.exists(t => t == (token2._1, -10)) shouldBe true
      }

      // Transaction tries to burn tokens when no burning was requested
      // Inputs: (note, same token in two boxes)
      // Box1: Token1, Amount1
      // Box2: Token1. Amount2
      //
      // Outputs:
      // Box1: Token1, Amount1
      {
        // another input with the same token as input1
        val input2_with_token1 = createBox(oneErg, tree2, additionalTokens = Seq(token1.copy(_2 = 20)))
        val output1 = createBox(oneErg * 2 + Parameters.MinFee, tree1, additionalTokens = Seq(token1))

        val unsigned = createUnsignedTransaction(ctx, prover,
          IndexedSeq(input1, input2_with_token1),
          IndexedSeq(output1), tokensToBurn = IndexedSeq.empty)

        val ex = intercept[TokenBalanceException] {
          prover.reduce(unsigned, 0)
        }
        ex.getMessage should include("Transaction tries to burn tokens when no burning was requested")
        ex.tokensDiff.exists(t => t == (token1._1, -20)) shouldBe true
      }

      // invalid burning even when burning was requested
      {
        val output1 = createBox(oneErg * 2 + Parameters.MinFee, tree1, additionalTokens = Seq(token1, token2.copy(_2 = 10)))
        val unsigned = createUnsignedTransaction(ctx, prover,
          IndexedSeq(input1, input2), IndexedSeq(output1),
          tokensToBurn = IndexedSeq(ergoToken1))
        val ex = intercept[TokenBalanceException] {
          prover.reduce(unsigned, 0)
        }
        ex.getMessage should include("Transaction tries to burn tokens, but not how it was requested")
        val token2_BurningWasNotRequested = ex.tokensDiff.exists(t => t == (token2._1, 10))
        val token1_WasRequestedButNotBurned = ex.tokensDiff.exists(t => t == (token1._1, -10))
        token2_BurningWasNotRequested shouldBe true
        token1_WasRequestedButNotBurned shouldBe true
      }

      // attempt to mint more than 1 token
      {
        val input1 = createBox(oneErg, tree1) // no tokens
        val output1 = createBox(oneErg * 2 + Parameters.MinFee, tree1, additionalTokens = Seq(token1, token2))
        val unsigned = createUnsignedTransaction(ctx, prover,
          IndexedSeq(input1), IndexedSeq(output1), tokensToBurn = IndexedSeq.empty)
        val ex = intercept[TokenBalanceException] {
          prover.reduce(unsigned, 0)
        }
        ex.getMessage should include("Only one token can be minted in a transaction")
        val token1_mint_attempted = ex.tokensDiff.exists(t => t == (token1._1, token1._2))
        val token2_mint_attempted = ex.tokensDiff.exists(t => t == (token2._1, token2._2))
        token1_mint_attempted shouldBe true
        token2_mint_attempted shouldBe true
        ex.tokensDiff.length shouldBe 2
      }

      // attempt to mint 1 token but with invalid id
      {
        val input1 = createBox(oneErg, tree1) // no tokens
        val output1 = createBox(oneErg * 2 + Parameters.MinFee, tree1, additionalTokens = Seq(token1))
        val unsigned = createUnsignedTransaction(ctx, prover,
          IndexedSeq(input1), IndexedSeq(output1), tokensToBurn = IndexedSeq.empty)
        val ex = intercept[TokenBalanceException] {
          prover.reduce(unsigned, 0)
        }
        ex.getMessage should include("Cannot mint a token with invalid id")
        val token1_mint_attempted = ex.tokensDiff.exists(t => t == (token1._1, token1._2))
        token1_mint_attempted shouldBe true
        ex.tokensDiff.length shouldBe 1
      }

    }
  }
}
