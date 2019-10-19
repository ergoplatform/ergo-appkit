package org.ergoplatform.polyglot;

import org.ergoplatform.*;
import sigmastate.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;

public class OutBoxBuilderImpl implements OutBoxBuilder {

  private final byte _networkPrefix;
  private long _value = 0;
  private Dictionary<String, Object> _constants;
  private String _contractText = "";
  private ArrayList<ErgoToken> _tokens = new ArrayList<>();

  public OutBoxBuilderImpl(byte networkPrefix) {
    _networkPrefix = networkPrefix;
  }

  public OutBoxBuilderImpl value(long value) {
    _value = value;
    return this;
  }

  @Override
  public OutBoxBuilderImpl proposition(Proposition proposition) {
    return null;
  }

  @Override
  public OutBoxBuilderImpl contract(Dictionary<String, Object> constants, String contractText) {
    _constants = constants;
    _contractText = contractText;
    return this;
  }

  public OutBoxBuilderImpl tokens(ErgoToken... tokens) {
    Collections.addAll(_tokens, tokens);
    return this;
  }

  @Override
  public OutBoxBuilderImpl registers(Dictionary<Byte, Object> regs) {
    return null;
  }

  public OutBox build() {
    Values.ErgoTree tree = JavaHelpers.compile(_constants, _contractText, _networkPrefix);
    return new OutBoxImpl(JavaHelpers.createBox(_value, tree, _tokens, 0));
  }
}
