# Introduction
This document summarizes entry points of major features for code analysis. 
It also summarizes classes for data structures required to implement features.

# Application Programming
## Remote Procedure Call
## Command Line Interface

# Peer to Peer Networking
## Peer Discovery
## Peer Authentication
## Block Signing
## Block Download
## Block Reorganization

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


## Pay to Script Hash
```
scriptPubKey: OP_HASH160 <scriptHash> OP_EQUAL 
scriptSig: ..signatures... <serialized script>
```

```
m-of-n multi-signature transaction:
scriptSig: 0 <sig1> ... <script>
script: OP_m <pubKey1> ... OP_n OP_CHECKMULTISIG
```

## Multi-sig
Locking Script : 
```
0 <sig1> <sig2>  
```

Unlocking Script : 
```
OP_2 <pubKey1> <pubKey2> <pubKey3> OP_3 OP_CHECKMULTISIG
```