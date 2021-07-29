package org.ergoplatform.appkit;

import java.util.List;

/**
 * Exceptions raised by {@link Mnemonic#checkEnglishMnemonic(List)}
 */
public class MnemonicValidationException extends Exception {
    public MnemonicValidationException() {
        super();
    }

    public MnemonicValidationException(String msg) {
        super(msg);
    }

    /**
     * Thrown when an argument to MnemonicCode is empty
     */
    public static class MnemonicEmptyException extends MnemonicValidationException {
        public MnemonicEmptyException() {
            super();
        }
    }

    /**
     * Thrown when an argument to MnemonicCode is of wrong list size
     */
    public static class MnemonicWrongListSizeException extends MnemonicValidationException {
        public MnemonicWrongListSizeException() {
            super();
        }
    }

    /**
     * Thrown when a list of MnemonicCode words fails the checksum check.
     */
    public static class MnemonicChecksumException extends MnemonicValidationException {
        public MnemonicChecksumException() {
            super();
        }
    }

    /**
     * Thrown when a word is encountered which is not in the MnemonicCode's word list.
     */
    public static class MnemonicWordException extends MnemonicValidationException {
        /**
         * Contains the word that was not found in the word list.
         */
        public final String badWord;

        public MnemonicWordException(String badWord) {
            super();
            this.badWord = badWord;
        }
    }
}
