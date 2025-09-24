package org.ergoplatform.appkit.impl;

import com.google.gson.Gson;
import org.ergoplatform.ErgoBox;
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.ErgoLikeTransactionSerializer;
import org.ergoplatform.Input;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.OutBox;
import org.ergoplatform.appkit.SignedInput;
import org.ergoplatform.appkit.SignedTransaction;
import org.ergoplatform.restapi.client.ErgoTransaction;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
import org.ergoplatform.restapi.client.JSON;
import org.ergoplatform.sdk.ErgoId;
import org.ergoplatform.sdk.SdkIsos;
import sigma.ast.ErgoTree;
import sigma.data.Iso;

import sigma.serialization.SigmaByteWriter;
import sigma.serialization.SigmaSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SignedTransactionImpl implements SignedTransaction {

    private final BlockchainContextBase _ctx;
    private final ErgoLikeTransaction _tx;
    private final int _txCost;

    public SignedTransactionImpl(BlockchainContextBase ctx, ErgoLikeTransaction tx, int txCost) {
        _ctx = ctx;
        _tx = tx;
        _txCost = txCost;
    }

    /**
     * Returns underlying {@link ErgoLikeTransaction}
     */
    public ErgoLikeTransaction getTx() {
        return _tx;
    }

    @Override
    public String toString() {
        return "Signed(" + _tx + ")";
    }

    @Override
    public String getId() {
        return _tx.id();
    }

    @Override
    public String toJson(boolean prettyPrint) {
        return toJson(prettyPrint, true);
    }

    @Override
    public String toJson(boolean prettyPrint, boolean formatJson) {
    	ErgoTransaction tx = ScalaBridge.isoErgoTransaction().from(_tx);
    	if (prettyPrint) {
            for (ErgoTransactionOutput o : tx.getOutputs()) {
                ErgoTree tree = ScalaBridge.isoStringToErgoTree().to(o.getErgoTree());
                o.ergoTree(tree.toString());
            }
    	}
    	Gson gson = (prettyPrint || formatJson) ? JSON.createGson().setPrettyPrinting().create() : JSON.createGson().create();
    	String json = gson.toJson(tx);
    	return json;
    }

    @Override
    public List<SignedInput> getSignedInputs() {
        List<Input> inputs = SdkIsos.JListToIndexedSeq(Iso.<Input>identityIso()).from(_tx.inputs());
        List<SignedInput> res = new ArrayList<>(inputs.size());
        for (Input input : inputs) {
            res.add(new SignedInputImpl(this, input));
        }
        return res;
    }

    @Override
    public List<InputBox> getOutputsToSpend() {
        List<ErgoBox> outputs = SdkIsos.JListToIndexedSeq(Iso.<ErgoBox>identityIso()).from(_tx.outputs());
        List<InputBox> res = new ArrayList<>(outputs.size());
        for (ErgoBox ergoBox : outputs) {
            res.add(new InputBoxImpl(ergoBox));
        }
        return res;
    }

    @Override
    public List<String> getInputBoxesIds() {
        List<Input> inputs = SdkIsos.JListToIndexedSeq(Iso.<Input>identityIso()).from(_tx.inputs());
        List<String> res = new ArrayList<>(inputs.size());
        for (Input input : inputs) {
            res.add(new ErgoId(input.boxId()).toString());
        }
        return res;
    }

    @Override
    public List<OutBox> getOutputs() {
        List<ErgoBox> outputs = SdkIsos.JListToIndexedSeq(Iso.<ErgoBox>identityIso()).from(_tx.outputs());
        List<OutBox> res = new ArrayList<>(outputs.size());
        for (ErgoBox ergoBox : outputs) {
            res.add(new OutBoxImpl(ergoBox));
        }
        return res;
    }

    @Override
    public int getCost() {
        return _txCost;
    }

    @Override
    public byte[] toBytes() {
        SigmaByteWriter w = SigmaSerializer.startWriter();
        ErgoLikeTransactionSerializer.serialize(_tx, w);
        w.putUInt(_txCost);
        return w.toBytes();
    }

    @Override
    public int hashCode() {
        return 31 * _tx.hashCode() + _txCost;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SignedTransactionImpl) {
            SignedTransactionImpl that = (SignedTransactionImpl)obj;
            return Objects.equals(that._tx, this._tx) && that._txCost == this._txCost;
        }
        return false;
    }
}
