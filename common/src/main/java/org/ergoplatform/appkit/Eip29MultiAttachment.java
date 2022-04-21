package org.ergoplatform.appkit;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import scala.Tuple2;
import special.collection.Coll;

/**
 * EIP-29 Attachment containing list of EIP-29 attachments
 */
public class Eip29MultiAttachment extends Eip29GenericAttachment {
    private final Tuple2<Integer, Coll<Byte>>[] attachmentList;

    Eip29MultiAttachment(byte[] attachmentContent) {
        super(Type.MULTI_ATTACHMENT.toTypeRawValue(), attachmentContent);

        ErgoValue<?> attachmentTuples = ErgoValue.fromHex(new String(attachmentContent, StandardCharsets.UTF_8));
        if (!attachmentTuples.getType().equals(ErgoType.collType(ErgoType.pairType(ErgoType.integerType(), ErgoType.collType(ErgoType.byteType()))))) {
            throw new IllegalArgumentException("Multi attachment content needs to be Coll[(Int, Coll[Byte])]");
        }

        attachmentList = (Tuple2[]) ((Coll<?>) attachmentTuples.getValue()).toArray();
    }

    private Eip29MultiAttachment(byte[] attachmentContent, Tuple2<Integer, Coll<Byte>>[] attachmentList) {
        super(Type.MULTI_ATTACHMENT.toTypeRawValue(), attachmentContent);
        this.attachmentList = attachmentList;
    }

    /**
     * @return i-th attachment of this multi attachment
     */
    public Eip29Attachment getAttachment(int i) {
        return Eip29GenericAttachment.createFromAttachmentTuple(attachmentList[i]);
    }

    /**
     * @return count of all attachments contained in this multi attachment
     */
    public int getAttachmentCount() {
        return attachmentList.length;
    }

    /**
     * @param attachments list of attachments to include in a multi attachment
     * @return object representing a multi attachment
     */
    public static Eip29MultiAttachment buildForList(List<Eip29Attachment> attachments) {
        List<Tuple2<Integer, Coll<Byte>>> attachmentTuples = new ArrayList<>(attachments.size());
        for (Eip29Attachment attachment : attachments) {
            attachmentTuples.add(attachment.getErgoValue().getValue()._2);
        }
        Tuple2<Integer, Coll<Byte>>[] tupleArray = attachmentTuples.toArray(new Tuple2[]{});
        ErgoValue<Coll<Tuple2<Integer, Coll<Byte>>>> ergoValue = ErgoValue.of(tupleArray, ErgoType.pairType(ErgoType.integerType(), ErgoType.collType(ErgoType.byteType())));

        return new Eip29MultiAttachment(ergoValue.toHex().getBytes(StandardCharsets.UTF_8), tupleArray);
    }
}
