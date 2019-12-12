package org.ergoplatform.appkit;

import scala.util.Try;

/**
 * BIP39 mnemonic sentence (see: https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki)
 */
public class Mnemonic {
    private final String _phrase;
    private final String _password;

    Mnemonic(String phrase, String password) {
        _phrase = phrase;
        _password = password;
    }

    /**
     * Default strength of mnemonic security (number of bits)
     */
    public static int DEFAULT_STRENGTH = 160;

    /**
     * Generate random bytes with the given security strength (number of bits)
     */
    public static byte[] getEntropy(int strength) { return scorex.utils.Random.randomBytes(strength / 8); }

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

    /**
     * Generates a new mnemonic using english words and default strength parameters.
     */
    public static String generateEnglishMnemonic() {
        byte[] entropy = getEntropy(DEFAULT_STRENGTH);
        return Mnemonic.generate("english", DEFAULT_STRENGTH, entropy);
    }

    public static Mnemonic create(String phrase, String password) {
      return new Mnemonic(phrase, password);
    }

    public String getPhrase() {
        return _phrase;
    }

    public String getPassword() {
        return _password;
    }
}
