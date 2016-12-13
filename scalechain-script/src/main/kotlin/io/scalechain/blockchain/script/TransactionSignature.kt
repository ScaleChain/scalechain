package io.scalechain.blockchain.script

import io.netty.buffer.Unpooled
import java.io.ByteArrayOutputStream

import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.ops.OpCodeSparator
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.TransactionVerificationException
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.crypto.Hash256
import io.scalechain.crypto.HashFunctions
import io.scalechain.crypto.TransactionSigHash
import io.scalechain.util.Utils
import io.scalechain.util.toByteArray
import io.scalechain.util.writeUnsignedIntLE
import java.util.Arrays

object TransactionSignature {
  /** Get the script for verifying if a signature is valid.
    * Also it gets rid of signatures from the given script.
    *
    * @param rawScript The script where we want to remove the signature.
    * @param startOffset Copy bytes from this offset in rawScript to get the script for check sign
    * @param rawSignatures The signatures we are going to remove. To support OP_CHECKMULTISIG, it accepts multiple signatures.
    * @return The script for verifying if a signature is valid.
    */
  fun getScriptForCheckSig(rawScript:ByteArray, startOffset:Int, rawSignatures : Array<ScriptValue>) : ByteArray {
    // Step 1 : Copy the region of the raw script starting from startOffset
    val scriptFromStartOffset =
      if (startOffset>0)
        Arrays.copyOfRange(rawScript, startOffset, rawScript.size)
      else
        rawScript // In most cases, startOffset is 0. Do not copy anything.

    // Step 2 : Remove the signatures from the script if any.
    var signatureRemoved : ByteArray = scriptFromStartOffset
    for (rawSignature : ScriptValue in rawSignatures) {
      signatureRemoved = Utils.removeAllInstancesOf(signatureRemoved, rawSignature.value)
    }

    // Step 3 : Remove OP_CODESEPARATOR if any.
    return Utils.removeAllInstancesOfOp(signatureRemoved, OpCodeSparator().opCode().code.toInt())
  }

  /** Calculate hash value for a given transaction input, and part of script that unlocks the UTXO attached to the input.
    * Why use part of script instead of all script bytes?
    *
    * 1. We need to use bytes after the OP_CODESEPARATOR in the script.
    * 2. We need to get rid of all signature data from the script.
    * 3. We need to get rid of OP_CODESEPARATOR OP code from the script.
    *
    * @param transactionInputIndex The index of the transaction input to get the hash.
    * @param scriptData A part of unlocking script for the UTXO attached to the given transaction input.
    * @param howToHash Decides how to calculate the hash value from this transaction and the given script.
    *                  The value should be one of values in Transaction.SigHash
    * @return The calculated hash value.
    */
  fun calculateHash(transaction : Transaction, transactionInputIndex : Int, scriptData : ByteArray, howToHash : Int) : Hash256 {
    // Step 1 : Check if the transactionInputIndex is valid.
    if (transactionInputIndex < 0 || transactionInputIndex >= transaction.inputs.size) {
      throw TransactionVerificationException(ErrorCode.InvalidInputIndex, "calculateHash: invalid transaction input")
    }

    // Step 2 : For each hash type, mutate the transaction.
    val alteredTransaction = alter(transaction, transactionInputIndex, scriptData, howToHash)

    // Step 3 : calculate hash of the transaction.
    return calculateHash(alteredTransaction, howToHash)
  }

  /** Alter transaction inputs to calculate hash value used for signing/verifying a signature.
    *
    * See CTransactionSignatureSerializer of the Bitcoin core implementation for the details.
    *
    * @param transaction The transaction to alter.
    * @param transactionInputIndex See hashForSignature
    * @param scriptData See hashForSignature
    * @param howToHash See hashForSignature
    */
  private fun alter(transaction : Transaction, transactionInputIndex : Int, scriptData : ByteArray, howToHash : Int) : Transaction {

    var currentInputIndex = -1
    val newInputs = transaction.inputs.map { input ->
      currentInputIndex += 1
      when {
        input is NormalTransactionInput -> {
          val newUnlockingScript =
            if (currentInputIndex == transactionInputIndex) {
              UnlockingScript(scriptData)
            } else {
              UnlockingScript(ByteArray(0))
            }
          input.copy(
            unlockingScript = newUnlockingScript
          )
        }
        // No need to change the generation transaction
        else -> {
          assert( input is GenerationTransactionInput )
          input
        }
      }
    }

    return transaction.copy(
      inputs = newInputs
    )
  }

  /** Calculate hash value of this transaction for signing/validating a signature.
    *
    * @param transaction The transaction to calculate hash for signature of it.
    * @param howToHash The hash type. A value of Transaction.SigHash.
    * @return The calcuated hash.
    */
  fun calculateHash(transaction : Transaction, howToHash : Int) : Hash256 {

    val writeBuffer = Unpooled.buffer()
    val io = CodecInputOutputStream(writeBuffer, isInput = false)

    // Step 1 : Serialize the transaction
    TransactionCodec.transcode(io, transaction)
    // Step 2 : Write hash type
    writeBuffer.writeUnsignedIntLE( (0x000000ff and howToHash).toLong())

    // Step 3 : Calculate hash
    return HashFunctions.hash256(writeBuffer.toByteArray())
  }
}
