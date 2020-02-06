# A Graphical Notation for Designing of Ergo Contracts

### Why does it matter? 

[ErgoScript](https://ergoplatform.org/docs/ErgoScript.pdf) is the contracts language on
the Ergo blockchain. Even though it has concise syntax adopted from Scala, it still may
seem confusing at first, because conceptually ErgoScript is quite different from the
conventional languages we all know and love.

It is because Ergo's programming model is declarative whereas conventional programming is
imperative. In the declarative model of Ergo you need to tell the system what it should do
for you leaving it the freedom of choosing how it will be achieved. The typical question
"How I can send ERGs to Alice in ErgoScript?" should be rephrased as "What I should tell
the system so that Alice gets the coins?". From this perspective ErgoScript is one piece
of the puzzle, and the transactions API of
[Appkit](https://github.com/aslesarenko/ergo-appkit) is the other.

Declarative programming models has already won the battle against imperative programming in
many application domains like Big Data, Stream Processing, Deep Learning, Databases, etc. 
In a long term declarative programming is also going to win for blockchain applications.

In this post I want to introduce a graphical notation which can help do design complex
Ergo contracts in a declarative way.

### From Imperative to Declarative

Talking about transactions it is important to understand that in Ethereum a transaction
is a sequence of arbitrary operations executed by Ethereum VM. Unlike this, in Ergo, a
transaction is a mapping between input coins which it spends and output coins which it
creates preserving total balances of ERGs and tokens.

Thus, when in Ethereum contract "We send funds from Alice to Bob" we literally changing
balances and update storage with the concrete set of commands. In Ergo the effect of the
transaction on the state is that input coins (or Boxes in Ergo's parlance) are removed and
output boxes are added to the [UTXO](https://en.wikipedia.org/wiki/Unspent_transaction_output)
set, this happens atomically (all or nothing), and no contract code is necessary.   


It is not possible to "change" anything in ErgoScript because all the inputs, outputs and
other transaction parameters available in the script are immutable.
 

