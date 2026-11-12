package org.ergoplatform.appkit

import org.ergoplatform.appkit.impl.BoxAttachmentBuilder
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec

import java.nio.charset.StandardCharsets
import java.util.Collections

class BoxAttachmentMultiSpec extends AnyPropSpec with Matchers {

  private def multiAttachmentValueWithContent(content: Array[Byte]): ErgoValue[_] = {
    val contentPair = ErgoValue.pairOf(
      ErgoValue.of(BoxAttachment.Type.MULTI_ATTACHMENT.toTypeRawValue),
      ErgoValue.of(content))
    ErgoValue.pairOf(ErgoValue.of(BoxAttachment.MAGIC_BYTES), contentPair)
  }

  property("malformed multi attachment content is rejected with IllegalArgumentException") {
    // valid multi attachment parses as before (pin)
    val validMulti = BoxAttachmentBuilder.createMultiAttachment(
      Collections.singletonList(BoxAttachmentBuilder.createPlainTextAttachment("Your loan January")))
    val validBack = BoxAttachmentGeneric.createFromErgoValue(validMulti.getErgoValue)
    validBack.getType shouldBe BoxAttachment.Type.MULTI_ATTACHMENT
    validBack.asInstanceOf[BoxAttachmentMulti].getAttachmentCount shouldBe 1

    // content that is a serialized constant of the wrong type is already rejected
    // with IllegalArgumentException by the constructor's type check (pin)
    val wrongTypeContent = ErgoValue.of(42).toHex.getBytes(StandardCharsets.UTF_8)
    assertThrows[IllegalArgumentException] {
      BoxAttachmentGeneric.createFromErgoValue(multiAttachmentValueWithContent(wrongTypeContent))
    }

    // content that is not hex at all
    val notHexContent = "this is not hex".getBytes(StandardCharsets.UTF_8)
    assertThrows[IllegalArgumentException] {
      BoxAttachmentGeneric.createFromErgoValue(multiAttachmentValueWithContent(notHexContent))
    }

    // content that is hex, but not a serialized constant
    val notAConstantContent = "ff".getBytes(StandardCharsets.UTF_8)
    assertThrows[IllegalArgumentException] {
      BoxAttachmentGeneric.createFromErgoValue(multiAttachmentValueWithContent(notAConstantContent))
    }

    // empty content
    assertThrows[IllegalArgumentException] {
      BoxAttachmentGeneric.createFromErgoValue(multiAttachmentValueWithContent(new Array[Byte](0)))
    }

    // the unmasked hex entry point rejects the same malformation the same way
    val notAConstantValue = multiAttachmentValueWithContent(notAConstantContent)
    assertThrows[IllegalArgumentException] {
      BoxAttachmentBuilder.buildFromHexEncodedErgoValue(notAConstantValue.toHex)
    }
  }
}
