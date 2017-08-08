# Introduction
This document summarizes entry points of major features for code analysis. 
It also summarizes classes for data structures required to implement features.

# Application Programming
## Remote Procedure Call
The entry point for analysis of remote procedure call is JsonRpcMicroservice.runService. For the detailed code execution path, check data/docs/design/api-layer.md.

## Command Line Interface
ScaleChain implements command line interface using bash scripts. data/scripts/jsonrpc/scalechain-cli is the entry point for analysis.

# Peer to Peer Networking
## Peer Discovery
The list of peers is in scalechain.conf. ScaleChainPeer.initializeNetLayer reads them and passes to PeerToPeerNetworking.createPeerCommunicator, which connects to each peer listed in the conf file.

## Block Signing
Each node creates a special transaction that has OP_RETURN with the block header hash that the node wants to sign. The outpoint of the input 0 should have a locking script that matches one of the public key hashes in the scalechain.conf.
scalechain.conf contains a list of public key hashes that each of them corresponds to the public key hash of the private key to use for signing blocks.

## Peer Authentication
Each peer authenticates each other by checking if the public key hash in the locking script of the input 0 in the special transaction for the block signing.
The public key hash should be one of peer public key hashes in the scalechain.conf.


## Block Download
Execution step for block download is as follows.

1. Create getblocks message when an orphan block was received. When a node was disconnected from the blockchain network, it receives orphan blocks as soon as it connects to the network. BlockMessageHandler checks if the received block is an orphan. If yes, it sends getblocks request to get the parents of the orphan block.
2. The peer processes the getblocks message using GetBlocksMessageHandler. It responds with inv messages containing a list of block header hahses required for the requesting node to catch up.
3. The requesting node receives an inv message and processes it using InvMessageHandler, which sends getdata message for each block that the node does not have.
4. The peer processes the getdata messages using GetDataMessageHandler and responds with block message for each of them.
5. The requesting node processes the block messages using BlockMessageHandler.

## Block Reorganization
When a new block arrives, it is passed to Blockchain.putBlock. If the new block has greater chain-work even though it is attached to a fork of the blockchain,
the block reorganization starts. The entry point of the block reorganization is BlockMagnet.reorganize. BlockMagnet is for attaching and detaching blocks during the block reorganization.
TransactionMagnet is for invalidating transactions in the detached blocks and adding transactions in the newly attached blocks.

# Transaction
## Transaction Signing
TransactionSigner.sign signed an unsigned transaction or partly signed transaction. 
It returns SignedTransaction which contains a Transaction with the signature on the unlocking script of each input it signs.

## Transaction Signature Verification
TransactionVerifier.verify verifies inputs of each transaction by executing (1) unlocking script and (2) locking script of each input checking if they produce True on top of the stack. 

# Script Executor
Script executor parses the binary form of scripts (byte array) to produce a list of script operations.
Each script operation implements execute method to execute the script operation using script environment and script stack.

## Parse
ScriptParser.parse parses a given raw script in a byte array to get the list of ScriptOp(s).

## Evaluation
ScriptInterpreter.eval execute a list of parsed script operations. 
After the evaluation, it returns the value on top of the stack.

## Data structures
### ScriptOpList 
List of script operations, which is produced as a result of parsing binary script data.
### ScriptOp
Abstracts a script operation. Subclasses of ScriptOb implements actual execution of a script operation.
For example, Op1 pushes True(1) into the execution stack.
```
/** OP_1 or OP_TRUE(0x51) : Push the value "1" onto the stack
  */
case class Op1() extends Constant {
  def opCode() = OpCode(0x51)

  def execute(env : ScriptEnvironment): Unit = {
    super.pushTrue(env)
  }
}
```

### ScriptStack 
A script operation pops the required number of operations from the stack, and pushes the result on top of the stack.

## ScriptEnvironment
The script executor passes ScriptEnvironment to ScriptOp.execute, so that each operation can keep context data into the ScriptEnvironment object.

# Script Operations 
## Pay to Public Key Hash 
### Unlocking Script
The unlocking script has two OP_PUSH operations, one has a signature, the another has a public key.
OP_PUSH is implemented by OpPush(in Constant.scala)
```
<sig> <pubKey> 
```
### Locking Script 
The locking script has five script operations. 
```
OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
```

1. OP_DUP is implmented by OpDup(in Stack.scala)
2. OP_HASH160 is implemented by OpHash160(in Crypto.scala)
3. <pubKeyHash> pushes the public key hash on top of the stack, and it is implemented by OpPush(in Constant.scala).
4. OP_EQUALVERIFY pops two items from the stack and continues the execution if they are same. Implemented by OpEqualVerify( in BitwiseLogic.scala)
5. CheckSig(in Crypto.scala) implements signature verification, OP_CHECKSIG.

## Pay to Public Key
### Unlocking Script
The unlocking script has one OP_PUSH operations, which is the signature.
OP_PUSH is implemented by OpPush(in Constant.scala)
```
<sig> 
```
### Locking Script 
The locking script has two script operations.

1. OP_PUSH with the public key.
2. CheckSig(in Crypto.scala) implements signature verification, OP_CHECKSIG.
```
<pubKey> OP_CHECKSIG
```

## Pay to Script Hash
```
scriptPubKey: OP_HASH160 <scriptHash> OP_EQUAL 
scriptSig: ..signatures... <serialized script>
```

Execution steps :

1. Check the script hash of the <serialized script>
2. Get the redeem script by deserializing <serialized script>
3. Execute the scriptSig without the <serialized script>
4. Execute the redeem script.

For example, a m-of-n multi-signature transaction has the following script sig and redeem script.
```
scriptSig: 0 <sig1> ... <script>
redeem script: OP_m <pubKey1> ... OP_n OP_CHECKMULTISIG
```

Related code : 
NormalTransactionVerifier.getRedeemScript checks patterns of the locking script and unlocking script to extract the redeem script.
If the redeem script can be extracted, the execution of scripts follow the above execution steps.

## Multi-sig
Locking Script : 
```
0 <sig1> <sig2>  
```

Unlocking Script : 
```
OP_2 <pubKey1> <pubKey2> <pubKey3> OP_3 OP_CHECKMULTISIG
```

Related code : 
OpCheckMultiSig(Crypto.scala) executes OP_CHECKMULTISIG, and it checks the multi-sig by using CheckSig.checkMultiSig. 