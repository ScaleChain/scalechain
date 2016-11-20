package io.scalechain.blockchain.script

import java.io.ByteArrayOutputStream
import java.util
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.ops.OpCodeSparator
import io.scalechain.blockchain.{ErrorCode, TransactionVerificationException}
import io.scalechain.crypto.{Hash256, HashFunctions, TransactionSigHash}
import io.scalechain.util.Utils

object TransactionSignature {
  /** Get the script for verifying if a signature is valid.
    * Also it gets rid of signatures from the given script.
    *
    * @param rawScript The script where we want to remove the signature.
    * @param startOffset Copy bytes from this offset in rawScript to get the script for check sign
    * @param rawSignatures The signatures we are going to remove. To support OP_CHECKMULTISIG, it accepts multiple signatures.
    * @return The script for verifying if a signature is valid.
    */
  fun getScriptForCheckSig(rawScript:Array<Byte>, startOffset:Int, rawSignatures : Array<ScriptValue>) : Array<Byte> {
    // Step 1 : Copy the region of the raw script starting from startOffset
    val scriptFromStartOffset =
      if (startOffset>0)
        util.Arrays.copyOfRange(rawScript, startOffset, rawScript.length)
      else
        rawScript // In most cases, startOffset is 0. Do not copy anything.

    // Step 2 : Remove the signatures from the script if any.
    var signatureRemoved : Array<Byte> = scriptFromStartOffset
    for (rawSignature : ScriptValue <- rawSignatures) {
      signatureRemoved = Utils.removeAllInstancesOf(signatureRemoved, rawSignature.value)
    }

    // Step 3 : Remove OP_CODESEPARATOR if any.
    Utils.removeAllInstancesOfOp(signatureRemoved, OpCodeSparator().opCode().code)
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
  fun calculateHash(transaction : Transaction, transactionInputIndex : Int, scriptData : Array<Byte>, howToHash : Int) : Hash256 {
    // Step 1 : Check if the transactionInputIndex is valid.
    if (transactionInputIndex < 0 || transactionInputIndex >= transaction.inputs.length) {
      throw TransactionVerificationException(ErrorCode.InvalidInputIndex, "calculateHash: invalid transaction input")
    }

    // Step 2 : For each hash type, mutate the transaction.
    val alteredTransaction = alter(transaction, transactionInputIndex, scriptData, howToHash)

    // Step 3 : calculate hash of the transaction.
    calculateHash(alteredTransaction, howToHash)
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
  protected fun alter(transaction : Transaction, transactionInputIndex : Int, scriptData : Array<Byte>, howToHash : Int) : Transaction {

    var currentInputIndex = -1
    val newInputs = transaction.inputs.map { input =>
      currentInputIndex += 1
      input match {
        case normalTx : NormalTransactionInput => {
          val newUnlockingScript =
            if (currentInputIndex == transactionInputIndex) {
              UnlockingScript(scriptData)
            } else {
              UnlockingScript(Array<Byte>())
            }
          normalTx.copy(
            unlockingScript = newUnlockingScript
          )
        }
        // No need to change the generation transaction
        case generationTx : GenerationTransactionInput => generationTx
      }
    }

    transaction.copy(
      inputs = newInputs
    )
  }

  /** Calculate hash value of this transaction for signing/validating a signature.
    *
    * @param transaction The transaction to calculate hash for signature of it.
    * @param howToHash The hash type. A value of Transaction.SigHash.
    * @return The calcuated hash.
    */
  protected fun calculateHash(transaction : Transaction, howToHash : Int) : Hash256 {

    val bout = ByteArrayOutputStream()
    val dout = BlockDataOutputStream(bout)
    try {
      // Step 1 : Serialize the transaction
      val serializedBytes = TransactionCodec.serialize(transaction)
      dout.writeBytes(serializedBytes)
      // Step 2 : Write hash type
      Utils.uint32ToByteStreamLE(0x000000ff & howToHash, dout);
    } finally {
      dout.close()
    }
    // Step 3 : Calculate hash
    HashFunctions.hash256(bout.toByteArray)
  }
}
