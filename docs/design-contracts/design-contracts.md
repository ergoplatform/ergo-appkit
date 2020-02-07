# A Graphical Notation for Designing of Ergo Contracts

### Why does it matter? 

[ErgoScript](https://ergoplatform.org/docs/ErgoScript.pdf) is the contracts language on
the Ergo blockchain. Even though it has concise syntax adopted from Scala, it still may
seem confusing at first, because conceptually ErgoScript is quite different from the
conventional languages which we all know and love.

It is because Ergo's programming model is declarative whereas conventional programming is
imperative. In the declarative model of Ergo you need to tell the system what it should do
for you leaving it the freedom of choosing how it will be achieved. The typical question
"How I can send ERGs to Alice in ErgoScript?" should be rephrased as "What I should tell
the system so that Alice gets the coins?". From this perspective ErgoScript is one piece
of the puzzle, and the transactions API of
[Appkit](https://github.com/aslesarenko/ergo-appkit) is the other (we will talk about this
later in this post).

Declarative programming models has already won the battle against imperative programming
in many application domains like Big Data, Stream Processing, Deep Learning, Databases,
etc. In a long term declarative model is also going to win the hearts of dApp developers.

In this post I want to introduce a graphical notation which can help in designing of complex
Ergo contracts in a declarative way.

### From Imperative to Declarative

In the imperative programming model of Ethereum a transaction is a sequence of operations
executed by Ethereum VM. The following [Solidity
function](https://solidity.readthedocs.io/en/develop/introduction-to-smart-contracts.html#subcurrency-example)
implements a transfer of tokens from `sender` to `receiver`. The transaction starts when
`sender` calls this function on an instance of the contract and ends when the function
returns.

```
// Sends an amount of existing coins from any caller to an address
function send(address receiver, uint amount) public {
    require(amount <= balances[msg.sender], "Insufficient balance.");
    balances[msg.sender] -= amount;
    balances[receiver] += amount;
    emit Sent(msg.sender, receiver, amount);
}
```

The function first checks the pre-conditions, then updates the storage (i.e. balances) and
then publish the post-condition as Sent event. The gas consumed by transaction is sent
to the miner as a reward for executing this transaction.

Unlike Ethereum, in Ergo, a transaction is a mapping between input coins which it spends
and output coins which it creates preserving total balances of ERGs and tokens (in which
Ergo is similar to Bitcoin). 

Since Ergo natively support tokens for this specific example of sending tokens we don't
need to write any code in ErgoScript.
What we need instead is to create the 'send' transaction shown in the following figure.

![Debugger](send-tx.png)

We need in particular:
1) select unspent sender's boxes, containing in total `tB >= amount` of tokens and `B >=
txFee + minErg` ERGs
2) create one output (box) which is protected by `recipient` public key with `minErg` ERGs and
`amount` of tokens
3) create one _fee_ output protected by the minerFee contract with `txFee` ERGs 
4) create one _change_ output protected by `sender` public key, containing
`B - minErg - txFee` ERGs and `tB - amount` tokens.
5) create a new transaction, sign it using the sender's secret key and send to the Ergo
network.

Note, that all the transaction creation is done off-chain using Appkit Transaction API, thus it
is free of gas. To make it simple Appkit implements a library so
that the basic transactions like this can be created using a single method call.

Thus, when in the Ethereum contract "We send amount from sender to recipient" we literally
changing balances and update storage with the concrete set of commands, and this happens
on-chain. 

In Ergo (as in Bitcoin) the transaction of created off-chain and the effects of the
transaction on the state is that input coins (or Boxes in Ergo's parlance) are removed and
output boxes are added to the
[UTXO](https://en.wikipedia.org/wiki/Unspent_transaction_output) set, this happens
atomically (all or nothing), and no contract code is necessary in this simple example.


### From Changing State to Checking Context 

The figure above 
It is not possible to "change" anything in ErgoScript because all the inputs, outputs and
other transaction parameters available in the script are immutable.
 

