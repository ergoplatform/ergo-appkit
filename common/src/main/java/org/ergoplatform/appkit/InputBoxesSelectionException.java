package org.ergoplatform.appkit;

public class InputBoxesSelectionException extends RuntimeException {

    public InputBoxesSelectionException(String message) {
        super(message);
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
}
