package org.ergoplatform.appkit;

import org.ergoplatform.sdk.ErgoToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to keep track of amount of tokens to spend and tokens already covered by boxes.
 * Used to determine if more and which boxes need to be selected, and if a change box is needed.
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
     * Marks the given tokens as selected, subtracting the amount values from the remaining amount
     * of tokens needed to fulfill the initial tokens to spend.
     * Also keeps track if a change box is needed in case we selected to many tokens, see
     * {@link #isChangeBoxNeeded()}
     */
    public SelectTokensHelper useTokens(Iterable<ErgoToken> selectedTokens) {
        for (ErgoToken selectedToken : selectedTokens) {
            String tokenId = selectedToken.getId().toString();
            if (tokensLeft.containsKey(tokenId)) {
                Long currentValue = tokensLeft.get(tokenId);
                long newValue = currentValue - selectedToken.getValue();
                tokensLeft.put(tokenId, newValue);
                if (newValue < 0) {
                    changeBoxNeeded = true;
                }
            } else {
                changeBoxNeeded = true;
            }
        }
        return this;
    }

    /**
     * @return true if currently selected tokens can fulfill the initial tokens to spend
     */
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

    /**
     * @return true if a change box is needed. This is the case if more tokens were selected
     * than needed to spend.
     */
    public boolean isChangeBoxNeeded() {
        return changeBoxNeeded;
    }
}
