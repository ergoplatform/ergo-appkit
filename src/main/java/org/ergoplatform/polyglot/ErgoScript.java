package org.ergoplatform.polyglot;

import java.util.Dictionary;

public class ErgoScript {
    private Dictionary<String, Object> _constants;
    private String _code;

    public ErgoScript(Dictionary<String, Object> constants, String code) {
        _constants = constants;
        _code = code;
    }

    public Dictionary<String, Object> getConstants() {
        return _constants;
    }

    public String getCode() {
        return _code;
    }

    public static ErgoScript create(
            Dictionary<String, Object> constants, String code) {
        return new ErgoScript(constants, code);
    }
}
