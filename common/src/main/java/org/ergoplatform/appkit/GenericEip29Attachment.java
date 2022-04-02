package org.ergoplatform.appkit;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scala.Byte;
import scala.Tuple2;
import special.collection.Coll;

/**
 * Represents an attachment according to EIP-29.
 * This is the superclass of all actual attachment types, as well as representing unknown types.
 */
public class GenericEip29Attachment implements Eip29Attachment {
    public static final byte[] MAGIC_BYTES = new byte[]{0x50, 0x52, 0x50};
    private final int attachmentType;
    private final byte[] attachmentContent;

    private GenericEip29Attachment(int attachmentType, byte[] attachmentContent) {
        this.attachmentType = attachmentType;
        this.attachmentContent = attachmentContent;
    }

    @Override
    public Type getType() {
        return GenericEip29Attachment.Type.fromTypeRawValue(attachmentType);
    }

    @Override
    public int getTypeRawValue() {
        return attachmentType;
    }

    @Override
    public ErgoValue<Tuple2<Coll<Byte>, Tuple2<Integer, Coll<Byte>>>> getErgoValue() {
        ErgoValue<Tuple2<Integer, Coll<scala.Byte>>> contentPair = ErgoValue.pairOf(ErgoValue.of(getTypeRawValue()), ErgoValue.of(attachmentContent));
        return ErgoValue.pairOf(ErgoValue.of(MAGIC_BYTES), contentPair);
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
    public static Eip29Attachment createFromErgoValue(ErgoValue<?> r9) {
        String illegalArgumentException = "R9 must be of pair (Coll[0x50, 0x52, 0x50], Tuple2(Int, Coll[Byte]), actual: ";

        if (!(r9.getValue() instanceof Tuple2)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.getValue().getClass().getSimpleName());
        }

        Tuple2<?, ?> attachmentWrapper = (Tuple2<?, ?>) r9.getValue();
        if (!(attachmentWrapper._1 instanceof Coll) || !(attachmentWrapper._2 instanceof Tuple2)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.toHex());
        }

        byte[] magicBytes = JavaHelpers$.MODULE$.collToByteArray((Coll<Object>) attachmentWrapper._1);
        if (!Arrays.equals(GenericEip29Attachment.MAGIC_BYTES, magicBytes)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.toHex());
        }

        Tuple2<?, ?> attachmentValue = (Tuple2<?, ?>) attachmentWrapper._2;
        if (!(attachmentValue._1 instanceof Integer) || !(attachmentValue._2 instanceof Coll)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.toHex());
        }

        return createFromAttachmentTuple((Tuple2<Integer, Coll<Byte>>) attachmentValue);
    }

    private static Eip29Attachment createFromAttachmentTuple(Tuple2<Integer, Coll<Byte>> attachmentTuple) {
        int typeConstant = attachmentTuple._1;
        GenericEip29Attachment.Type attachmentType = GenericEip29Attachment.Type.fromTypeRawValue(typeConstant);
        byte[] attachmentContent = ScalaHelpers.collByteToByteArray(attachmentTuple._2);

        switch (attachmentType) {
            case PLAIN_TEXT:
                return new PlainTextAttachment(attachmentContent);
            case MULTI_ATTACHMENT:
                return new MultiAttachment(attachmentContent);
            default:
                return new GenericEip29Attachment(typeConstant, attachmentContent);
        }
    }

    /**
     * Attachment containing a simple text
     */
    public static class PlainTextAttachment extends GenericEip29Attachment {
        private final String text;

        public PlainTextAttachment(byte[] attachmentContent) {
            super(Type.PLAIN_TEXT.toTypeRawValue(), attachmentContent);

            text = new String(attachmentContent, StandardCharsets.UTF_8);
        }

        /**
         * @return text, attachment content
         */
        public String getText() {
            return text;
        }

        public static GenericEip29Attachment.PlainTextAttachment buildForText(String text) {
            return new PlainTextAttachment(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Attachment containing list of attachments
     */
    public static class MultiAttachment extends GenericEip29Attachment {
        private final Tuple2<Integer, Coll<Byte>>[] attachmentList;

        private MultiAttachment(byte[] attachmentContent) {
            super(Type.MULTI_ATTACHMENT.toTypeRawValue(), attachmentContent);

            ErgoValue<?> attachmentTuples = ErgoValue.fromHex(new String(attachmentContent, StandardCharsets.UTF_8));
            if (!attachmentTuples.getType().equals(ErgoType.collType(ErgoType.pairType(ErgoType.integerType(), ErgoType.collType(ErgoType.byteType()))))) {
                throw new IllegalArgumentException("Multi attachment content needs to be Coll[(Int, Coll[Byte])]");
            }

            attachmentList = (Tuple2[]) ((Coll<?>) attachmentTuples.getValue()).toArray();
        }

        private MultiAttachment(byte[] attachmentContent, Tuple2<Integer, Coll<Byte>>[] attachmentList) {
            super(Type.MULTI_ATTACHMENT.toTypeRawValue(), attachmentContent);
            this.attachmentList = attachmentList;
        }

        /**
         * @return i-th attachment of this multi attachment
         */
        public Eip29Attachment getAttachment(int i) {
            return GenericEip29Attachment.createFromAttachmentTuple(attachmentList[i]);
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
        public static GenericEip29Attachment.MultiAttachment buildForList(List<Eip29Attachment> attachments) {
            List<Tuple2<Integer, Coll<Byte>>> attachmentTuples = new ArrayList<>(attachments.size());
            for (Eip29Attachment attachment : attachments) {
                attachmentTuples.add(attachment.getErgoValue().getValue()._2);
            }
            Tuple2<Integer, Coll<Byte>>[] tupleArray = attachmentTuples.toArray(new Tuple2[]{});
            ErgoValue<Coll<Tuple2<Integer, Coll<Byte>>>> ergoValue = ErgoValue.of(tupleArray, ErgoType.pairType(ErgoType.integerType(), ErgoType.collType(ErgoType.byteType())));

            return new MultiAttachment(ergoValue.toHex().getBytes(StandardCharsets.UTF_8), tupleArray);
        }
    }
}
