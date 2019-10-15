package org.ergoplatform.polyglot;

import com.google.common.primitives.Ints;
import org.ergoplatform.*;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.Map;
import scala.collection.immutable.IndexedSeq;
import sigmastate.SType;
import sigmastate.TrivialProp;
import sigmastate.Values;
import special.collection.Coll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;


public class ErgoBoxBuilder {

  long _value = 0;
  ArrayList<ErgoToken> _tokens = new ArrayList<>();

  public ErgoBoxBuilder withValue(long value) {
    _value = value;
    return this;
  }

  public ErgoBoxBuilder withTokens(ErgoToken... tokens) {
    Collections.addAll(_tokens, tokens);
    return this;
  }

  public ErgoBox build() {
    return JavaHelpers.createBox(_value, _tokens, 0);
  }
}
