package org.ergoplatform.polyglot

abstract class MockedRunner {
  def nodeInfoResp: String
  def lastHeadersResp: String
  def boxResp: String
}
