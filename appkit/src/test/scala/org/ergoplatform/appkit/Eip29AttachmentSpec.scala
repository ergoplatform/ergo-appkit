package org.ergoplatform.appkit

import org.ergoplatform.appkit.impl.Eip29AttachmentBuilder
import org.junit.Assert
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}
import org.scalatest.PropSpec

import java.util.Collections

class Eip29AttachmentSpec extends PropSpec {

  private val textAttachmentContent = "Your loan January"

  property("build plain text") {
    val attachment = Eip29AttachmentBuilder.createPlainTextAttachment(textAttachmentContent)
    attachment shouldNot be (null)
    val ergoValue = attachment.getErgoValue
    ergoValue shouldNot be (null)
    val backFromValue: Eip29Attachment = GenericEip29Attachment.createFromErgoValue(ergoValue)
    Eip29Attachment.Type.PLAIN_TEXT shouldBe backFromValue.getType
    backFromValue.isInstanceOf[GenericEip29Attachment.PlainTextAttachment] shouldBe true
    attachment.getText shouldBe backFromValue.asInstanceOf[GenericEip29Attachment.PlainTextAttachment].getText

    val backFromHex: Eip29Attachment = Eip29AttachmentBuilder.buildFromHexEncodedErgoValue("3c0e400e035052500411596f7572206c6f616e204a616e75617279")
    attachment.getText shouldBe backFromHex.asInstanceOf[GenericEip29Attachment.PlainTextAttachment].getText

    val outboxRegistersForAttachment: Array[ErgoValue[_]] = backFromHex.getOutboxRegistersForAttachment
    outboxRegistersForAttachment.length shouldBe 6
    ErgoValue.fromHex(outboxRegistersForAttachment(0).toHex) shouldBe ErgoValue.unit
  }

  property("multi attachment test") {
    val multiAttachment = Eip29AttachmentBuilder.createMultiAttachment(Collections.singletonList(Eip29AttachmentBuilder.createPlainTextAttachment(textAttachmentContent)))
    multiAttachment.getAttachmentCount shouldBe 1
    textAttachmentContent shouldBe multiAttachment.getAttachment(0).asInstanceOf[GenericEip29Attachment.PlainTextAttachment].getText
    val ergoValue = multiAttachment.getErgoValue
    Assert.assertEquals("3c0e400e03505250022e30633430306530313034313135393666373537323230366336663631366532303461363136653735363137323739", ergoValue.toHex)

    val backFromValue = GenericEip29Attachment.createFromErgoValue(ergoValue)
    backFromValue.getType shouldBe Eip29Attachment.Type.MULTI_ATTACHMENT
    backFromValue.isInstanceOf[GenericEip29Attachment.MultiAttachment] shouldBe true
    val multiBackFromValue = backFromValue.asInstanceOf[GenericEip29Attachment.MultiAttachment]
    multiAttachment.getAttachmentCount shouldBe multiBackFromValue.getAttachmentCount
    textAttachmentContent shouldBe multiBackFromValue.getAttachment(0).asInstanceOf[GenericEip29Attachment.PlainTextAttachment].getText
  }
}
