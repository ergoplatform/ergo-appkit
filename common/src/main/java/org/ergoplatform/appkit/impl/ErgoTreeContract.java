package org.ergoplatform.appkit.impl;

import org.ergoplatform.ErgoAddress;
import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.Constants;
import org.ergoplatform.appkit.ErgoContract;
import org.ergoplatform.appkit.NetworkType;
import sigmastate.Values;

public class ErgoTreeContract implements ErgoContract {
    private final Values.ErgoTree _ergoTree;

    public ErgoTreeContract(Values.ErgoTree ergoTree) {
        _ergoTree = ergoTree;
    }

    @Override
    public Constants getConstants() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getErgoScript() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ErgoTreeContract substConstant(String name, Object value) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Values.ErgoTree getErgoTree() {
        return _ergoTree;
    }

    @Override
    public Address getAddress(NetworkType networkType) { return Address.fromErgoTree(_ergoTree, networkType); }

}
