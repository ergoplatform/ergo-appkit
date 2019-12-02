package org.ergoplatform.appkit.sandbox;

import scala.collection.Seq;
import scala.util.Try;

/**
 * BIP39 mnemonic sentence (see: https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki)
 */
public class Mnemonic {
//    public static int[] getAllowedStrengths() {
//        Seq<Object> strengthsSeq = org.ergoplatform.wallet.mnemonic.Mnemonic.AllowedStrengths();
//        strengthsSeq.toArray()
//        return strengthsSeq;
//    }

    /**
     * @param languageId - language identifier to be used in sentence
     * @param strength   - number of bits in the seed
     */
    public static String generate(String languageId, int strength, byte[] entropy) {
        org.ergoplatform.wallet.mnemonic.Mnemonic mnemonic = new org.ergoplatform.wallet.mnemonic.Mnemonic(languageId
                , strength);
        Try<String> resTry = mnemonic.toMnemonic(entropy);
        if (resTry.isFailure())
          throw new RuntimeException(
            String.format("Cannot create mnemonic for languageId: %s, strength: %d", languageId, strength));
        return resTry.get();
    }
}
