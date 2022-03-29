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

    public static Eip29Attachment buildFromHexEncodedErgoValue(String hexEncodedErgoValue) {
        return Eip29Attachment.createFromErgoValue(ErgoValue.fromHex(hexEncodedErgoValue));
    }

    @Nullable
    public static Eip29Attachment buildFromAdditionalRegisters(@Nonnull AdditionalRegisters registers) {
        String serializedErgoValue = getSerializedErgoValueForRegister(registers, "R9");
        if (serializedErgoValue != null)
            return buildFromHexEncodedErgoValue(serializedErgoValue);
        else
            return null;
    }

    @Nullable
    public static Eip29Attachment buildFromTransactionBox(@Nonnull TransactionBox transactionBox) {
        List<ErgoValue<?>> boxRegisters = transactionBox.getRegisters();

        if (boxRegisters.size() > 5)
            return Eip29Attachment.createFromErgoValue(boxRegisters.get(5));
        else
            return null;
    }

    public static Eip29Attachment.PlainTextAttachment createPlainTextAttachment(String text) {
        return Eip29Attachment.PlainTextAttachment.buildForText(text);
    }

    public static Eip29Attachment.MultiAttachment createMultiAttachment(List<Eip29Attachment> attachments) {
        return Eip29Attachment.MultiAttachment.buildForList(attachments);
    }
}
