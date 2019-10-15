package org.ergoplatform.polyglot;

import org.ergoplatform.*;
import scala.collection.JavaConverters;
import scala.collection.IndexedSeq;

import java.util.ArrayList;
import java.util.Collections;

public class ErgoTransactionBuilder {

  ArrayList<UnsignedInput> _inputs = new ArrayList<>();
  ArrayList<DataInput> _dataInputs = new ArrayList<>();
  ArrayList<ErgoBoxCandidate> _outputCandidates = new ArrayList<>();

  public ErgoTransactionBuilder withInputs(UnsignedInput... inputs) {
    Collections.addAll(_inputs, inputs);
    return this;
  }

  public ErgoTransactionBuilder withDataInputs(DataInput... dataInputs) {
    Collections.addAll(_dataInputs, dataInputs);
    return this;
  }

  public ErgoTransactionBuilder withCandidates(ErgoBoxCandidate... candidates) {
    _outputCandidates = new ArrayList<>();
    Collections.addAll(_outputCandidates, candidates);
    return this;
  }

  public UnsignedErgoLikeTransaction build() {
    IndexedSeq<UnsignedInput> inputs = JavaHelpers.toIndexedSeq(_inputs);
    IndexedSeq<DataInput> dataInputs = JavaHelpers.toIndexedSeq(_dataInputs);
    IndexedSeq<ErgoBoxCandidate> outputCandidates = JavaHelpers.toIndexedSeq(_outputCandidates);
    return new UnsignedErgoLikeTransaction(inputs, dataInputs, outputCandidates);
  }
}
