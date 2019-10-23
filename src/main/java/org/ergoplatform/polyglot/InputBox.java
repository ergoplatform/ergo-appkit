package org.ergoplatform.polyglot;

public interface InputBox {
    ErgoId getId();
    Long getValue();
}

interface Proposition {

}

interface Prover {
    SignedTransaction sign(UnsignedTransaction tx);
}

interface ProverBuilder {
    Prover build();
}