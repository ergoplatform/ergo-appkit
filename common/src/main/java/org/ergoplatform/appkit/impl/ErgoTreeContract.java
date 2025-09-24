package org.ergoplatform.appkit.impl;

import org.ergoplatform.ErgoAddress;
import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.Constants;
import org.ergoplatform.appkit.ErgoContract;
import org.ergoplatform.appkit.NetworkType;
import sigma.ast.ErgoTree;

public class ErgoTreeContract implements ErgoContract {
    private final ErgoTree _ergoTree;
    private final NetworkType _networkType;
    public ErgoTreeContract(ErgoTree ergoTree, NetworkType networkType) {
        _ergoTree = ergoTree;
        _networkType = networkType;
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
    public ErgoTree getErgoTree() {
        return _ergoTree;
    }

    @Override
    public Address toAddress() { return Address.fromErgoTree(_ergoTree, _networkType); }

}
