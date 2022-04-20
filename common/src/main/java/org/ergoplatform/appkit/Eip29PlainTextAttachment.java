package org.ergoplatform.appkit;

import java.nio.charset.StandardCharsets;

/**
 * EIP-29 attachment containing a simple text
 */
public class Eip29PlainTextAttachment extends Eip29GenericAttachment {
    private final String text;

    public Eip29PlainTextAttachment(byte[] attachmentContent) {
        super(Type.PLAIN_TEXT.toTypeRawValue(), attachmentContent);

        text = new String(attachmentContent, StandardCharsets.UTF_8);
    }

    /**
     * @return text, attachment content
     */
    public String getText() {
        return text;
    }

    public static Eip29PlainTextAttachment buildForText(String text) {
        return new Eip29PlainTextAttachment(text.getBytes(StandardCharsets.UTF_8));
    }
}
