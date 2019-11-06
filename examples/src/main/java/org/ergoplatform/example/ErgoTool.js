const Long = Java.type("java.lang.Long")
const RestApiErgoClient = Java.type("org.ergoplatform.example.util.RestApiErgoClient")
const ErgoClientException = Java.type("org.ergoplatform.appkit.ErgoClientException")
const ConstantsBuilder = Java.type("org.ergoplatform.appkit.ConstantsBuilder")
const ErgoContract = Java.type("org.ergoplatform.appkit.ErgoContract")
const ErgoToolConfig = Java.type("org.ergoplatform.ergotool.ErgoToolConfig")
const Parameters = Java.type("org.ergoplatform.appkit.Parameters")

const amountToPay = Long.parseLong(process.argv[2]);
const conf = ErgoToolConfig.load("ergotool.json");
const nodeConf = conf.getNode();
const ergoClient = RestApiErgoClient.create(
    nodeConf.getNodeApi().getApiUrl(),
    nodeConf.getNetworkType(),
    nodeConf.getNodeApi().getApiKey());

const newBoxDelay = 30;

const txJson = ergoClient.execute(function (ctx) {
    const wallet = ctx.getWallet();
    const totalToSpend = amountToPay + Parameters.MinFee;
    const boxes = wallet.getUnspentBoxes(totalToSpend);
    if (!boxes.isPresent())
        throw new ErgoClientException("Not enough coins in the wallet to pay " + totalToSpend, null);

    const prover = ctx.newProverBuilder()
        .withMnemonic(
            nodeConf.getWallet().getMnemonic(),
            nodeConf.getWallet().getPassword())
        .build();

    const txB = ctx.newTxBuilder();
    const newBox = txB.outBoxBuilder()
        .value(amountToPay)
        .contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("deadline", ctx.getHeight() + newBoxDelay)
                .item("pkOwner", prover.getP2PKAddress().pubkey())
                .build(),
            "{ sigmaProp(HEIGHT > deadline) && pkOwner }"))
        .build();
    const tx = txB.boxesToSpend(boxes.get())
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(prover.getP2PKAddress())
        .build();

    const signed = prover.sign(tx);
    const txId = ctx.sendTransaction(signed);
    return signed.toJson(true);
});

print(txJson);