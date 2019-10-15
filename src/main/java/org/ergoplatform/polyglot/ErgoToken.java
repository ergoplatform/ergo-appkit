package org.ergoplatform.polyglot;

public class ErgoToken {
  private final byte[] _id;
  private final long _value;

  public ErgoToken(byte[] id, long value) {
    _id = id;
    _value = value;
  }

  public byte[] getId() {
    return _id;
  }

  public long getValue() {
    return _value;
  }
}
