package org.ergoplatform.appkit;

import java.util.Arrays;

import org.ergoplatform.sdk.JavaHelpers;
import scala.Tuple2;
import sigma.Coll;

/**
 * Represents an attachment according to EIP-29.
 * This is the superclass of all actual attachment types, as well as representing unknown types.
 */
public class BoxAttachmentGeneric implements BoxAttachment {
    private final int attachmentType;
    private final byte[] attachmentContent;

    BoxAttachmentGeneric(int attachmentType, byte[] attachmentContent) {
        this.attachmentType = attachmentType;
        this.attachmentContent = attachmentContent;
    }

    @Override
    public Type getType() {
        return BoxAttachmentGeneric.Type.fromTypeRawValue(attachmentType);
    }

    @Override
    public int getTypeRawValue() {
        return attachmentType;
    }

    @Override
    public ErgoValue<Tuple2<Coll<Byte>, Tuple2<Integer, Coll<Byte>>>> getErgoValue() {
        ErgoValue<Tuple2<Integer, Coll<Byte>>> contentPair = ErgoValue.pairOf(
            ErgoValue.of(getTypeRawValue()),
            ErgoValue.of(attachmentContent));
        return ErgoValue.pairOf(ErgoValue.of(BoxAttachment.MAGIC_BYTES), contentPair);
    }

    @Override
    public ErgoValue<?>[] getOutboxRegistersForAttachment() {
        ErgoValue<?>[] registers = new ErgoValue[6];

        for (int i = 0; i < registers.length - 1; i++) {
            registers[i] = ErgoValue.unit();
        }
        registers[5] = getErgoValue();
        return registers;
    }

    /**
     * @param r9 Ergo value to create the attachment object from, usually stored in register 9 of boxes
     * @return object representing the attachment
     */
    public static BoxAttachment createFromErgoValue(ErgoValue<?> r9) {
        String illegalArgumentException = "R9 must be of pair (Coll[0x50, 0x52, 0x50], Tuple2(Int, Coll[Byte]), actual: ";

        if (!(r9.getValue() instanceof Tuple2)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.getValue().getClass().getSimpleName());
        }

        Tuple2<?, ?> attachmentWrapper = (Tuple2<?, ?>) r9.getValue();
        if (!(attachmentWrapper._1 instanceof Coll) || !(attachmentWrapper._2 instanceof Tuple2)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.getType().toString());
        }

        byte[] magicBytes = collByteArrayChecked((Coll<?>) attachmentWrapper._1, illegalArgumentException);
        if (!Arrays.equals(BoxAttachment.MAGIC_BYTES, magicBytes)) {
            throw new IllegalArgumentException(illegalArgumentException + "Magic bytes not matched.");
        }

        Tuple2<?, ?> attachmentValue = (Tuple2<?, ?>) attachmentWrapper._2;
        if (!(attachmentValue._1 instanceof Integer) || !(attachmentValue._2 instanceof Coll)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.getType());
        }

        return createFromAttachmentTuple((Tuple2<Integer, Coll<Byte>>) attachmentValue);
    }

    /**
     * Converts a collection that is expected to hold bytes to a byte array. A collection
     * holding any other element type is rejected with an IllegalArgumentException instead
     * of failing inside the byte conversion with a ClassCastException. An empty collection
     * converts to an empty array regardless of its declared element type.
     */
    private static byte[] collByteArrayChecked(Coll<?> coll, String illegalArgumentMessage) {
        if (coll.length() > 0 && coll.tItem() != ErgoType.byteType().getRType()) {
            throw new IllegalArgumentException(illegalArgumentMessage + coll.tItem());
        }
        return JavaHelpers.collToByteArray((Coll<Object>) coll);
    }

    static BoxAttachment createFromAttachmentTuple(Tuple2<Integer, Coll<Byte>> attachmentTuple) {
        Integer typeConstant = attachmentTuple._1;
        BoxAttachmentGeneric.Type attachmentType = BoxAttachmentGeneric.Type.fromTypeRawValue(typeConstant);
        byte[] attachmentContent = collByteArrayChecked(attachmentTuple._2,
            "Attachment content must be of type Coll[Byte], actual element type: ");

        switch (attachmentType) {
            case PLAIN_TEXT:
                return new BoxAttachmentPlainText(attachmentContent);
            case MULTI_ATTACHMENT:
                return new BoxAttachmentMulti(attachmentContent);
            default:
                return new BoxAttachmentGeneric(typeConstant, attachmentContent);
        }
    }

}
