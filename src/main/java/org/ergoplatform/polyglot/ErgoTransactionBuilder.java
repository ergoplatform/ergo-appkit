package org.ergoplatform.polyglot;

import org.ergoplatform.*;
import scala.collection.JavaConverters;
import scala.collection.IndexedSeq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ErgoTransactionBuilder {

  ArrayList<UnsignedInput> _inputs = new ArrayList<>();
  ArrayList<DataInput> _dataInputs = new ArrayList<>();
  ArrayList<ErgoBoxCandidate> _outputCandidates = new ArrayList<>();

  public ErgoTransactionBuilder withInputs(String... inputBoxIds) {
     List<UnsignedInput> items = Arrays.asList(inputBoxIds).stream()
        .map(id -> JavaHelpers.createUnsignedInput(id))
        .collect(Collectors.toList());
     _inputs.addAll(items);
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
