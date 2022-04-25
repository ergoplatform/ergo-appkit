package org.ergoplatform.appkit;

import java.nio.charset.StandardCharsets;

/**
 * EIP-29 attachment containing a simple text
 */
public class BoxAttachmentPlainText extends BoxAttachmentGeneric {
    private final String text;

    public BoxAttachmentPlainText(byte[] attachmentContent) {
        super(Type.PLAIN_TEXT.toTypeRawValue(), attachmentContent);

        text = new String(attachmentContent, StandardCharsets.UTF_8);
    }

    /**
     * @return text, attachment content
     */
    public String getText() {
        return text;
    }

    public static BoxAttachmentPlainText buildForText(String text) {
        return new BoxAttachmentPlainText(text.getBytes(StandardCharsets.UTF_8));
    }
}
