package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.{ScriptValue, ScriptEnvironment}
import io.scalechain.util.Hash

trait Crypto extends ScriptOp

/** OP_RIPEMD160(0xa6) : Return RIPEMD160 hash of top item
  * Before : in
  * After  : hash
  */
case class OpRIPEMD160() extends Crypto {
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
case class OpCodeSparator() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_CHECKSIG(0xac) : Pop a public key and signature and validate the signature for the transaction’s hashed data, return TRUE if matching
  */
case class OpCheckSig() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_CHECKSIGVERIFY(0xad) : Same as CHECKSIG, then OP_VERIFY to halt if not TRUE
  */
case class OpCheckSigVerify() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_CHECKMULTISIG(0xae) : Run CHECKSIG for each pair of signature and public key provided. All must match. Bug in implementation pops an extra value, prefix with OP_NOP as workaround
  */
case class OpCheckMultiSig() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_CHECKMULTISIGVERIFY(0xaf) : Same as CHECKMULTISIG, then OP_VERIFY to halt if not TRUE
  */
case class OpCheckMultiSigVerify() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}
