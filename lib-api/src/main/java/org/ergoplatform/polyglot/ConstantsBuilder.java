package org.ergoplatform.polyglot;

import java.util.Dictionary;
import java.util.Hashtable;

public class ConstantsBuilder {

   Constants _constants = new Constants();

  public ConstantsBuilder item(String name, Object value)  {
    _constants.put(name, value);
    return this;
  }

  public Constants build() {
    return _constants;
  }

  public static ConstantsBuilder create() { return new ConstantsBuilder(); }

  public static Constants empty() { return create().build(); }
}

