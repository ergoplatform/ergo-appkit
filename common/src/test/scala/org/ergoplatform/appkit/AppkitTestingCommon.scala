package org.ergoplatform.appkit

import scalan.RType
import sigmastate.eval.SigmaDsl

trait AppkitTestingCommon {
  /** The mnemonic used in tests and test vectors. */
  val mnemonic = SecretString.create("slow silly start wash bundle suffer bulb ancient height spin express remind today effort helmet")

  /** Testnet address generated from `mnemonic` and corresponding to master key.
   * (i.e. `m / 0` derivation path).
   */
  val addrStr = "3WzR39tWQ5cxxWWX6ys7wNdJKLijPeyaKgx72uqg9FJRBCdZPovL"

  /** Mainnet address generated from `mnemonic` and corresponding to first EIP-3 address..
   * (i.e. `m/44'/429'/0'/0/0` derivation path).
   */
  val firstEip3AddrStr = "9eatpGQdYNjTi5ZZLK7Bo7C3ms6oECPnxbQTRn6sDcBNLMYSCa8"

  /** Mainnet address generated from `mnemonic` and corresponding to second EIP-3 address..
   * (i.e. `m/44'/429'/0'/0/1` derivation path).
   */
  val secondEip3AddrStr = "9iBhwkjzUAVBkdxWvKmk7ab7nFgZRFbGpXA9gP6TAoakFnLNomk"

  /** The address which corresponds to master key of the `mnemonic`. */
  val address = Address.fromMnemonic(
    NetworkType.MAINNET,
    Mnemonic.create(mnemonic, SecretString.empty()))

  /** Helper method to construct a collection from items. */
  def Coll[T](items: T*)(implicit cT: RType[T]) = SigmaDsl.Colls.fromItems(items:_*)
}
