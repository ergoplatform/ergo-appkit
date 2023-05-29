package org.ergoplatform.appkit;

import org.ergoplatform.sdk.ErgoId;
import org.ergoplatform.sdk.ErgoToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a collection of boxes covering the given amount of NanoErgs to spend.
 * If the amount is not covered in full, then isCovered() returns false.
 * Thus, this class allows to represent partial coverage, which is useful to collect boxes
 * in many steps.
 */
public class CoveringBoxes {
    private final long _amountToSpend;
    private final List<InputBox> _boxes;
    private final List<ErgoToken> tokensToSpend;
    private final boolean changeBoxNeeded;

    public CoveringBoxes(long amountToSpend, List<InputBox> boxes,
                         List<ErgoToken> tokensToSpend, boolean changeBoxNeeded) {
        _amountToSpend = amountToSpend;
        _boxes = boxes;
        this.tokensToSpend = tokensToSpend;
        this.changeBoxNeeded = changeBoxNeeded;
    }

    /** The amount covered by the boxes in this set. */
    public long getCoveredAmount() {
        long value = 0;
        for (InputBox box : _boxes) {
            value = value + box.getValue();
        }
        return value;
    }

    /**
     * @return list of tokens covered by boxes
     */
    public List<ErgoToken> getCoveredTokens() {
        HashMap<ErgoId, ErgoToken> coveredTokens = new HashMap<>();
        for (InputBox box : _boxes) {
            for (ErgoToken token : box.getTokens()) {
                ErgoId tokenId = token.getId();
                if (!coveredTokens.containsKey(tokenId)) {
                    coveredTokens.put(tokenId, token);
                } else {
                    ErgoToken tokenInMap = coveredTokens.get(tokenId);
                    coveredTokens.put(tokenId, new ErgoToken(tokenId, token.getValue() + tokenInMap.getValue()));
                }
            }

        }
        return new ArrayList<>(coveredTokens.values());
    }

    /**
     * @return true if the amount and the tokens are covered by the boxes in this set, false otherwise.
     */
    public boolean isCovered() {
        return getCoveredAmount() >= _amountToSpend &&
            new SelectTokensHelper(tokensToSpend).useTokens(getCoveredTokens()).areTokensCovered();
    }

    /** Returns a list of boxes stored in this set. */
    public List<InputBox> getBoxes() {
        return _boxes;
    }

    /**
     * @return true if a change box is needed to spend the selected boxes
     */
    public boolean isChangeBoxNeeded() {
        return changeBoxNeeded;
    }
}
