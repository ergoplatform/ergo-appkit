package org.ergoplatform.appkit

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

trait TestingBase extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks
