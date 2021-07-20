package org.ergoplatform.appkit;

import java.util.List;

public class CoveringBoxes {
    private final long _amountToSpend;
    private final List<InputBox> _boxes;

    public CoveringBoxes(long amountToSpend, List<InputBox> boxes) {
        _amountToSpend = amountToSpend;
        _boxes = boxes;
    }

    public long getCoveredAmount() {
        return _boxes.stream()
            .map(InputBox::getValue)
            .reduce(0L, Long::sum);
    }

    public boolean isCovered() { return getCoveredAmount() >= _amountToSpend; }

    public List<InputBox> getBoxes() {
        return _boxes;
    }
}
