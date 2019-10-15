package org.ergoplatform.polyglot;

import org.ergoplatform.DataInput;
import org.ergoplatform.ErgoBoxCandidate;
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.Input;
import scala.collection.JavaConverters;
import scala.collection.immutable.IndexedSeq;

import java.util.ArrayList;
import java.util.Collections;

public class ErgoTransactionBuilder {

  ArrayList<ErgoBoxCandidate> _outputCandidates = null;

  public ErgoTransactionBuilder withCandidates(ErgoBoxCandidate... candidates) {
    _outputCandidates = new ArrayList<>();
    Collections.addAll(_outputCandidates, candidates);
    return this;
  }

  public ErgoLikeTransaction build() {
    IndexedSeq<Input> inputs = null;
    IndexedSeq<DataInput> dataInputs = null;
    IndexedSeq<ErgoBoxCandidate> outputCandidates = JavaConverters.asScalaIterator(_outputCandidates.iterator()).toIndexedSeq();
    return new ErgoLikeTransaction(inputs, dataInputs, outputCandidates);
  }
}
