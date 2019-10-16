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

  private final byte _networkPrefix;
  private long _value = 0;
  private Dictionary<String, Object> _constants;
  private String _contractText = "";
  private ArrayList<ErgoToken> _tokens = new ArrayList<>();

  public ErgoBoxBuilder(byte networkPrefix) {
    _networkPrefix = networkPrefix;
  }

  public ErgoBoxBuilder value(long value) {
    _value = value;
    return this;
  }

  public ErgoBoxBuilder contract(Dictionary<String, Object> constants, String contractText) {
    _constants = constants;
    _contractText = contractText;
    return this;
  }

  public ErgoBoxBuilder tokens(ErgoToken... tokens) {
    Collections.addAll(_tokens, tokens);
    return this;
  }

  public ErgoBox build() {
    Values.ErgoTree tree = JavaHelpers.compile(_constants, _contractText, _networkPrefix);
    return JavaHelpers.createBox(_value, tree, _tokens, 0);
  }
}
