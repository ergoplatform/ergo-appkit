package org.ergoplatform.appkit;

import org.ergoplatform.P2PKAddress;
import org.ergoplatform.sdk.wallet.protocol.context.ErgoLikeParameters;
import special.sigma.BigInt;

import sigmastate.Values.SigmaBoolean;
import sigmastate.interpreter.HintsBag;

import java.util.List;

/**
 * Interface of the provers that can be used to sign {@link UnsignedTransaction}s.
 */
public interface ErgoProver {
    /**
     * Returns Pay-To-Public-Key {@link org.ergoplatform.ErgoAddress Ergo address} of this prover.
     */
    P2PKAddress getP2PKAddress();

    /**
     * Returns Pay-To-Public-Key address of this prover (represented as {@link Address}).
     * The returned address corresponds to the master secret derived from the mnemonic
     * phrase configured in the builder.
     */
    Address getAddress();

    /**
     * Returns a master secret key of this prover.
     *
     * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki">BIP-32</a>
     */
    BigInt getSecretKey();

    /**
     * Returns a master secret key of this prover.
     *
     * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki">BIP-32</a>
     */
    List<Address> getEip3Addresses();

    /**
     * Signs unsigned transaction by using configured secrets.
     * This method delegate to {@link ErgoProver#sign(UnsignedTransaction, int)} with
     * baseCost = 0
     *
     * @param tx transaction to be signed
     * @return new instance of {@link SignedTransaction} which contains necessary
     * proofs
     */
    SignedTransaction sign(UnsignedTransaction tx);

    /**
     * Signs unsigned transaction by using configured secrets.
     * The prover can attach signatures (aka `proofs of knowledge`) to the inputs
     * spent by the given {@link UnsignedTransaction transaction}.
     *
     * @param tx       transaction to be signed
     * @param baseCost computational cost before this transaction validation,
     *                 the validation starts with this value and shouldn't exceed the
     *                 total block limit known to the prover (see
     *                 {@link AppkitProvingInterpreter} and
     *                 {@link ErgoLikeParameters#maxBlockCost()})
     * @return new instance of {@link SignedTransaction} which contains necessary
     * proofs
     */
    SignedTransaction sign(UnsignedTransaction tx, int baseCost);

    /**
     * Signs an arbitrary message under a key representing a 
     * statement provable via a sigma-protocol.
     *
     * @param sigmaProp Sigma proposition to sign the message with
     * @param message message to sign
     * @param hintsBag additional hints for a signer (useful for distributed signing)
     * @return signed message
     * @see Signature#verifySignature(SigmaProp, byte[], byte[])
     */
    byte[] signMessage(SigmaProp sigmaProp, byte[] message, HintsBag hintsBag);

    ReducedTransaction reduce(UnsignedTransaction tx, int baseCost);

    SignedTransaction signReduced(ReducedTransaction tx, int baseCost);
}

