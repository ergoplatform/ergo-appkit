package org.ergoplatform.appkit.impl;

import static org.ergoplatform.appkit.impl.Eip4TokenBuilder.getSerializedErgoValueForRegister;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.appkit.BoxAttachment;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.BoxAttachmentGeneric;
import org.ergoplatform.appkit.BoxAttachmentMulti;
import org.ergoplatform.appkit.BoxAttachmentPlainText;
import org.ergoplatform.appkit.TransactionBox;
import org.ergoplatform.explorer.client.model.AdditionalRegisters;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BoxAttachmentBuilder {
    /**
     * @return register number for attachment to use according EIP-29
     */
    public static int getAttachmentRegisterIndex() {
        return ErgoBox.maxRegisters() - 1;
    }

    /**
     * @return EIP-29 compliant attachment built from serialized ergo value
     */
    public static BoxAttachment buildFromHexEncodedErgoValue(String hexEncodedErgoValue) {
        return BoxAttachmentGeneric.createFromErgoValue(ErgoValue.fromHex(hexEncodedErgoValue));
    }

    /**
     * @return EIP-29 compliant attachment, if found. null otherwise
     */
    @Nullable
    public static BoxAttachment buildFromAdditionalRegisters(@Nonnull AdditionalRegisters registers) {
        try {
            return buildFromHexEncodedErgoValue(getSerializedErgoValueForRegister(registers, "R" + getAttachmentRegisterIndex()));
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * @return EIP-29 compliant attachment, if found. null otherwise
     */
    @Nullable
    public static BoxAttachment buildFromTransactionBox(@Nonnull TransactionBox transactionBox) {
        List<ErgoValue<?>> boxRegisters = transactionBox.getRegisters();
        try {
            int register9Index = getAttachmentRegisterIndex() - ErgoBox.startingNonMandatoryIndex();
            return BoxAttachmentGeneric.createFromErgoValue(boxRegisters.get(register9Index));
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * @return PlainTextAttachment for given text
     */
    public static BoxAttachmentPlainText createPlainTextAttachment(String text) {
        return BoxAttachmentPlainText.buildForText(text);
    }

    /**
     * @return MultiAttachment for given attachment list
     */
    public static BoxAttachmentMulti createMultiAttachment(List<BoxAttachment> attachments) {
        return BoxAttachmentMulti.buildForList(attachments);
    }
}
