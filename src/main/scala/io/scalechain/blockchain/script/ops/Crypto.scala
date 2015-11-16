package io.scalechain.blockchain.script.ops

import java.util

import io.scalechain.blockchain.block.Script
import io.scalechain.blockchain.util.ECKey.ECDSASignature
import io.scalechain.blockchain.util.{ECKey, Utils}
import io.scalechain.blockchain.{ScriptParseException, ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.{ScriptValue, ScriptEnvironment}
import io.scalechain.util.{Hash256, Hash}
import io.scalechain.util.HexUtil._

trait Crypto extends ScriptOp

/** OP_RIPEMD160(0xa6) : Return RIPEMD160 hash of top item
  * Before : in
  * After  : hash
  */
case class OpRIPEMD160() extends Crypto {
  def opCode() = OpCode(0xa6)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput)
    }

    val topItem = env.stack.pop()
    val hash = Hash.ripemd160(topItem.value)
    env.stack.push(ScriptValue.valueOf(hash.value))
  }
}

/** OP_SHA1(0xa7) : Return SHA1 hash of top item
  * Before : in
  * After  : hash
  */
case class OpSHA1() extends Crypto {
  def opCode() = OpCode(0xa7)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput)
    }
    val topItem = env.stack.pop()
    val hash = Hash.sha1(topItem.value)
    env.stack.push(ScriptValue.valueOf(hash.value))
  }
}

/** OP_SHA256(0xa8) : Return SHA256 hash of top item
  * Before : in
  * After  : hash
  */
case class OpSHA256() extends Crypto {
  def opCode() = OpCode(0xa8)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput)
    }
    val topItem = env.stack.pop()
    val hash = Hash.sha256(topItem.value)
    env.stack.push(ScriptValue.valueOf(hash.value))
  }
}

/** OP_HASH160(0xa9) : Return RIPEMD160(SHA256(x)) hash of top item
  * Before : in
  * After  : hash
  */
case class OpHash160() extends Crypto {
  def opCode() = OpCode(0xa9)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput)
    }
    val topItem = env.stack.pop()
    val hash = Hash.hash160(topItem.value)
    env.stack.push(ScriptValue.valueOf(hash.value))
  }
}

/** OP_HASH256(0xaa) : Return SHA256(SHA256(x)) hash of top item
  * Before : in
  * After  : hash
  */
case class OpHash256() extends Crypto {
  def opCode() = OpCode(0xaa)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput)
    }
    val topItem = env.stack.pop()
    val hash = Hash.hash256(topItem.value)
    env.stack.push(ScriptValue.valueOf(hash.value))
  }
}

/** OP_CODESEPARATOR(0xab) : Mark the beginning of signature-checked data
  */
case class OpCodeSparator(sigCheckOffset : Int = 0) extends Crypto {
  def opCode() = OpCode(0xab)

  def execute(env : ScriptEnvironment): Unit = {
    // The sigCheckOffset is set by create method, and it should be greater than 0
    assert(sigCheckOffset > 0)
    env.setSigCheckOffset(sigCheckOffset)
  }

  override def create(script : Script, offset : Int) : (ScriptOp, Int) = {
    // The offset is the next byte of the OpCodeSparator OP code in the raw script.
    val sigCheckOffset = offset

    if (sigCheckOffset >= script.length) {
      throw new ScriptParseException(ErrorCode.NoDataAfterCodeSparator)
    }

    (OpCodeSparator(sigCheckOffset), 0)
  }

}

trait CheckSig extends Crypto {
  def getScriptForCheckSig(rawScript:Array[Byte], startOffset:Int, rawSignature : Array[Byte]) : Array[Byte] = {
    // Step 1 : Copy the region of the raw script starting from startOffset
    val scriptFromStartOffset =
      if (startOffset>0)
        util.Arrays.copyOfRange(rawScript, startOffset, rawScript.length)
      else
        rawScript // In most cases, startOffset is 0. Do not copy anything.

    // Step 2 : Remove the signature from the script if any.
    val signatureRemoved = Utils.removeAllInstancesOf(scriptFromStartOffset, rawSignature)

    // Step 3 : Remove OP_CODESEPARATOR if any.
    Utils.removeAllInstancesOfOp(signatureRemoved, OpCodeSparator().opCode().code)
  }

  def checkSig(script : Script, env : ScriptEnvironment): Unit = {
    assert(script != null)
    assert(env.transaction != null)
    assert(env.transactionInputIndex.isDefined)

    val publicKey = env.stack.pop()
    val rawSigature = env.stack.pop()

    // Check if the signature format is valid.
    // BUGBUG : See if we always have to check the signature format.
    if (!ECDSASignature.isEncodingCanonical(rawSigature.value)) {
      throw new ScriptEvalException(ErrorCode.InvalidSignatureFormat)
    }

    // use only the low 5 bits from the last byte of the signature to get the hash mode.
    // TODO : The 0x1f constant is from TransactionSignature.sigHashMode of BitcoinJ. Investigate if it is necessary.
    //val howToHash : Int = rawSigature.value.last & 0x1f
    val howToHash : Int = rawSigature.value.last

    val signature : ECKey.ECDSASignature = ECKey.ECDSASignature.decodeFromDER(rawSigature.value)

    val scriptData : Array[Byte] = getScriptForCheckSig(script.data, env.getSigCheckOffset, rawSigature.value )

    val hashOfInput : Hash256 = env.transaction.hashForSignature(env.transactionInputIndex.get, scriptData, howToHash)

    if (ECKey.verify(hashOfInput.value, signature, publicKey.value)) {
      super.pushTrue(env)
    } else {
      super.pushFalse(env)
    }
  }

