package org.ergoplatform.appkit

import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

trait TestingBase extends AnyPropSpec with Matchers with ScalaCheckDrivenPropertyChecks
