package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.script.ops.OpPush
import io.scalechain.blockchain.script.ScriptValue
import io.scalechain.blockchain.script.ScriptSerializer
import io.scalechain.blockchain.script.TransactionSignature
import io.scalechain.blockchain.storage.BlockIndex
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.TransactionSignException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.UnsupportedFeature
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.transaction.SigHash
import io.scalechain.crypto.ECKey.ECDSASignature
import io.scalechain.crypto.ECKey
import io.scalechain.crypto.Hash256
import io.scalechain.util.ByteArrayExt
import io.scalechain.util.HexUtil

import scala.annotation.tailrec
import java.util.*

enum class SigHash  {
  ALL, NONE, SINGLE, ALL_OR_ANYONECANPAY, NONE_OR_ANYONECANPAY, SINGLE_OR_ANYONECANPAY
}

/** The result of signing a transaction.
  *
  * @param transaction the transaction with signatures.
  * @param complete true if transaction is fully signed; false if more signatures are required
  */
data class SignedTransaction(val transaction : Transaction, val complete : Boolean)

/**
  * Created by kangmo on 5/13/16.
  */
class TransactionSigner(private val db : KeyValueDatabase) {


  /** Sign an input of a transaction with the list of given private keys.
    *
    * @param transaction The transaction to sign
    * @param inputIndex The input we want to sign. it is an index to transaction.inputs.
    * @param privateKeys An array holding private keys.
    * @param lockingScript The locking script that the given input unlocks. From the ouput pointed by the out point in the input.
    * @param chainView A blockchain view that can get the transaction output pointed by an out point.
    * @return The signed input.
    */
  protected fun signInput(transaction: Transaction, inputIndex : Int, normalTxInput : NormalTransactionInput, privateKeys : List<PrivateKey>, lockingScript : LockingScript, sigHash : SigHash, chainView : BlockchainView) : TransactionInput {
    // We already checked if the number of private keys is 1. Multisig is not supported yet.
    assert(privateKeys.size == 1)

    val keyToUse = privateKeys.first()

    val scriptData : ByteArray = TransactionSignature.getScriptForCheckSig(
      lockingScript.data,  // The locking script data.
      0, // The data to sign starts from offset 0
      arrayOf() ) // We have no signatures to scrub from the locking script.

    val howToHash =
        if (sigHash == SigHash.ALL) {
          1
        } else {
          // We already checked that sigHash is SigHash.ALL. Currently it is the only supported hash type.
          assert (false)
          0
        }

    val hashOfInput : Hash256 = TransactionSignature.calculateHash(transaction, inputIndex, scriptData, howToHash)

    val signature : ECDSASignature =  ECKey.doSign(hashOfInput.value, keyToUse.value)

    // Encode the signature to DER format, and append the hash type.
    val encodedSignature = signature.encodeToDER() + ByteArrayExt.from(howToHash.toByte())

    val publickKey = PublicKey.from(keyToUse)
    // If we use compressed version, the transaction verification fails. Need investigation.
    val encodedPublicKey = publickKey.encode()

    val unlockingScriptOps = listOf(
      OpPush.from(encodedSignature), // Signature.
      OpPush.from(encodedPublicKey)  // Public Key.
    )

    val unlockingScriptWithSignature = UnlockingScript( ScriptSerializer.serialize(unlockingScriptOps) )

    return normalTxInput.copy(
      unlockingScript = unlockingScriptWithSignature
    )
  }

  /** For an input, try to find matching private keys to sign, and sign the input.
    *
    * @param transaction The transaction to sign
    * @param inputIndex The input we want to sign. it is an index to transaction.inputs.
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @param chainView A blockchain view that can get the transaction output pointed by an out point.
    * @return Some(transaction) if the given input was signed and the signature was verified. None otherwise.
    */
  protected fun tryToSignInput(transaction: Transaction, inputIndex : Int, privateKeys : List<PrivateKey>, sigHash : SigHash, chainView : BlockchainView) : Transaction? {
    val inputToSign = transaction.inputs[inputIndex]

    when {
      inputToSign is NormalTransactionInput -> {
        val lockingScript : LockingScript = NormalTransactionVerifier(db, inputToSign, transaction, inputIndex).getLockingScript(chainView)
        val addresses : List<CoinAddress> = LockingScriptAnalyzer.extractAddresses(lockingScript)

        // TODO : Sign an input with multiple public key hashes.
        if (addresses.size > 1) {
          throw TransactionSignException(ErrorCode.UnsupportedFeature, "Multisig is not supported yet. Input Index : " + inputIndex)
        }
        if (addresses.isEmpty()) {
          throw TransactionSignException(ErrorCode.UnsupportedFeature, "Unsupported locking script for the transaction input. Input Index : " + inputIndex)
        }

        val address = addresses.first()

        val keysToUse = privateKeys.filter { key ->
          val publicKey = PublicKey.from(key)

          if ( Arrays.equals( publicKey.getHash().value, address.publicKeyHash) ) { // If a public key hash matches, we can sign the transaction.
            true
          } else {
            false
          }
        }

        // Need to remove this assertion after implementing the multisig.
        assert(keysToUse.size <= 1)

        if (keysToUse.isEmpty()) { // We don't have the private key to sign the input.
          return null
        } else {
          // Get the signed input to create a transaction that has the signed input instead of the original one.
          val signedInput : TransactionInput = signInput(transaction, inputIndex, inputToSign, keysToUse, lockingScript, sigHash, chainView)
          val transactionWithSignedInput = transaction.copy(
              inputs = transaction.inputs.mapIndexed { i, transactionInput ->
                if (i == inputIndex)
                  signedInput
                else
                  transactionInput
              }
          )

          // TODO : Uncomment.
          //TransactionVerifier(transactionWithSignedInput).verifyInput(inputIndex, blockIndex)

          return transactionWithSignedInput
        }
      }
      else -> {
        throw TransactionSignException( ErrorCode.InvalidTransactionInput, "An input to sign should be a normal transaction input, not a generation transaction input. Input Index : " + inputIndex)
      }
    }
  }

