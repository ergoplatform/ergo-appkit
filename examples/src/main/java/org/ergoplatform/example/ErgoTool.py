import sys
import java

Long = java.type("java.lang.Long")
RestApiErgoClient = java.type("org.ergoplatform.example.util.RestApiErgoClient")
ErgoClientException = java.type("org.ergoplatform.appkit.ErgoClientException")
ConstantsBuilder = java.type("org.ergoplatform.appkit.ConstantsBuilder")
ErgoContract = java.type("org.ergoplatform.appkit.ErgoContract")
ErgoToolConfig = java.type("org.ergoplatform.ergotool.ErgoToolConfig")
Parameters = java.type("org.ergoplatform.appkit.Parameters")

amountToPay = Long.parseLong(sys.argv[1])
conf = ErgoToolConfig.load("ergotool.json")
nodeConf = conf.getNode()
ergoClient = RestApiErgoClient.create(
    nodeConf.getNodeApi().getApiUrl(),
    nodeConf.getNetworkType(),
    nodeConf.getNodeApi().getApiKey())

newBoxDelay = 30

def send(ctx):
    wallet = ctx.getWallet()
    totalToSpend = amountToPay + Parameters.MinFee
    boxes = wallet.getUnspentBoxes(totalToSpend)
    if not boxes.isPresent():
        raise ErgoClientException("Not enough coins in the wallet to pay " + totalToSpend, None);

    prover = ctx.\
        newProverBuilder().\
        withMnemonic(
            nodeConf.getWallet().getMnemonic(),
            nodeConf.getWallet().getPassword()).\
        build()

    txB = ctx.newTxBuilder()

    newBox = txB.outBoxBuilder().\
        value(amountToPay).\
        contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("deadline", ctx.getHeight() + newBoxDelay)
                .item("pkOwner", prover.getP2PKAddress().pubkey()).build(),
            "{ sigmaProp(HEIGHT > deadline) && pkOwner }")).\
            build()

    tx = txB.boxesToSpend(boxes.get()).\
        outputs(newBox).\
        fee(Parameters.MinFee).\
        sendChangeTo(prover.getP2PKAddress()).\
        build();

    signed = prover.sign(tx)
    txId = ctx.sendTransaction(signed)
    return signed.toJson(True)

txJson = ergoClient.execute(send)

print(txJson)