  def checkMultiSig(script : Script, env : ScriptEnvironment): Unit = {
    assert(script != null)
    assert(env.transaction != null)
    assert(env.transactionInputIndex.isDefined)

    // TODO : Implement
    assert(false);

    // TODO : Implement it
    assert(false);
  }
}

/** OP_CHECKSIG(0xac) : Pop a public key and signature and validate the signature for the transaction’s hashed data, return TRUE if matching
  * Before : <signature> <public key>
  * After  : (1) 1 if the signature is correct.
  *          (2) an empty array if the signature is not correct.
  * Additional input :
  * Followings are additional input values for calculate hash value.
  * With this hash value and the public key, we can verify if the signature is valid.
  *   1. Transaction ( The transaction we are verifying )
  *   2. Transaction input index ( which has an UTXO )
  *   3. A part of script byte array.
  *   3.1 For calulating hash, we use the byte values after the OP_CODESEPARATOR only.
  *   3.2 Also need to remove signature data if exists.
  *   3.3 Also need to remove OP_CODESEPARATOR operation if exists. ( Need more investigation )
  */
case class OpCheckSig(val script : Script = null) extends CheckSig {
  def opCode() = OpCode(0xac)

  def execute(env : ScriptEnvironment): Unit = {
    assert(script != null)

    super.checkSig(script, env)
  }

  override def create(script : Script, offset : Int) : (ScriptOp, Int) = {
    (OpCheckSig(script), 0)
  }

  // toString of LockingScript/UnlockingScript tries to parse the script to list all operations in it.
  // This causes stack overflow, so do not parse the script which is attached to operations while calling toString.
  override def toString = s"OpCheckSig(Script(${scalaHex(script.data)}))"
}

/** OP_CHECKSIGVERIFY(0xad) : Same as CHECKSIG, then OP_VERIFY to halt if not TRUE
  */
case class OpCheckSigVerify(val script : Script = null) extends CheckSig {
  override def opCode() = OpCode(0xad)

  override def execute(env : ScriptEnvironment): Unit = {
    assert(script != null)

    super.checkSig(script, env)
    super.verify(env)
  }

  override def create(script : Script, offset : Int) : (ScriptOp, Int) = {
    (OpCheckSigVerify(script), 0)
  }

  // toString of LockingScript/UnlockingScript tries to parse the script to list all operations in it.
  // This causes stack overflow, so do not parse the script which is attached to operations while calling toString.
  override def toString = s"OpCheckSigVerify(Script(${scalaHex(script.data)}))"
}

/** OP_CHECKMULTISIG(0xae) : Run CHECKSIG for each pair of signature and public key provided. All must match. Bug in implementation pops an extra value, prefix with OP_NOP as workaround
  *  Before : x sig1 sig2 ... <number of signatures> pub1 pub2 <number of public keys>
  *  After : True if multisig check passes. False otherwise.
  *
  *  Compares the first signature against each public key until it finds an ECDSA match.
  *  Starting with the subsequent public key, it compares the second signature against each remaining public key
  *  until it finds an ECDSA match. The process is repeated until all signatures have been checked or
  *  not enough public keys remain to produce a successful result. All signatures need to match a public key.
  *  Because public keys are not checked again if they fail any signature comparison,
  *  signatures must be placed in the scriptSig using the same order
  *  as their corresponding public keys were placed in the scriptPubKey or redeemScript.
  *  If all signatures are valid, 1 is returned, 0 otherwise.
  *  Due to a bug, one extra unused value is removed from the stack.
  */
case class OpCheckMultiSig(val script : Script = null) extends CheckSig {
  def opCode() = OpCode(0xae)

  def execute(env : ScriptEnvironment): Unit = {
    assert(script != null)

    super.checkMultiSig(script, env)
  }

  override def create(script : Script, offset : Int) : (ScriptOp, Int) = {
    (OpCheckMultiSig(script), 0)
  }

  // toString of LockingScript/UnlockingScript tries to parse the script to list all operations in it.
  // This causes stack overflow, so do not parse the script which is attached to operations while calling toString.
  override def toString = s"OpCheckMultiSig(Script(${scalaHex(script.data)}))"
}

/** OP_CHECKMULTISIGVERIFY(0xaf) : Same as CHECKMULTISIG, then OP_VERIFY to halt if not TRUE
  */
case class OpCheckMultiSigVerify(val script : Script = null) extends CheckSig {
  override def opCode() = OpCode(0xaf)
  override def execute(env : ScriptEnvironment): Unit = {
    assert(script != null)

    super.checkMultiSig(script, env)
    super.verify(env)
  }

  override def create(script : Script, offset : Int) : (ScriptOp, Int) = {
    (OpCheckMultiSigVerify(script), 0)
  }

  // toString of LockingScript/UnlockingScript tries to parse the script to list all operations in it.
  // This causes stack overflow, so do not parse the script which is attached to operations while calling toString.
  override def toString = s"OpCheckMultiSigVerify(Script(${scalaHex(script.data)}))"
}
