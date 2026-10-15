[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/common/src/main/java/org/ergoplatform/appkit/Address.java)

The `Address` class in the `ergo-appkit` project provides a set of methods for working with Ergo addresses. An Ergo address is a string representation of an ErgoTree, which is a script that defines the conditions under which a transaction output can be spent. The `Address` class provides methods for creating, parsing, and manipulating Ergo addresses.

The `Address` class has a constructor that takes an `ErgoAddress` object and initializes the instance variables `_address`, `_base58String`, and `_addrBytes`. The `_address` variable is the original `ErgoAddress` object, `_base58String` is the base58-encoded string representation of the address, and `_addrBytes` is the byte array representation of the address.

The `Address` class provides methods for extracting information from an Ergo address. The `getNetworkType()` method returns the `NetworkType` of the address (either `MAINNET` or `TESTNET`). The `isMainnet()` method returns `true` if the address is from the mainnet, and `false` otherwise. The `isP2PK()` method returns `true` if the address is a Pay-to-Public-Key (P2PK) address, and `false` otherwise. The `isP2S()` method returns `true` if the address is a Pay-to-Script (P2S) address, and `false` otherwise.

The `Address` class also provides methods for extracting the public key from a P2PK address. The `asP2PK()` method returns the underlying `P2PKAddress` object, and the `getPublicKey()` method returns the `DLogProtocol.ProveDlog` object representing the public key. The `getPublicKeyGE()` method returns the `GroupElement` object representing the public key.

The `Address` class provides methods for converting an Ergo address to an `ErgoContract` object and to a byte array representation of the ErgoTree's proposition bytes. The `toErgoContract()` method returns an `ErgoContract` object representing the address, and the `toPropositionBytes()` method returns the byte array representation of the ErgoTree's proposition bytes.

The `Address` class also provides methods for creating an `Address` object from a base58-encoded string, from an ErgoTree proposition bytes, and from a mnemonic phrase. The `create()` method creates an `Address` object from a base58-encoded string. The `fromPropositionBytes()` method creates an `Address` object from an ErgoTree proposition bytes. The `fromMnemonic()` method creates an `Address` object from a mnemonic phrase. The `createEip3Address()` method creates an `Address` object from an extended public key using the EIP-3 derivation path.

Overall, the `Address` class provides a set of methods for working with Ergo addresses, including methods for extracting information from an address, converting an address to an `ErgoContract` object or a byte array representation of the ErgoTree's proposition bytes, and creating an `Address` object from various inputs.
## Questions: 
 1. What is the purpose of the `Address` class?
- The `Address` class is used to represent an Ergo address and provides methods to extract information from it.

2. What types of Ergo addresses can be represented by the `Address` class?
- The `Address` class can represent Pay-To-Public-Key (P2PK) and Pay-To-Script (P2S) Ergo addresses.

3. How can an `Address` instance be created from a mnemonic phrase?
- An `Address` instance can be created from a mnemonic phrase using the `fromMnemonic` method, which takes the network type, mnemonic phrase, and optional password as arguments.