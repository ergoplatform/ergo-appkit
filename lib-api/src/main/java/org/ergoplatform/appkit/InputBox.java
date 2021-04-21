package org.ergoplatform.appkit;

import org.ergoplatform.ErgoBox;
import scala.collection.immutable.Map;
import sigmastate.SType;
import sigmastate.Values;

import java.util.List;

/**
 * Interface of UTXO boxes which can be accessed in the blockchain node.
 * Instances of this interface can be {@link BlockchainContext#getBoxesById(String...) obtained}
 * from {@link BlockchainContext} and {@link UnsignedTransactionBuilder#boxesToSpend(java.util.List) spent}
 * as part of a new transaction.
 */
public interface InputBox {
    /**
     * Returns the id of this box.
     */
    ErgoId getId();

    /**
     * Returns the ERG value stored in this box, i.e. unspent value in UTXO.
     */
    Long getValue();

    /**
     * Returns the tokens stored in this box.
     */
    List<ErgoToken> getTokens();

    /**
     * Returns values of the non-mandatory registers which are stored in the box (R4, R5, R6, R7, R8, R9)
     * Index 0 corresponds to R4, 1 -> R5, etc.
     */
    List<ErgoValue<?>> getRegisters();

    /**
     * Returns the ErgoTree of the script guarding the box
     */
    Values.ErgoTree getErgoTree();

    /**
     * Extends the given input with context variables (aka {@link
     * sigmastate.interpreter.ContextExtension}.
     * Note, this method don't change `this` instance.
     *
     * @param variables a list of variables
     * @return a new instance of InputBox with the given variables attached.
     */
    InputBox withContextVars(ContextVar... variables);

    /**
     * Json representation of this transaction.
     *
     * @param prettyPrint if `true` then ergoTree is pretty-printed, otherwise encoded bytes
     *                    are used.
     * @return string with json text
     */
    String toJson(boolean prettyPrint);

    /**
     * Json representation of this transaction.
     * Note, Json formatting is pretty-printed if either prettyPrint or formatJson is set
     * to true.
     *
     * @param prettyPrint if `true` then ergoTree is pretty-printed, otherwise
     *                    encoded bytes are used.
     * @param formatJson  if `true` then json pretty printer is used to format json output.
     *
     * @return string with json text
     */
    String toJson(boolean prettyPrint, boolean formatJson);

}

