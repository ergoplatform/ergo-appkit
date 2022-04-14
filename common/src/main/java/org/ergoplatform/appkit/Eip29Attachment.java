package org.ergoplatform.appkit;

import scala.Tuple2;
import special.collection.Coll;

/**
 * Represents an attachment according to EIP-29.
 */
public interface Eip29Attachment {
    /**
     * @return type of this attachment, if known
     */
    Type getType();

    /**
     * @return raw value type of this attachment. Can return types unknown to this implementation.
     */
    int getTypeRawValue();

    /**
     * @return returns full ErgoValue for this attachment, to use for register 9 of out boxes
     */
    ErgoValue<Tuple2<Coll<Byte>, Tuple2<Integer, Coll<Byte>>>> getErgoValue();

    /**
     * @return array R4-R9 to use with OutboxBuilder#registers
     */
    ErgoValue<?>[] getOutboxRegistersForAttachment();

    /**
     * Type of attachment
     */
    enum Type {
        /**
         * This attachment is a list of attachments
         */
        MULTI_ATTACHMENT,
        /**
         * This attachment is a plain text, see {@link GenericEip29Attachment.PlainTextAttachment#getText()}
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
            for (Type value : Eip29Attachment.Type.values()) {
                if (value.toTypeRawValue() == typeRawValue)
                    return value;
            }
            return UNDEFINED;
        }
    }
}
