package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoBoxCandidate;
import org.ergoplatform.UnsignedErgoLikeTransaction;

import java.util.Dictionary;

interface InputBox {
    ErgoId getId();
}

interface Proposition {

}

interface Prover {
    SignedTransaction sign(UnsignedTransaction tx);
}

interface ProverBuilder {
    Prover build();
}