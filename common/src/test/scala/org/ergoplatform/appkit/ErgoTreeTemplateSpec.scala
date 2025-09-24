package org.ergoplatform.appkit

import org.ergoplatform.appkit.AppkitIso.isoErgoTypeToSType
import org.ergoplatform.sdk.JavaHelpers.UniversalConverter
import sigma.ast.{IntConstant, ErgoTree}
import sigmastate.helpers.NegativeTesting
import sigma.serialization.generators.ObjectGenerators
import sigma.ast.{SType, SInt, EQ}
import sigmastate.Plus
import org.ergoplatform.sdk.SdkIsos._
import java.util.{List => JList}

class ErgoTreeTemplateSpec extends TestingBase
  with AppkitTestingCommon
  with ObjectGenerators
  with NegativeTesting {
  val tree = ErgoTree.fromProposition(EQ(IntConstant(10), Plus(IntConstant(9), IntConstant(1))).toSigmaProp)

  property("should create template without parameters") {
    tree.constants.length shouldBe 3
    val template = ErgoTreeTemplate.fromErgoTree(tree)
    template.getParameterCount shouldBe 0
  }

  property("should create template with parameters") {
    val template = ErgoTreeTemplate.fromErgoTree(tree)
      .withParameterPositions(Array(0))
    template.getParameterCount shouldBe 1
    template.getParameterValue(0) shouldBe ErgoValue.of(10)
    val expectedTypes = IndexedSeq(SInt: SType).convertTo[JList[ErgoType[_]]]
    template.getParameterTypes shouldBe expectedTypes
  }

  property("should apply parameters") {
    val template = ErgoTreeTemplate.fromErgoTree(tree)
      .withParameterPositions(Array(0))
    val newTree = template.applyParameters(ErgoValue.of(11))
    val expectedTree = ErgoTree.fromProposition(
      EQ(IntConstant(11), Plus(IntConstant(9), IntConstant(1))).toSigmaProp
    )
    newTree shouldBe expectedTree

    assertExceptionThrown(
      template.applyParameters(ErgoValue.of(11), ErgoValue.of(20)),
      exceptionLike[IllegalArgumentException]("Wrong number of newValues. Expected 1 but was 2")
    )

    assertExceptionThrown(
      template.applyParameters(ErgoValue.of(1.toByte)), // invalid type of ErgoValue (should be Int)
      exceptionLike[IllegalArgumentException]("expected new constant to have the same SInt$ tpe, got SByte$")
    )
  }

  property("should validate parameters") {
    assertExceptionThrown(
      ErgoTreeTemplate.fromErgoTree(tree).withParameterPositions(Array(0, 0)),
      exceptionLike[IllegalArgumentException]("Duplicate positions: [0,0]")
    )
    assertExceptionThrown(
      ErgoTreeTemplate.fromErgoTree(tree).withParameterPositions(Array(3)),
      exceptionLike[IllegalArgumentException]("Invalid parameter position 3")
    )
  }

}
