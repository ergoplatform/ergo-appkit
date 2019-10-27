package org.ergoplatform.polyglot;

import sigmastate.Values;

/**
 * Representation of ErgoScript contract using source code and named constants.
 * This information is enough to compile contract into {@link Values.ErgoTree}.
 * Once constructed the instances are immutable.
 * Methods which do transformations produce new instances.
 */
public class ErgoContract {
    final private Constants _constants;
    final private String _code;

    private ErgoContract(Constants constants, String code) {
        _constants = constants;
        _code = code;
    }

    /** Returns named constants used to compile this contract. */
    public Constants getConstants() {
        return _constants;
    }

    /**
     * Returns a source code of ErgoScript contract.
     */
    public String getErgoScript() {
        return _code;
    }

    /**
     * Creates a new contract with given parameters.
     */
    public static ErgoContract create(
            Constants constants, String code) {
        return new ErgoContract(constants, code);
    }

    /**
     * Creates a new contract by substituting the constant {@code name} with the new {@code value}.
     */
    ErgoContract substConstant(String name, Object value) {
        Constants cloned = (Constants)_constants.clone();
        cloned.replace(name, value);
        return create(cloned, _code);
    }
}