  /** Signs a transaction from the first input. This is a recursive function with the base case at the end of the inputs.
    *
    * @param transaction The transaction to sign
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @param chainView A blockchain view that can get the transaction output pointed by an out point.
    * @return The transaction with the newly signed input updated.
    */
  tailrec
  protected fun signInputsFrom(transaction : Transaction, inputIndex : Int, privateKeys : List<PrivateKey>, sigHash : SigHash, chainView : BlockchainView) : Transaction {

    // TODO : List.length is costly. Optimize it by passing an input itself by dropping one item at head for each invocation of this method.
    if (inputIndex >= transaction.inputs.size) { // The base case. We try to sign all inputs.
      return transaction
    } else {
      // Do our best to sign an input
      val newTransaction = tryToSignInput(transaction, inputIndex, privateKeys, sigHash, chainView) ?: transaction

      // Recursively call the signInputsFrom by increasing the input index.
      return signInputsFrom(newTransaction, inputIndex + 1, privateKeys, sigHash, chainView )
    }
  }

  /** Merge signatures of the original transaction and newly signed transaction.
    *
    * @param beforeSigning The original transaction.
    * @param afterSigning The newly signed transaction.
    * @param chainView A blockchain view that can get the transaction output pointed by an out point.
    * @return The transaction with merged inputs.
    */
  protected fun mergeSignatures(beforeSigning : Transaction, afterSigning : Transaction, chainView : BlockchainView): SignedTransaction {
    assert(beforeSigning.inputs.size == afterSigning.inputs.size)

    var allInputsSigned = true

    // Preserve signatures in the original transaction if any.
    val mergedInputs = (beforeSigning.inputs zip afterSigning.inputs).map { pair ->
      val orgInput = pair.first
      val newInput = pair.second

      if (orgInput is NormalTransactionInput && newInput is NormalTransactionInput) {
        if ( orgInput.unlockingScript.data.isNotEmpty() ) {
          orgInput
        } else {
          if (newInput.unlockingScript.data.isEmpty()) {
            allInputsSigned = false
          }
          newInput
        }
      } else {
        // We already checked if all inputs are normal transaction inputs.
        throw AssertionError()
      }
    }

    // Get the final transaction by copying the merged inputs.
    val finalTransaction = beforeSigning.copy(inputs = mergedInputs)

    // Make sure that the transaction verification passes if all inputs are signed.
    if (allInputsSigned) {
      TransactionVerifier(db, finalTransaction).verify(chainView)
    }

    return SignedTransaction(
      finalTransaction,
      allInputsSigned
    )
  }

  /** Sign a transaction.
    *
    * @param transaction The transaction to sign.
    * @param chainView A blockchain view that can get the transaction output pointed by an out point.
    * @param dependencies  Unspent transaction output details. The previous outputs being spent by this transaction.
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @return true if all inputs are signed, false otherwise.
    */
  fun sign(transaction   : Transaction,
           chainView     : BlockchainView,
           dependencies  : List<UnspentTransactionOutput>,
           privateKeys   : List<PrivateKey>,
           sigHash       : SigHash
          ) : SignedTransaction {
    // Only ALL SigHash type is supported for now.
    if (sigHash != SigHash.ALL) {
      throw UnsupportedFeature(ErrorCode.UnsupportedHashType)
    }
    // dependencies parameter is not supported yet.
    if ( ! dependencies.isEmpty() ) {
      throw UnsupportedFeature(ErrorCode.UnsupportedFeature)
    }

    // TODO : Make the error code compatible with Bitcoin.
    if (transaction.inputs.first().isCoinBaseInput()) {
      throw TransactionSignException( ErrorCode.UnableToSignCoinbaseTransaction )
    }

    val signedTranasction = signInputsFrom(transaction, 0/*input index*/, privateKeys, sigHash, chainView)

    val signedResult : SignedTransaction = mergeSignatures(transaction, signedTranasction, chainView)
    return signedResult
  }
}
