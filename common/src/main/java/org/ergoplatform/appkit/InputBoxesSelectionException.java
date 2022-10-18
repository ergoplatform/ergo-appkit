package org.ergoplatform.appkit;

import java.util.List;
import java.util.Map;

public class InputBoxesSelectionException extends RuntimeException {

    public InputBoxesSelectionException(String message) {
        super(message);
    }

    /**
     * Thrown when a max amount of input boxes is set to be found, but it does not cover
     * the amount of ERG and/or tokens to send.
     */
    public static class InputBoxLimitExceededException extends InputBoxesSelectionException {

        public final long remainingAmount;
        public final List<ErgoToken> remainingTokens;
        public final int boxLimit;

        public InputBoxLimitExceededException(String message, long remainingAmount, List<ErgoToken> remainingTokens, int boxLimit) {
            super(message);
            this.remainingAmount = remainingAmount;
            this.remainingTokens = remainingTokens;
            this.boxLimit = boxLimit;
        }
    }

    /**
     * Thrown when a change box is needed, but ERG amount in all inboxes is not enough to create the
     * change box
     */
    public static class NotEnoughCoinsForChangeException extends InputBoxesSelectionException {

        public NotEnoughCoinsForChangeException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when the ERG amount needed was not found in all available boxes.
     */
    public static class NotEnoughErgsException extends InputBoxesSelectionException {
        public final long balanceFound;

        public NotEnoughErgsException(String message, long balanceFound) {
            super(message);
            this.balanceFound = balanceFound;
        }
    }

    /**
     * Thrown when a token amount needed was not found in all available boxes.
     */
    public static class NotEnoughTokensException extends InputBoxesSelectionException {
        public final Map<String, Long> tokenBalances;

        public NotEnoughTokensException(String message, Map<String, Long> tokenBalances) {
            super(message);
            this.tokenBalances = tokenBalances;
        }
    }
}
