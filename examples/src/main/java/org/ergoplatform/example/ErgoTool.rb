Long = Java.type("java.lang.Long")
JInteger = Java.type("java.lang.Integer")  # to avoid conflict with Ruby's Integer
RestApiErgoClient = Java.type("org.ergoplatform.example.util.RestApiErgoClient")
ErgoClientException = Java.type("org.ergoplatform.appkit.ErgoClientException")
ConstantsBuilder = Java.type("org.ergoplatform.appkit.ConstantsBuilder")
ErgoContract = Java.type("org.ergoplatform.appkit.ErgoContract")
ErgoToolConfig = Java.type("org.ergoplatform.ergotool.ErgoToolConfig")
Parameters = Java.type("org.ergoplatform.appkit.Parameters")

amountToPay = Long.parseLong(ARGV[0])
conf = ErgoToolConfig.load("ergotool.json")
newBoxSpendingDelay = JInteger.parseInt(conf.getParameters().get("newBoxSpendingDelay"))

nodeConf = conf.getNode()
ergoClient = RestApiErgoClient.create(
    nodeConf.getNodeApi().getApiUrl(),
    nodeConf.getNetworkType(),
    nodeConf.getNodeApi().getApiKey())

txJson = ergoClient.execute(lambda {|ctx|  # note, we use lambda to convert code block to Proc object
    wallet = ctx.getWallet()
    txFee = Parameters["MinFee"]
    totalToSpend = amountToPay + txFee
    boxes = wallet.getUnspentBoxes(totalToSpend)
    if !boxes.isPresent()
        raise ErgoClientException, "Not enough coins in the wallet to pay " + totalToSpend, Nil
    end

    prover = ctx.newProverBuilder()
        .withMnemonic(
            nodeConf.getWallet().getMnemonic(),
            nodeConf.getWallet().getPassword())
        .build()

    txB = ctx.newTxBuilder()
    newBox = txB.outBoxBuilder()
        .value(amountToPay)
        .contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("deadline", ctx.getHeight() + newBoxSpendingDelay)
                .item("pkOwner", prover.getP2PKAddress().pubkey())
                .build(),
            "{ sigmaProp(HEIGHT > deadline) && pkOwner }"))
        .build()
    tx = txB.boxesToSpend(boxes.get())
        .outputs(newBox)
        .fee(txFee)
        .sendChangeTo(prover.getP2PKAddress())
        .build();

    signed = prover.sign(tx);
    txId = ctx.sendTransaction(signed);
    signed.toJson(true);
})

print(txJson);