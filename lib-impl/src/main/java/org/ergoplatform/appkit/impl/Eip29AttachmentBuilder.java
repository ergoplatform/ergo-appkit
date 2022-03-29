package org.ergoplatform.appkit.impl;

import static org.ergoplatform.appkit.impl.Eip4TokenBuilder.getSerializedErgoValueForRegister;

import org.ergoplatform.appkit.Eip29Attachment;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.TransactionBox;
import org.ergoplatform.explorer.client.model.AdditionalRegisters;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Tuple2;

public class Eip29AttachmentBuilder {

    /**
     * @return EIP-29 compliant attachment built from serialized ergo value
     */
    public static Eip29Attachment buildFromHexEncodedErgoValue(String hexEncodedErgoValue) {
        return Eip29Attachment.createFromErgoValue(ErgoValue.fromHex(hexEncodedErgoValue));
    }

    /**
     * @return EIP-29 compliant attachment, if found. null otherwise
     */
    @Nullable
    public static Eip29Attachment buildFromAdditionalRegisters(@Nonnull AdditionalRegisters registers) {
        try {
            return buildFromHexEncodedErgoValue(getSerializedErgoValueForRegister(registers, "R9"));
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
            return Eip29Attachment.createFromErgoValue(boxRegisters.get(5));
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * @return PlainTextAttachment for given text
     */
    public static Eip29Attachment.PlainTextAttachment createPlainTextAttachment(String text) {
        return Eip29Attachment.PlainTextAttachment.buildForText(text);
    }

    /**
     * @return MultiAttachment for given attachment list
     */
    public static Eip29Attachment.MultiAttachment createMultiAttachment(List<Eip29Attachment> attachments) {
        return Eip29Attachment.MultiAttachment.buildForList(attachments);
    }
}
