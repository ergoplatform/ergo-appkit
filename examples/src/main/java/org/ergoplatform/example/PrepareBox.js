const MockData = Java.type("org.ergoplatform.example.MockData")
const FileMockedErgoClient = Java.type("org.ergoplatform.example.util.FileMockedErgoClient")
const ConstantsBuilder = Java.type("org.ergoplatform.appkit.ConstantsBuilder")
const ErgoContract = Java.type("org.ergoplatform.appkit.ErgoContract")

var ergoScript = process.argv[2]
if (typeof(ergoScript) == "undefined")
  ergoScript = "{ sigmaProp(CONTEXT.headers.size == 9) }"

// print all command line arguments
// for (let j = 0; j < process.argv.length; j++) {
//     console.log(j + ' -> ' + (process.argv[j]));
// }

var res = new FileMockedErgoClient(MockData.infoFile, MockData.lastHeadersFile, MockData.boxFile)
   .execute(function (ctx) {
        var mockTxB = ctx.newTxBuilder()
        var out = mockTxB.outBoxBuilder()
            .contract(ErgoContract.create(ConstantsBuilder.empty(), ergoScript))
            .build()
        var spendingTxB = ctx.newTxBuilder()
        var tx = spendingTxB
            .boxesToSpend(out.convertToInputWith(MockData.mockTxId, 0))
            .outputs(
                spendingTxB.outBoxBuilder()
                    .contract(ErgoContract.create(ConstantsBuilder.empty(), "{ true }"))
                    .build())
            .build()
        var proverB = ctx.newProverBuilder()
        var prover = proverB.withMnemonic("abc", null).build()
        var signed = prover.sign(tx)
        return signed
    })

print(res)