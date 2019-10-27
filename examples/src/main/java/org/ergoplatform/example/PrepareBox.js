const MockData = Java.type("org.ergoplatform.example.MockData")
const FileMockedRunner = Java.type("org.ergoplatform.example.util.FileMockedRunner")
const ConstantsBuilder = Java.type("org.ergoplatform.polyglot.ConstantsBuilder")

var ergoScript = process.argv[2]
if (typeof(ergoScript) == "undefined")
  ergoScript = "{ sigmaProp(CONTEXT.headers.size == 9) }"

// print all command line arguments
// for (let j = 0; j < process.argv.length; j++) {
//     console.log(j + ' -> ' + (process.argv[j]));
// }

var res = new FileMockedRunner(MockData.infoFile, MockData.lastHeadersFile, MockData.boxFile)
   .run(function (ctx) {
        var mockTxB = ctx.newTxBuilder()
        var out = mockTxB.outBoxBuilder()
            .contract(ConstantsBuilder.empty(), ergoScript)
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