package org.ergoplatform.appkit.impl;
import org.ergoplatform.appkit.PreHeader;

public class PreHeaderImpl implements PreHeader {
    final special.sigma.PreHeader _ph;

    public PreHeaderImpl(special.sigma.PreHeader ph) {
        _ph = ph;
    }
}
