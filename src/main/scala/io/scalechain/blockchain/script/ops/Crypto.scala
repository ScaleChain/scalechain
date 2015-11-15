package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.util.ECKey.ECDSASignature
import io.scalechain.blockchain.util.{ECKey, Utils}
import io.scalechain.blockchain.{ScriptParseException, ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.{ScriptValue, ScriptEnvironment}
import io.scalechain.util.Hash

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

  override def create(rawScript : Array[Byte], offset : Int) : (ScriptOp, Int) = {
    // The offset is the next byte of the OpCodeSparator OP code in the raw script.
    val sigCheckOffset = offset

    if (sigCheckOffset >= rawScript.length) {
      throw new ScriptParseException(ErrorCode.NoDataAfterCodeSparator)
    }

    (OpCodeSparator(sigCheckOffset), 0)
  }

}

trait CheckSig extends Crypto {
  def getScriptForCheckSig(rawScript:Array[Byte], startOffset:Int) : Array[Byte] = {
    // TODO : Implement
    assert(false);
    null
  }

  def checkSig(rawScript : Array[Byte], env : ScriptEnvironment): Unit = {
    assert(rawScript != null)
    assert(env.transaction != null)
    assert(env.transactionInputIndex.isDefined)

    val publicKey = env.stack.pop()
    val rawSigature = env.stack.pop()

    // Check if the signature format is valid.
    // BUGBUG : See if we always have to check the signature format.
    if (!ECDSASignature.isEncodingCanonical(rawSigature.value)) {
      throw new ScriptEvalException(ErrorCode.InvalidSignatureFormat)
    }

    val howToHash : Int = rawSigature.value.last

    val signature : ECKey.ECDSASignature = ECKey.ECDSASignature.decodeFromDER(rawSigature.value)

    val scriptData : Array[Byte] = getScriptForCheckSig(rawScript, env.getSigCheckOffset )

    val hashOfInput : Array[Byte] = env.transaction.hashForSignature(env.transactionInputIndex.get, scriptData, howToHash)

    if (ECKey.verify(hashOfInput, signature, publicKey.value)) {
      super.pushTrue(env)
    } else {
      super.pushFalse(env)
    }
  }

  def checkMultiSig(rawScript : Array[Byte], env : ScriptEnvironment): Unit = {
    assert(rawScript != null)
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
case class OpCheckSig(val rawScript : Array[Byte] = null) extends CheckSig {
  def opCode() = OpCode(0xac)

  def execute(env : ScriptEnvironment): Unit = {
    assert(rawScript != null)

    super.checkSig(rawScript, env)
  }

  override def create(rawScript : Array[Byte], offset : Int) : (ScriptOp, Int) = {
    (OpCheckSig(rawScript), 0)
  }
}

/** OP_CHECKSIGVERIFY(0xad) : Same as CHECKSIG, then OP_VERIFY to halt if not TRUE
  */
case class OpCheckSigVerify(val rawScript : Array[Byte] = null) extends CheckSig {
  override def opCode() = OpCode(0xad)

  override def execute(env : ScriptEnvironment): Unit = {
    assert(rawScript != null)

    super.checkSig(rawScript, env)
    super.verify(env)
  }

  override def create(rawScript : Array[Byte], offset : Int) : (ScriptOp, Int) = {
    (OpCheckSigVerify(rawScript), 0)
  }

}

/** OP_CHECKMULTISIG(0xae) : Run CHECKSIG for each pair of signature and public key provided. All must match. Bug in implementation pops an extra value, prefix with OP_NOP as workaround
  */
case class OpCheckMultiSig(val rawScript : Array[Byte] = null) extends CheckSig {
  def opCode() = OpCode(0xae)

  def execute(env : ScriptEnvironment): Unit = {
    assert(rawScript != null)

    super.checkMultiSig(rawScript, env)
  }

  override def create(rawScript : Array[Byte], offset : Int) : (ScriptOp, Int) = {
    (OpCheckMultiSig(rawScript), 0)
  }

}

/** OP_CHECKMULTISIGVERIFY(0xaf) : Same as CHECKMULTISIG, then OP_VERIFY to halt if not TRUE
  */
case class OpCheckMultiSigVerify(val rawScript : Array[Byte] = null) extends CheckSig {
  override def opCode() = OpCode(0xaf)
  override def execute(env : ScriptEnvironment): Unit = {
    assert(rawScript != null)

    super.checkMultiSig(rawScript, env)
    super.verify(env)
  }

  override def create(rawScript : Array[Byte], offset : Int) : (ScriptOp, Int) = {
    (OpCheckMultiSigVerify(rawScript), 0)
  }
}
