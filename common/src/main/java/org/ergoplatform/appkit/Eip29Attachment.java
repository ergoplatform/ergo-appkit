package org.ergoplatform.appkit;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scala.Tuple2;
import special.collection.Coll;

/**
 * Represents an attachment according to EIP-29
 */
public class Eip29Attachment {
    public static final byte[] MAGIC_BYTES = new byte[]{0x50, 0x52, 0x50};
    private final int attachmentType;
    private final byte[] attachmentContent;

    private Eip29Attachment(int attachmentType, byte[] attachmentContent) {
        this.attachmentType = attachmentType;
        this.attachmentContent = attachmentContent;
    }

    /**
     * @return type of this attachment, if known
     */
    public Type getType() {
        return Eip29Attachment.Type.fromTypeRawValue(attachmentType);
    }

    /**
     * @return raw value type of this attachment. Can return types unknown to this implementation.
     */
    public int getTypeRawValue() {
        return attachmentType;
    }

    /**
     * @return returns full ErgoValue for this attachment, to use for register 9 of out boxes
     */
    public ErgoValue<Tuple2<byte[], Tuple2<Integer, byte[]>>> getErgoValue() {
        ErgoValue<Tuple2> contentPair = ErgoValue.pairOf(ErgoValue.of(getTypeRawValue()), ErgoValue.of(attachmentContent));
        return (ErgoValue<Tuple2<byte[], Tuple2<Integer, byte[]>>>) (ErgoValue) ErgoValue.pairOf(ErgoValue.of(MAGIC_BYTES), contentPair);
    }

    /**
     * @return array R4-R9 to use with OutboxBuilder#registers
     */
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

        Tuple2 attachmentWrapper = (Tuple2) r9.getValue();
        if (!(attachmentWrapper._1 instanceof Coll) || !(attachmentWrapper._2 instanceof Tuple2)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.toHex());
        }

        byte[] magicBytes = JavaHelpers$.MODULE$.collToByteArray((Coll<Object>) attachmentWrapper._1);
        if (!Arrays.equals(Eip29Attachment.MAGIC_BYTES, magicBytes)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.toHex());
        }

        Tuple2 attachmentValue = (Tuple2) attachmentWrapper._2;
        if (!(attachmentValue._1 instanceof Integer) || !(attachmentValue._2 instanceof Coll)) {
            throw new IllegalArgumentException(illegalArgumentException + r9.toHex());
        }

        return createFromAttachmentTuple(attachmentValue);
    }

    private static Eip29Attachment createFromAttachmentTuple(Tuple2 attachmentTuple) {
        int typeConstant = (int) attachmentTuple._1;
        Eip29Attachment.Type attachmentType = Eip29Attachment.Type.fromTypeRawValue(typeConstant);
        byte[] attachmentContent = JavaHelpers$.MODULE$.collToByteArray((Coll<Object>) attachmentTuple._2);

        switch (attachmentType) {
            case PLAIN_TEXT:
                return new PlainTextAttachment(attachmentContent);
            case MULTI_ATTACHMENT:
                return new MultiAttachment(attachmentContent);
            default:
                return new Eip29Attachment(typeConstant, attachmentContent);
        }
    }

    /**
     * Type of attachment
     */
    public enum Type {
        /**
         * This attachment is a list of attachments
         */
        MULTI_ATTACHMENT,
        /**
         * This attachment is a plain text, see {@link PlainTextAttachment#getText()}
         */
        PLAIN_TEXT,
        /**
         * Attachment type is undefined or unknown
         */
        UNDEFINED;

        /**
         * @return raw int constant for attachment type according to EIP-29
         */
        public int toTypeRawValue() {
            switch (this) {
                case MULTI_ATTACHMENT:
                    return 1;
                case PLAIN_TEXT:
                    return 2;
                default:
                    return 0;
            }
        }

        /**
         * @return Type object for given attachment type raw value according to EIP-29
         */
        public static Type fromTypeRawValue(int typeRawValue) {
            for (Type value : Type.values()) {
                if (value.toTypeRawValue() == typeRawValue)
                    return value;
            }
            return UNDEFINED;
        }
    }

    /**
     * Attachment containing a simple text
     */
    public static class PlainTextAttachment extends Eip29Attachment {
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

        public static Eip29Attachment.PlainTextAttachment buildForText(String text) {
            return new PlainTextAttachment(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Attachment containing list of attachments
     */
    public static class MultiAttachment extends Eip29Attachment {
        private final Tuple2[] attachmentList;

        private MultiAttachment(byte[] attachmentContent) {
            super(Type.MULTI_ATTACHMENT.toTypeRawValue(), attachmentContent);

            ErgoValue<?> attachmentTuples = ErgoValue.fromHex(new String(attachmentContent, StandardCharsets.UTF_8));
            if (!attachmentTuples.getType().equals(ErgoType.collType(ErgoType.pairType(ErgoType.integerType(), ErgoType.collType(ErgoType.byteType()))))) {
                throw new IllegalArgumentException("Multi attachment content needs to be Coll[(Int, Coll[Byte])]");
            }

            attachmentList = (Tuple2[]) ((Coll) attachmentTuples.getValue()).toArray();
        }

        private MultiAttachment(byte[] attachmentContent, Tuple2[] attachmentList) {
            super(Type.MULTI_ATTACHMENT.toTypeRawValue(), attachmentContent);
            this.attachmentList = attachmentList;
        }

        /**
         * @return i-th attachment of this multi attachment
         */
        public Eip29Attachment getAttachment(int i) {
            return Eip29Attachment.createFromAttachmentTuple(attachmentList[i]);
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
        public static Eip29Attachment.MultiAttachment buildForList(List<Eip29Attachment> attachments) {
            List<Tuple2> attachmentTuples = new ArrayList<>(attachments.size());
            for (Eip29Attachment attachment : attachments) {
                attachmentTuples.add(attachment.getErgoValue().getValue()._2);
            }
            Tuple2[] tupleArray = attachmentTuples.toArray(new Tuple2[]{});
            ErgoValue ergoValue = ErgoValue.of(tupleArray, ErgoType.pairType(ErgoType.integerType(), ErgoType.collType(ErgoType.byteType())));

            return new MultiAttachment(ergoValue.toHex().getBytes(StandardCharsets.UTF_8), tupleArray);
        }
    }
}
