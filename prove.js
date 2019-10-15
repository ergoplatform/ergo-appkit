
const ErgoLikeContext = Java.type("org.ergoplatform.ErgoLikeContext")
const ErgoUnsafeProver = Java.type("org.ergoplatform.wallet.interface4j.crypto.ErgoUnsafeProver")
const UnsignedErgoLikeTransaction = Java.type("org.ergoplatform.UnsignedErgoLikeTransaction")
const ErgoTransactionBuilder = Java.type("org.ergoplatform.polyglot.ErgoTransactionBuilder")

function printMethods(obj) {
    print("Methods of " + obj)
    for (var m in obj) { print(m); }
}

// print(ErgoLikeContext)

printMethods(ErgoUnsafeProver)
printMethods(UnsignedErgoLikeTransaction)

let prover = new ErgoUnsafeProver

printMethods(prover)

let builder = new ErgoTransactionBuilder

printMethods(builder)

let tx = builder
    .withCandidates()
    .build();

printMethods(tx)
