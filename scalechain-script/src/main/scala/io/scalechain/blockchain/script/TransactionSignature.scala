package io.scalechain.blockchain.script

import java.io.ByteArrayOutputStream
import io.scalechain.blockchain.block._
import io.scalechain.blockchain.block.codec.BlockSerializer
import io.scalechain.blockchain.proto.{UnlockingScript, NormalTransactionInput, TransactionInput, Transaction}
import io.scalechain.blockchain.{ErrorCode, TransactionVerificationException}
import io.scalechain.crypto.{Hash256, HashFunctions, TransactionSigHash}
import io.scalechain.io.BlockDataOutputStream
import io.scalechain.util.Utils

object TransactionSignature {
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
  def calculateHash(transaction : Transaction, transactionInputIndex : Int, scriptData : Array[Byte], howToHash : Int) : Hash256 = {
    // Step 1 : Check if the transactionInputIndex is valid.
    if (transactionInputIndex < 0 || transactionInputIndex >= transaction.inputs.length) {
      throw new TransactionVerificationException(ErrorCode.InvalidInputIndex)
    }

    // Step 2: copy each field of this transaction and create a new one.
    //         Why? To change some fields of the transaction to calculate hash value from it.
    val copiedTransaction : Transaction = transaction.copy()

    // Step 3 : For each hash type, mutate the transaction.
    alter(copiedTransaction, transactionInputIndex, scriptData, howToHash)

    // Step 4 : calculate hash of the transaction.
    calculateHash(copiedTransaction, howToHash)
  }

  /** Utility function : For each normal transaction inputs, call a mutate function.
    * Pass the index to the inputs array and normal transaction input to the mutate function.
    *
    * @param transaction The transaction to iterate normal transaction inputs.
    * @param mutate A function with two parameters. (1) transaction input index (2) normal transaction input
    */
  protected def forEachNormalTransaction(transaction : Transaction)(mutate : (Int, NormalTransactionInput) => Unit) : Unit = {
    var txIndex = 0
    for (txInput : TransactionInput <- transaction.inputs) {
      txInput match {
        case normalTxInput : NormalTransactionInput => {
          mutate(txIndex, normalTxInput)
        }
        case _ => {
          // nothing to do for the generation transaction.
        }
      }
      txIndex += 1
    }
  }

  /** Alter transaction inputs to calculate hash value used for signing/verifying a signature.
    *
    * @param transaction The transaction to alter.
    * @param transactionInputIndex See hashForSignature
    * @param scriptData See hashForSignature
    * @param howToHash See hashForSignature
    */
  protected def alter(transaction : Transaction, transactionInputIndex : Int, scriptData : Array[Byte], howToHash : Int) : Unit = {
    howToHash match {
      case TransactionSigHash.ALL => {
        // Set an empty unlocking script for all inputs
        forEachNormalTransaction(transaction) { (txIndex, normalTxInput) =>
          normalTxInput.unlockingScript =
            if (txIndex == transactionInputIndex)
              UnlockingScript(scriptData)
            else
              UnlockingScript(Array[Byte]())
        }
      }
      case TransactionSigHash.NONE => {
        throw new TransactionVerificationException(ErrorCode.UnsupportedHashType)
      }
      case TransactionSigHash.SINGLE => {
        throw new TransactionVerificationException(ErrorCode.UnsupportedHashType)
      }
    }
  }

  /** Calculate hash value of this transaction for signing/validating a signature.
    *
    * @param transaction The transaction to calculate hash for signature of it.
    * @param howToHash The hash type. A value of Transaction.SigHash.
    * @return The calcuated hash.
    */
  protected def calculateHash(transaction : Transaction, howToHash : Int) : Hash256 = {

    val bout = new ByteArrayOutputStream()
    val dout = new BlockDataOutputStream(bout)
    try {
      val serializer = new BlockSerializer(dout)
      // Step 1 : Serialize the transaction
      serializer.writeTransaction(transaction)
      // Step 2 : Write hash type
      Utils.uint32ToByteStreamLE(0x000000ff & howToHash, dout);
    } finally {
      dout.close()
    }
    // Step 3 : Calculate hash
    HashFunctions.hash256(bout.toByteArray)
  }
}
