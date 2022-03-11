package org.ergoplatform.appkit;

import org.ergoplatform.P2PKAddress;
import org.ergoplatform.ErgoLikeInterpreter;

import sigmastate.Values.SigmaBoolean;
import sigmastate.eval.CompiletimeIRContext;

public class Signature {
    private Signature() {
        // prevent instantiation
    }

    /**
     * Verifies a signature on given (arbitrary) message for a given public key.
     *
     * @param sigmaTree     public key (represented as a tree)
     * @param message       message to verify
     * @param signature signature for the message
     * @return whether signature is valid or not
     */
    public static boolean verifySignature(SigmaBoolean sigmaTree, byte[] message, byte[] signature) {
        return new ErgoLikeInterpreter((new CompiletimeIRContext())).verifySignature(sigmaTree, message, signature);
    }

    /**
     * Verifies a signature on given (arbitrary) message for a
     * using an address' public key.
     *
     * @param addr          address whose public key will be used to verify message
     * @param message       message to verify
     * @param signature signature for the message
     * @return whether signature is valid or not
     */
    public static boolean verifySignature(P2PKAddress addr, byte[] message, byte[] signature) {
        return new ErgoLikeInterpreter((new CompiletimeIRContext())).verifySignature(addr.pubkey(), message, signature);
    }
}
