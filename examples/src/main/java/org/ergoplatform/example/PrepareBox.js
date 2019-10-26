const MockData = Java.type("org.ergoplatform.example.MockData")
const FileMockedRunner = Java.type("org.ergoplatform.example.util.FileMockedRunner")
const ConstantsBuilder = Java.type("org.ergoplatform.polyglot.ConstantsBuilder")

var res = new FileMockedRunner(MockData.infoFile, MockData.lastHeadersFile, MockData.boxFile).run(function (ctx) {
    var mockTxB = ctx.newTxBuilder()
    var out = mockTxB.outBoxBuilder()
        .contract(ConstantsBuilder.empty(), "{ sigmaProp(CONTEXT.headers.size == 9) }")
        .build()
    var spendingTxB = ctx.newTxBuilder()
    var tx = spendingTxB
        .boxesToSpend(out.convertToInputWith(MockData.mockTxId, 0))
        .outputs(
            spendingTxB.outBoxBuilder()
                .contract(ConstantsBuilder.empty(), "{ true }")
                .build())
        .build()
    var proverB = ctx.newProver()
    var prover = proverB.withSeed("abc").build()
    var signed = prover.sign(tx)
    return signed
})

print(res)