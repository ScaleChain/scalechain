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

## Transaction Validation

# Script Executor

2. 실행기 시작지점 

3. 주요 구조체 

4. 실행기 동작 방식
바이너리 스크립트 => 파싱 => ScriptOpList => 실행 => 성공/실패로 결과



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
OP_DUP is implmented by OpDup(in Stack.scala)
OP_HASH160 is implemented by OpHash160(in Crypto.scala)
<pubKeyHash> pushes the public key hash on top of the stack, and it is implemented by OpPush(in Constant.scala).
OP_EQUALVERIFY pops two items from the stack and continues the execution if they are same. Implemented by OpEqualVerify( in BitwiseLogic.scala)
CheckSig(in Crypto.scala) implements signature verification, OP_CHECKSIG.

## Pay to Public Key
## Pay to Script Hash
## Multi-sig