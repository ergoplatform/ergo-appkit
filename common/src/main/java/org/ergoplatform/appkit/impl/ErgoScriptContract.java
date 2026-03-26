package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.*;
import sigma.ast.ErgoTree;


public class ErgoScriptContract implements ErgoContract {
    final private Constants _constants;
    final private String _code;
    private final NetworkType _networkType;
    private final byte _blockVersion;

    private ErgoScriptContract(Constants constants, String code, NetworkType networkType, byte blockVersion) {
        _constants = constants;
        _code = code;
        _networkType = networkType;
        _blockVersion = blockVersion;
    }

    @Override
    public Constants getConstants() {
        return _constants;
    }

    @Override
    public String getErgoScript() {
        return _code;
    }

    /**
     * Creates a new contract with given parameters (v5 default, no v6 activation).
     */
    public static ErgoScriptContract create(
            Constants constants, String code, NetworkType networkType) {
        return new ErgoScriptContract(constants, code, networkType, (byte) 0);
    }

    /**
     * Creates a new contract with given parameters and blockVersion for v6 activation.
     * When blockVersion >= 4, v6 opcodes (serialize, deserializeTo, getReg, checkPow,
     * bitwise ops, etc.) are enabled during compilation.
     */
    public static ErgoScriptContract create(
            Constants constants, String code, NetworkType networkType, byte blockVersion) {
        return new ErgoScriptContract(constants, code, networkType, blockVersion);
    }

    @Override
    public ErgoScriptContract substConstant(String name, Object value) {
        Constants cloned = (Constants)_constants.clone();
        cloned.replace(name, value);
        return create(cloned, _code, _networkType, _blockVersion);
    }

    @Override
    public ErgoTree getErgoTree() {
        if (_blockVersion >= 4) {
            // v6 activation: use version-aware compiler
            ErgoTree ergoTree = AppkitHelpers.compile(
                    _constants, _code, _networkType.networkPrefix, _blockVersion);
            return ergoTree;
        } else {
            // Legacy path: no version context needed
            ErgoTree ergoTree = AppkitHelpers.compile(
                    _constants, _code, _networkType.networkPrefix);
            return ergoTree;
        }
    }

    @Override
    public Address toAddress() {
        return Address.fromErgoTree(getErgoTree(), _networkType);
    }

}
