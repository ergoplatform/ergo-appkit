package org.ergoplatform.appkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to keep track of amount of tokens to spend and amount of tokens already found
 */
public class SelectTokensHelper {
    private final HashMap<String, Long> tokensLeft;
    private boolean changeBoxNeeded;

    public SelectTokensHelper(Iterable<ErgoToken> tokensToSpent) {
        tokensLeft = new HashMap<>();

        for (ErgoToken ergoToken : tokensToSpent) {
            tokensLeft.put(ergoToken.getId().toString(), ergoToken.getValue());
        }

        changeBoxNeeded = false;
    }

    /**
     * Checks if the given tokens are needed to fulfill the tokens to
     * spend
     *
     * @return if the found tokens were needed to fill the tokens left
     */
    public boolean areTokensNeeded(Iterable<ErgoToken> foundTokens) {
        boolean tokensNeeded = false;
        for (ErgoToken foundToken : foundTokens) {
            String tokenId = foundToken.getId().toString();
            if (tokensLeft.containsKey(tokenId)) {
                Long currentValue = tokensLeft.get(tokenId);
                if (currentValue > 0) {
                    tokensNeeded = true;
                    break;
                }
            }
        }
        return tokensNeeded;
    }

    /**
     * Marks the given tokens as needed, subtracting the amount from the remaining amount of
     * tokens to find.
     * Also updates if a change box is needed
     */
    public void useTokens(Iterable<ErgoToken> foundTokens) {
        for (ErgoToken foundToken : foundTokens) {
            String tokenId = foundToken.getId().toString();
            if (tokensLeft.containsKey(tokenId)) {
                Long currentValue = tokensLeft.get(tokenId);
                long newValue = currentValue - foundToken.getValue();
                tokensLeft.put(tokenId, newValue);
                if (newValue < 0) {
                    changeBoxNeeded = true;
                }
            } else {
                changeBoxNeeded = true;
            }
        }
    }

    public boolean areTokensCovered() {
        boolean success = true;
        for (Long value : tokensLeft.values()) {
            if (value > 0) {
                success = false;
                break;
            }
        }
        return success;
    }

    public List<ErgoToken> getRemainingTokenList() {
        List<ErgoToken> result = new ArrayList<>();
        for (Map.Entry<String, Long> tokenAmount : tokensLeft.entrySet()) {
            long amountLeft = tokenAmount.getValue();
            if (amountLeft > 0) {
                result.add(new ErgoToken(tokenAmount.getKey(), amountLeft));
            }
        }
        return result;
    }

    public boolean isChangeBoxNeeded() {
        return changeBoxNeeded;
    }
}
