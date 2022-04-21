package org.ergoplatform.appkit.impl;

import static org.ergoplatform.appkit.impl.Eip4TokenBuilder.getSerializedErgoValueForRegister;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.appkit.Eip29Attachment;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.Eip29GenericAttachment;
import org.ergoplatform.appkit.Eip29MultiAttachment;
import org.ergoplatform.appkit.Eip29PlainTextAttachment;
import org.ergoplatform.appkit.TransactionBox;
import org.ergoplatform.explorer.client.model.AdditionalRegisters;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Eip29AttachmentBuilder {
    /**
     * @return register number for attachment to use according EIP-29
     */
    public static int getAttachmentRegisterIndex() {
        return ErgoBox.maxRegisters() - 1;
    }

    /**
     * @return EIP-29 compliant attachment built from serialized ergo value
     */
    public static Eip29Attachment buildFromHexEncodedErgoValue(String hexEncodedErgoValue) {
        return Eip29GenericAttachment.createFromErgoValue(ErgoValue.fromHex(hexEncodedErgoValue));
    }

    /**
     * @return EIP-29 compliant attachment, if found. null otherwise
     */
    @Nullable
    public static Eip29Attachment buildFromAdditionalRegisters(@Nonnull AdditionalRegisters registers) {
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
    public static Eip29Attachment buildFromTransactionBox(@Nonnull TransactionBox transactionBox) {
        List<ErgoValue<?>> boxRegisters = transactionBox.getRegisters();
        try {
            int register9Index = getAttachmentRegisterIndex() - ErgoBox.startingNonMandatoryIndex();
            return Eip29GenericAttachment.createFromErgoValue(boxRegisters.get(register9Index));
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * @return PlainTextAttachment for given text
     */
    public static Eip29PlainTextAttachment createPlainTextAttachment(String text) {
        return Eip29PlainTextAttachment.buildForText(text);
    }

    /**
     * @return MultiAttachment for given attachment list
     */
    public static Eip29MultiAttachment createMultiAttachment(List<Eip29Attachment> attachments) {
        return Eip29MultiAttachment.buildForList(attachments);
    }
}
