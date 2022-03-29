package org.ergoplatform.appkit;

import org.ergoplatform.appkit.impl.Eip29AttachmentBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import scala.Tuple2;

public class Eip29AttachmentTest {

    private static final String textAttachmentContent = "Your loan January";

    @Test
    public void buildPlainTextTest() {
        Eip29Attachment.PlainTextAttachment attachment = Eip29AttachmentBuilder.createPlainTextAttachment(textAttachmentContent);
        Assert.assertNotNull(attachment);
        ErgoValue<Tuple2<byte[], Tuple2<Integer, byte[]>>> ergoValue = attachment.getErgoValue();
        Assert.assertNotNull(ergoValue);
        Eip29Attachment backFromValue = Eip29Attachment.createFromErgoValue(ergoValue);
        Assert.assertEquals(Eip29Attachment.Type.PLAIN_TEXT, backFromValue.getType());
        Assert.assertTrue(backFromValue instanceof Eip29Attachment.PlainTextAttachment);
        Assert.assertEquals(attachment.getText(), ((Eip29Attachment.PlainTextAttachment) backFromValue).getText());

        Eip29Attachment backFromHex = Eip29AttachmentBuilder.buildFromHexEncodedErgoValue("3c0e400e035052500411596f7572206c6f616e204a616e75617279");
        Assert.assertEquals(attachment.getText(), ((Eip29Attachment.PlainTextAttachment) backFromHex).getText());
    }

    @Test
    public void buildMultiAttachmentTest() {
        Eip29Attachment.MultiAttachment multiAttachment = Eip29AttachmentBuilder.createMultiAttachment(Collections.singletonList(Eip29AttachmentBuilder.createPlainTextAttachment(textAttachmentContent)));
        Assert.assertEquals(1, multiAttachment.getAttachmentCount());
        Assert.assertEquals(textAttachmentContent, ((Eip29Attachment.PlainTextAttachment) multiAttachment.getAttachment(0)).getText());
        ErgoValue<Tuple2<byte[], Tuple2<Integer, byte[]>>> ergoValue = multiAttachment.getErgoValue();
        Assert.assertEquals("3c0e400e03505250022e30633430306530313034313135393666373537323230366336663631366532303461363136653735363137323739", ergoValue.toHex());

        Eip29Attachment backFromValue = Eip29Attachment.createFromErgoValue(ergoValue);
        Assert.assertEquals(Eip29Attachment.Type.MULTI_ATTACHMENT, backFromValue.getType());
        Assert.assertTrue(backFromValue instanceof Eip29Attachment.MultiAttachment);
        Eip29Attachment.MultiAttachment multiBackFromValue = (Eip29Attachment.MultiAttachment) backFromValue;
        Assert.assertEquals(multiAttachment.getAttachmentCount(), multiBackFromValue.getAttachmentCount());
        Assert.assertEquals(textAttachmentContent, ((Eip29Attachment.PlainTextAttachment) multiBackFromValue.getAttachment(0)).getText());
    }
}
