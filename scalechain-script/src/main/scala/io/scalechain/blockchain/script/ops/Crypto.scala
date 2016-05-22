package io.scalechain.blockchain.script.ops

import java.util
import io.scalechain.blockchain.proto.Script
import io.scalechain.blockchain.script.TransactionSignature
import io.scalechain.blockchain.{Config, ScriptParseException, ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.{ScriptOpList, ScriptValue, ScriptEnvironment}
import io.scalechain.crypto.ECKey.ECDSASignature
import io.scalechain.util.{ByteArray, HexUtil, Utils}
import HexUtil._
import io.scalechain.crypto.{Hash256, HashFunctions, ECKey}

trait Crypto extends ScriptOp

/** OP_RIPEMD160(0xa6) : Return RIPEMD160 hash of top item
  * Before : in
  * After  : hash
  */
case class OpRIPEMD160() extends Crypto {
  def opCode() = OpCode(0xa6)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpRIPEMD160")
    }

    val topItem = env.stack.pop()
    val hash = HashFunctions.ripemd160(topItem.value)
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
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpSHA1")
    }
    val topItem = env.stack.pop()
    val hash = HashFunctions.sha1(topItem.value)
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
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpSHA256")
    }
    val topItem = env.stack.pop()
    val hash = HashFunctions.sha256(topItem.value)
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
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpHash160")
    }
    val topItem = env.stack.pop()
    val hash = HashFunctions.hash160(topItem.value)
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
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpHash256")
    }
    val topItem = env.stack.pop()
    val hash = HashFunctions.hash256(topItem.value)
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

  def checkSig(script : Script, env : ScriptEnvironment): Unit = {
    assert(script != null)
    assert(env.transaction != null)
    assert(env.transactionInputIndex.isDefined)

    // At least we need to have two items on the stack.
    //   1. a public key.
    //   2. a signature
    if (env.stack.size() < 2) {
      // TODO : Write a test for this branch.
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:CheckSig")
    }

    val publicKey = env.stack.pop()
    val rawSignature = env.stack.pop()

    // Check if the signature format is valid.
    // BUGBUG : See if we always have to check the signature format.
    if (!ECDSASignature.isEncodingCanonical(rawSignature.value)) {
      throw new ScriptEvalException(ErrorCode.InvalidSignatureFormat, "ScriptOp:CheckSig")
    }

    val signature : ECKey.ECDSASignature = ECKey.ECDSASignature.decodeFromDER(rawSignature.value)

    val scriptData : Array[Byte] = TransactionSignature.getScriptForCheckSig(script.data, env.getSigCheckOffset, Array(rawSignature) )

    // use only the low 5 bits from the last byte of the signature to get the hash mode.
    // TODO : The 0x1f constant is from TransactionSignature.sigHashMode of BitcoinJ. Investigate if it is necessary.
    //val howToHash : Int = rawSigature.value.last & 0x1f
    val howToHash : Int = rawSignature.value.last

    val hashOfInput : Hash256 = TransactionSignature.calculateHash(env.transaction, env.transactionInputIndex.get, scriptData, howToHash)

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

    // At least we need to have 5 items on the stack.
    //   1. the number of public keys
    //   2. at least a public key.
    //   3. the number of signatures.
    //   4. at least a signature
    //   5. dummy
    if (env.stack.size() < 5) {
      // TODO : Write a test for this branch.
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:CheckMultiSig, the total number of stack items")
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Step 1 : Get the public key count
    val publicKeyCount = env.stack.popInt().intValue()
    if (publicKeyCount < 0 || publicKeyCount > Config.MAX_PUBLIC_KEYS_FOR_MULTSIG)
      throw new ScriptEvalException(ErrorCode.TooManyPublicKeys, "ScriptOp:CheckMultiSig, the number of public keys")

    // Now, we need to have at least publicKeyCount + 3 items on the stack.
    //   1. publicKeyCount public keys.
    //   2. the number of signatures.
    //   3. at least a signature
    //   4. dummy
    if (env.stack.size() < publicKeyCount + 3) {
      // TODO : Write a test for this branch.
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:CheckMultiSig, the remaining number of stack items after getting the public key count")
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Step 2 : Get the public keys
    val publicKeys : Seq[ScriptValue] = for (i : Int <- 1 to publicKeyCount) yield {
      env.stack.pop()
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Step 3 : Get the signature count
    val signatureCount = env.stack.popInt().intValue()
    if (signatureCount < 0 || signatureCount > publicKeyCount)
      throw new ScriptEvalException(ErrorCode.TooManyPublicKeys, "ScriptOp:CheckMultiSig, the public key count")

    // Now, we need to have at least signatureKeyCount + 1 items on the stack.
    //   1. signatureCount signatures
    //   2. dummy
    if (env.stack.size() < signatureCount + 1) {
      // TODO : Write a test for this branch.
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:CheckMultiSig, the signature count")
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Step 4 : Get the signatures
    val signatures : Seq[ScriptValue] = for (i : Int <- 1 to signatureCount) yield {
      env.stack.pop()
    }

    // The reference implementation had a bug to pop one more item from the stack.
    env.stack.pop();


    ////////////////////////////////////////////////////////////////////////////////
    // Step 5 : Scrub scriptData to get rid of signatures from it.
    val scriptData : Array[Byte] = TransactionSignature.getScriptForCheckSig(script.data, env.getSigCheckOffset, signatures.toArray )

    var isValid = true
    var consumedPublicKeyCount = 0
    var consumedSignatureCount = 0

    ////////////////////////////////////////////////////////////////////////////////
    // Step 6 : For each signature, try to match it with public keys.
    for (rawSignature : ScriptValue <- signatures ) {

      ////////////////////////////////////////////////////////////////////////////////
      // Step 6.1 : Check the signature format.
      // BUGBUG : See if we always have to check the signature format.
      if (!ECDSASignature.isEncodingCanonical(rawSignature.value)) {
        throw new ScriptEvalException(ErrorCode.InvalidSignatureFormat, "ScriptOp:CheckMultiSig, invalid raw signature format.")
      }


      ////////////////////////////////////////////////////////////////////////////////
      // Step 6.2 : Get the hash value from the spending transaction.
      // use only the low 5 bits from the last byte of the signature to get the hash mode.
      // TODO : The 0x1f constant is from TransactionSignature.sigHashMode of BitcoinJ. Investigate if it is necessary.
      //val howToHash : Int = rawSigature.value.last & 0x1f
      val howToHash : Int = rawSignature.value.last

      val hashOfInput : Hash256 = TransactionSignature.calculateHash(env.transaction, env.transactionInputIndex.get, scriptData, howToHash)

      // Step 6.3 : Try to match the signature with a public key
      val signature : ECKey.ECDSASignature = ECKey.ECDSASignature.decodeFromDER(rawSignature.value)
      var signatureVerified = false
      // Loop until we successfully verify the signature.
      while(consumedPublicKeyCount < publicKeyCount &&
            !signatureVerified) {
        val publicKey = publicKeys(consumedPublicKeyCount)
        if (ECKey.verify(hashOfInput.value, signature, publicKey.value)) {
          signatureVerified = true
          consumedSignatureCount +=1
        }
        consumedPublicKeyCount += 1
      }

      val signaturesLeft = signatureCount - consumedSignatureCount
      val publicKeysLeft = publicKeyCount - consumedPublicKeyCount
      if (signaturesLeft > publicKeysLeft) {
        isValid = false
      }
    }

    if (isValid) {
      super.pushTrue(env)
    } else {
      super.pushFalse(env)
    }
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
  //
  // Note : When we create the script from scala program like OpCheckSig(), not by ScriptParser.parse,
  // We may have script set to null. Need to check if it is null first.
  override def toString = {
    val scriptData = if (script == null) ByteArray.arrayToByteArray(Array()) else script.data
    s"OpCheckSig(Script(${scalaHex(scriptData)}))"
  }
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
  *
  *  Ex> An example of 2 of 3 multisig.
  *
  *  <Unlocking Script ; provided by a spending transaction>
  *  ScriptOpList(operations=
  *    Array(
  *      Op0(), // Dummy value. we need it because the reference implementation has a bug
  *             // popping an additional item from the stack at the end of OP_CHECKMULTISIG execution.
  *      OpPush(72,ScriptBytes(bytes("sig2"))), // Second signature.
  *      OpPush(72,ScriptBytes(bytes("sig1")))  // First signature.
  *    )
  *  ),
  *
  *  <Locking Script; attached to UTXO >
  *  ScriptOpList(operations=
  *    Array(
  *      OpNum(2), // The number of required signatures.
  *      OpPush(33,ScriptBytes(bytes("pub key3"))), // Third public key
  *      OpPush(33,ScriptBytes(bytes("pub key2"))), // Second public key
  *      OpPush(33,ScriptBytes(bytes("pub key1"))), // First public key
  *      OpNum(3), // The number of public keys.
  *      OpCheckMultiSig(Script(bytes("..."))) // The OP_CHECKMULTISIG signature.
  *    )
  *  )
  *
  *  The order of signatures should match the order of public keys.
  *  ex> If sig1 matches pub key2, sig2 can not match pub key 1, but it can match pub key 3.
  *      If sig1 matches pub key1, sig2 can either match pub key 2 or 3.
  *
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
