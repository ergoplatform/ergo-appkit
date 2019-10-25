package org.ergoplatform.polyglot.ni;

import scalan.RType;
import special.collection.CollOverArray;

public class TestColl {
    public static void main(String[] args) {
      CollOverArray coll = new CollOverArray(new Object[1], RType.IntType());
      System.out.println(coll.toString());
    }
}
