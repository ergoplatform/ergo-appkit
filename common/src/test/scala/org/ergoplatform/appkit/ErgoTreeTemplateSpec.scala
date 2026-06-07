package org.ergoplatform.appkit

import org.ergoplatform.appkit.AppkitIso.isoErgoTypeToSType
import sigma.ast.{ErgoTree, IntConstant, BoolToSigmaProp}
import sigma.ast.{EQ, SInt, SType}

import java.util.{List => JList}
import scala.collection.JavaConverters._

class ErgoTreeTemplateSpec extends TestingBase
  with AppkitTestingCommon {

  // Build tree via BoolToSigmaProp wrapper since fromProposition requires SigmaPropValue in Sigma 6.0
  // Use simple EQ without Plus to avoid ArithOp import issues
  val tree = {
    val prop = BoolToSigmaProp(EQ(IntConstant(10), IntConstant(10)))
    ErgoTree.fromProposition(prop)
  }

  property("should create template without parameters") {
    tree.constants.length shouldBe 2
    val template = ErgoTreeTemplate.fromErgoTree(tree)
    template.getParameterCount shouldBe 0
  }

  property("should create template with parameters") {
    val template = ErgoTreeTemplate.fromErgoTree(tree)
      .withParameterPositions(Array(0))
    template.getParameterCount shouldBe 1
    template.getParameterValue(0) shouldBe ErgoValue.of(10)
    val expectedTypes: JList[ErgoType[_]] = IndexedSeq(SInt: SType).map(t => isoErgoTypeToSType.from(t)).asJava
    template.getParameterTypes shouldBe expectedTypes
  }

  property("should apply parameters") {
    val template = ErgoTreeTemplate.fromErgoTree(tree)
      .withParameterPositions(Array(0))
    val newTree = template.applyParameters(ErgoValue.of(11))
    val expectedTree = {
      val prop = BoolToSigmaProp(EQ(IntConstant(11), IntConstant(10)))
      ErgoTree.fromProposition(prop)
    }
    newTree shouldBe expectedTree

    val ex1 = intercept[IllegalArgumentException] {
      template.applyParameters(ErgoValue.of(11), ErgoValue.of(20))
    }
    ex1.getMessage should include("Wrong number of newValues")

    val ex2 = intercept[IllegalArgumentException] {
      template.applyParameters(ErgoValue.of(1.toByte)) // invalid type of ErgoValue (should be Int)
    }
    ex2.getMessage should include("expected new constant to have the same")
  }

  property("should validate parameters") {
    val ex3 = intercept[IllegalArgumentException] {
      ErgoTreeTemplate.fromErgoTree(tree).withParameterPositions(Array(0, 0))
    }
    ex3.getMessage should include("Duplicate positions")

    val ex4 = intercept[IllegalArgumentException] {
      ErgoTreeTemplate.fromErgoTree(tree).withParameterPositions(Array(2))
    }
    ex4.getMessage should include("Invalid parameter position 2")
  }

}
