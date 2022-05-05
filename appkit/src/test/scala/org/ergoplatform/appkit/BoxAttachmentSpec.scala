package org.ergoplatform.appkit

import org.ergoplatform.appkit.impl.BoxAttachmentBuilder
import org.junit.Assert
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}
import org.scalatest.PropSpec

import java.util.Collections

class BoxAttachmentSpec extends PropSpec {

  private val textAttachmentContent = "Your loan January"

  property("build plain text") {
    val attachment = BoxAttachmentBuilder.createPlainTextAttachment(textAttachmentContent)
    attachment shouldNot be (null)
    val ergoValue = attachment.getErgoValue
    ergoValue shouldNot be (null)
    val backFromValue: BoxAttachment = BoxAttachmentGeneric.createFromErgoValue(ergoValue)
    BoxAttachment.Type.PLAIN_TEXT shouldBe backFromValue.getType
    backFromValue.isInstanceOf[BoxAttachmentPlainText] shouldBe true
    attachment.getText shouldBe backFromValue.asInstanceOf[BoxAttachmentPlainText].getText

    val backFromHex: BoxAttachment = BoxAttachmentBuilder.buildFromHexEncodedErgoValue("3c0e400e035052500411596f7572206c6f616e204a616e75617279")
    attachment.getText shouldBe backFromHex.asInstanceOf[BoxAttachmentPlainText].getText

    val outboxRegistersForAttachment: Array[ErgoValue[_]] = backFromHex.getOutboxRegistersForAttachment
    outboxRegistersForAttachment.length shouldBe 6
    ErgoValue.fromHex(outboxRegistersForAttachment(0).toHex) shouldBe ErgoValue.unit
  }

  property("multi attachment test") {
    val multiAttachment = BoxAttachmentBuilder.createMultiAttachment(
        Collections.singletonList(BoxAttachmentBuilder.createPlainTextAttachment(textAttachmentContent))
    )
    multiAttachment.getAttachmentCount shouldBe 1
    textAttachmentContent shouldBe multiAttachment.getAttachment(0).asInstanceOf[BoxAttachmentPlainText].getText
    val ergoValue = multiAttachment.getErgoValue
    Assert.assertEquals("3c0e400e03505250022e30633430306530313034313135393666373537323230366336663631366532303461363136653735363137323739", ergoValue.toHex)

    val backFromValue = BoxAttachmentGeneric.createFromErgoValue(ergoValue)
    backFromValue.getType shouldBe BoxAttachment.Type.MULTI_ATTACHMENT
    backFromValue.isInstanceOf[BoxAttachmentMulti] shouldBe true
    val multiBackFromValue = backFromValue.asInstanceOf[BoxAttachmentMulti]
    multiAttachment.getAttachmentCount shouldBe multiBackFromValue.getAttachmentCount
    textAttachmentContent shouldBe multiBackFromValue.getAttachment(0).asInstanceOf[BoxAttachmentPlainText].getText
  }
}
