package org.ergoplatform.polyglot;

import java.util.Dictionary;
import java.util.Hashtable;

public class ConstantsBuilder {

   Dictionary<String, Object> _dictionary = new Hashtable<>();

  public ConstantsBuilder item(String name, Object value)  {
    _dictionary.put(name, value);
    return this;
  }

  public Dictionary<String, Object> build() {
    return _dictionary;
  }

  public static ConstantsBuilder create() { return new ConstantsBuilder(); }
}
