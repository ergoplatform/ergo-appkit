package org.ergoplatform.appkit;

import org.ergoplatform.wallet.mnemonic.WordList;

import java.util.Collections;
import java.util.List;

import scala.Option;
import scala.util.Try;

/**
 * BIP39 mnemonic sentence (see: https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki)
 */
public class Mnemonic {
    public static final String LANGUAGE_ID_ENGLISH = "english";
    private final char[] _phrase;
    private final char[] _password;

    Mnemonic(char[] phrase, char[] password) {
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
        org.ergoplatform.wallet.mnemonic.Mnemonic mnemonic =
                new org.ergoplatform.wallet.mnemonic.Mnemonic(languageId
                        , strength);
        Try<org.ergoplatform.wallet.interface4j.SecretString> resTry = mnemonic.toMnemonic(entropy);
        if (resTry.isFailure())
            throw new RuntimeException(
                    String.format("Cannot create mnemonic for languageId: %s, strength: %d", languageId,
                            strength));
        return resTry.get().toStringUnsecure();
    }

    /**
     * Generates a new mnemonic using english words and default strength parameters.
     */
    public static String generateEnglishMnemonic() {
        byte[] entropy = getEntropy(DEFAULT_STRENGTH);
        return Mnemonic.generate(LANGUAGE_ID_ENGLISH, DEFAULT_STRENGTH, entropy);
    }

    /**
     * Creates {@link Mnemonic} instance with the given phrase and password.
     * Both phrase and password is passed by reference. This security sensitive data is not copied.
     */
    public static Mnemonic create(char[] phrase, char[] password) {
        return new Mnemonic(phrase, password);
    }

    /**
     * Creates {@link Mnemonic} instance with the given phrase and password.
     */
    public static Mnemonic create(SecretString phrase, SecretString password) {
        return new Mnemonic(phrase.getData(), password.getData());
    }

    /**
     * Returns secret mnemonic phrase stored in this {@link Mnemonic} instance.
     */
    public SecretString getPhrase() {
        return SecretString.create(_phrase);
    }

    /**
     * Returns secret mnemonic password stored in this {@link Mnemonic} instance.
     */
    public SecretString getPassword() {
        return SecretString.create(_password);
    }

    public byte[] toSeed() {
        Option<String> passOpt = Iso.arrayCharToOptionString().to(getPassword());
        return JavaHelpers.mnemonicToSeed(String.valueOf(_phrase), passOpt);
    }

    /**
     * Check to see if a mnemonic word list is valid, convenience method to call {@link #toEntropy(String, List)}
     */
    public static void checkEnglishMnemonic(List<String> words) throws MnemonicValidationException {
        toEntropy(LANGUAGE_ID_ENGLISH, words);
    }

    /**
     * Convert mnemonic word list to original entropy value. Can be used to validate a given
     * mnemonic sentence.
     * Kindly borrowed from bitcoinj
     */
    public static byte[] toEntropy(String languageId, List<String> words) throws MnemonicValidationException {
        if (words.size() % 3 > 0)
            throw new MnemonicValidationException.MnemonicWrongListSizeException();

        if (words.size() == 0)
            throw new MnemonicValidationException.MnemonicEmptyException();

        // Look up all the words in the list and construct the
        // concatenation of the original entropy and the checksum.
        //
        int concatLenBits = words.size() * 11;
        boolean[] concatBits = new boolean[concatLenBits];
        int wordindex = 0;
        List<String> wordList = JavaHelpers$.MODULE$.toJavaList(WordList.load(languageId).get().words().toList());
        for (String word : words) {
            // Find the words index in the wordlist.
            int ndx = Collections.binarySearch(wordList, word);
            if (ndx < 0)
                throw new MnemonicValidationException.MnemonicWordException(word);

            // Set the next 11 bits to the value of the index.
            for (int ii = 0; ii < 11; ++ii)
                concatBits[(wordindex * 11) + ii] = (ndx & (1 << (10 - ii))) != 0;
            ++wordindex;
        }

        int checksumLengthBits = concatLenBits / 33;
        int entropyLengthBits = concatLenBits - checksumLengthBits;

        // Extract original entropy as bytes.
        byte[] entropy = new byte[entropyLengthBits / 8];
        for (int ii = 0; ii < entropy.length; ++ii)
            for (int jj = 0; jj < 8; ++jj)
                if (concatBits[(ii * 8) + jj])
                    entropy[ii] |= 1 << (7 - jj);

        // Take the digest of the entropy.
        byte[] hash = scorex.crypto.hash.Sha256.hash(entropy);
        boolean[] hashBits = bytesToBits(hash);

        // Check all the checksum bits.
        for (int i = 0; i < checksumLengthBits; ++i)
            if (concatBits[entropyLengthBits + i] != hashBits[i])
                throw new MnemonicValidationException.MnemonicChecksumException();

        return entropy;
    }

    private static boolean[] bytesToBits(byte[] data) {
        boolean[] bits = new boolean[data.length * 8];
        for (int i = 0; i < data.length; ++i)
            for (int j = 0; j < 8; ++j)
                bits[(i * 8) + j] = (data[i] & (1 << (7 - j))) != 0;
        return bits;
    }
}
