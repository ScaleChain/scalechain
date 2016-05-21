package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.script.ops.OpPush
import io.scalechain.blockchain.script.{ScriptValue, ScriptSerializer, TransactionSignature}
import io.scalechain.blockchain.storage.BlockIndex
import io.scalechain.blockchain.{TransactionSignException, ErrorCode, UnsupportedFeature}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.transaction.SigHash.SigHash
import io.scalechain.crypto.ECKey.ECDSASignature
import io.scalechain.crypto.{ECKey, Hash256}
import io.scalechain.util.ByteArray


object SigHash extends Enumeration {
  type SigHash = Value
  val ALL                    = new Val(nextId, "ALL")
  val NONE                   = new Val(nextId, "NONE")
  val SINGLE                 = new Val(nextId, "SINGLE")
  // BUGBUG : Should we use bitwise OR??
  val ALL_OR_ANYONECANPAY    = new Val(nextId, "ALL|ANYONECANPAY")
  val NONE_OR_ANYONECANPAY   = new Val(nextId, "NONE|ANYONECANPAY")
  val SINGLE_OR_ANYONECANPAY = new Val(nextId, "SINGLE|ANYONECANPAY")
}

/**
  * Created by kangmo on 5/13/16.
  */
object TransactionSigner {

  /** The result of signing a transaction.
    *
    * @param transaction the transaction with signatures.
    * @param complete true if transaction is fully signed; false if more signatures are required
    */
  case class SignedTransaction(transaction : Transaction, complete : Boolean)

  /** Sign an input of a transaction with the list of given private keys.
    *
    * @param transaction The transaction to sign
    * @param inputIndex The input we want to sign. it is an index to transaction.inputs.
    * @param privateKeys An array holding private keys.
    * @param lockingScript The locking script that the given input unlocks. From the ouput pointed by the out point in the input.
    * @param blockIndex The block index required to verify transaction signature.
    * @return The signed input.
    */
  protected [transaction] def signInput(transaction: Transaction, inputIndex : Int, normalTxInput : NormalTransactionInput, privateKeys : List[PrivateKey], lockingScript : LockingScript, sigHash : SigHash, blockIndex : BlockIndex) : TransactionInput = {
    // We already checked if the number of private keys is 1. Multisig is not supported yet.
    assert(!privateKeys.isDefinedAt(1))
    // We already checked that we have at least one private key.
    assert(!privateKeys.isEmpty)

    val keyToUse = privateKeys.head

    val scriptData : Array[Byte] = TransactionSignature.getScriptForCheckSig(
      lockingScript.data,  // The locking script data.
      0, // The data to sign starts from offset 0
      Array() ) // We have no signatures to scrub from the locking script.

    val howToHash = sigHash match {
      case SigHash.ALL => 1
      case _ => {
        // We already checked that sigHash is SigHash.ALL. Currently it is the only supported hash type.
        assert (false)
        0
      }
    }

    val hashOfInput : Hash256 = TransactionSignature.calculateHash(transaction, inputIndex, scriptData, howToHash)

    val signature : ECDSASignature =  ECKey.doSign(hashOfInput.value, keyToUse.value)

    // Encode the signature to DER format, and append the hash type.
    val encodedSignature = signature.encodeToDER() ++ Array(howToHash.toByte)
    assert( encodedSignature.length <= 75 )

    val publickKey = PublicKey.from(keyToUse)
    val encodedPublicKey = publickKey.encode(compressed = true)
    assert( encodedPublicKey.length <= 75 )

    val unlockingScriptOps = List(
      OpPush(encodedSignature.length, ScriptValue.valueOf(encodedSignature)), // Signature.
      OpPush(encodedPublicKey.length, ScriptValue.valueOf(encodedPublicKey))  // Public Key.
    )

    val unlockingScriptWithSignature = UnlockingScript( ScriptSerializer.serialize(unlockingScriptOps) )

    normalTxInput.copy(
      unlockingScript = unlockingScriptWithSignature
    )
  }

  /** For an input, try to find matching private keys to sign, and sign the input.
    *
    * @param transaction The transaction to sign
    * @param inputIndex The input we want to sign. it is an index to transaction.inputs.
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @param blockIndex The block index required to verify transaction signature.
    * @return Some(transaction) if the given input was signed and the signature was verified. None otherwise.
    */
  protected[transaction] def tryToSignInput(transaction: Transaction, inputIndex : Int, privateKeys : List[PrivateKey], sigHash : SigHash, blockIndex : BlockIndex) : Option[Transaction] = {
    val inputToSign = transaction.inputs(inputIndex)

    inputToSign match {
      case normalTxInput : NormalTransactionInput => {
        val lockingScript : LockingScript = new NormalTransactionVerifier(normalTxInput, transaction, inputIndex).getLockingScript(blockIndex)
        val addresses : List[CoinAddress] = LockingScriptAnalyzer.extractAddresses(lockingScript)

        // TODO : Sign an input with multiple public key hashes.
        if (addresses.isDefinedAt(1)) {
          throw new TransactionSignException(ErrorCode.UnsupportedFeature, "Multisig is not supported yet. Input Index : " + inputIndex)
        }
        if (addresses.isEmpty) {
          throw new TransactionSignException(ErrorCode.UnsupportedFeature, "Unsupported locking script for the transaction input. Input Index : " + inputIndex)
        }

        val address = addresses.head
        val keysToUse = privateKeys.filter { key =>
          val publicKey = PublicKey.from(key)
          if ( ByteArray( publicKey.getHash().value ) == address.publicKeyHash ) { // If a public key hash matches, we can sign the transaction.
            true
          } else {
            false
          }
        }

        // Need to remove this assertion after implementing the multisig.
        assert(keysToUse.length <= 1)

        if (keysToUse.isEmpty) { // We have no private key to sign the input.
          // Get the signed input to create a new transaction that has the signed input instead of the original one.
          val signedInput : TransactionInput = signInput(transaction, inputIndex, normalTxInput, keysToUse, lockingScript, sigHash, blockIndex)
          val transactionWithSignedInput = transaction.copy(
            inputs = transaction.inputs.updated(inputIndex, signedInput)
          )

          new TransactionVerifier(transactionWithSignedInput).verifyInput(inputIndex, blockIndex)

          Some( transactionWithSignedInput )
        } else {
          None
        }
      }
      case tx => {
        throw new TransactionSignException( ErrorCode.InvalidTransactionInput, "An input to sign should be a normal transaction input, not a generation transaction input. Input Index : " + inputIndex)
      }
    }
  }

  /** Signs a transaction from the first input. This is a recursive function with the base case at the end of the inputs.
    *
    * @param transaction The transaction to sign
    * @param inputIndex The input we want to sign. it is an index to transaction.inputs.
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @param blockIndex The block index required to verify transaction signature.
    * @return The transaction with the newly signed input updated.
    */
  protected[transaction] def signInputsFrom(transaction : Transaction, inputIndex : Int, privateKeys : List[PrivateKey], sigHash : SigHash, blockIndex : BlockIndex) : Transaction = {

    // TODO : List.length is costly. Optimize it by passing an input itself by dropping one item at head for each invocation of this method.
    if (inputIndex >= transaction.inputs.length) { // The base case. We try to sign all inputs.
      transaction
    }

    // Do our best to sign an input
    val newTransaction = tryToSignInput(transaction, inputIndex, privateKeys, sigHash, blockIndex).getOrElse(transaction)

    // Recursively call the signInputsFrom by increasing the input index.
    signInputsFrom(newTransaction, inputIndex + 1, privateKeys, sigHash, blockIndex )
  }

  /** Merge signatures of the original transaction and newly signed transaction.
    *
    * @param beforeSigning The original transaction.
    * @param afterSigning The newly signed transaction.
    * @param blockIndex The block index required to verify transaction signature.
    * @return The transaction with merged inputs.
    */
  protected[transaction] def mergeSignatures(beforeSigning : Transaction, afterSigning : Transaction, blockIndex : BlockIndex): SignedTransaction = {
    assert(beforeSigning.inputs.length == afterSigning.inputs.length)

    var allInputsSigned = true

    // Preserve signatures in the original transaction if any.
    val mergedInputs = (beforeSigning.inputs zip afterSigning.inputs).map {
      case (orgInput : NormalTransactionInput, newInput : NormalTransactionInput) => {
        if ( orgInput.unlockingScript.data.length > 0 ) {
          orgInput
        } else {
          if (newInput.unlockingScript.data.length == 0) {
            allInputsSigned = false
          }
          newInput
        }
      }

      case _ => {
        // We already checked if all inputs are normal transaction inputs.
        assert(false)
        null
      }
    }

    // Get the final transaction by copying the new merged inputs.
    val finalTransaction = beforeSigning.copy(inputs = mergedInputs)

    // Make sure that the transaction verification passes if all inputs are signed.
    if (allInputsSigned) {
      new TransactionVerifier(finalTransaction).verify(blockIndex)
    }

    SignedTransaction(
      finalTransaction,
      allInputsSigned
    )
  }

  /** Sign a transaction.
    *
    * @param transaction The transaction to sign.
    * @param blockIndex The block index required to verify transaction signature.
    * @param dependencies  Unspent transaction output details. The previous outputs being spent by this transaction.
    * @param privateKeys An array holding private keys.
    * @param sigHash The type of signature hash to use for all of the signatures performed.
    * @return true if all inputs are signed, false otherwise.
    */
  def sign(transaction   : Transaction,
           blockIndex    : BlockIndex,
           dependencies  : List[UnspentTransactionOutput],
           privateKeys   : List[PrivateKey],
           sigHash       : SigHash
          ) : SignedTransaction = {
    // Only ALL SigHash type is supported for now.
    if (sigHash != SigHash.ALL) {
      throw new UnsupportedFeature(ErrorCode.UnsupportedHashType)
    }
    // dependencies parameter is not supported yet.
    if ( ! dependencies.isEmpty ) {
      throw new UnsupportedFeature(ErrorCode.UnsupportedFeature)
    }

    // TODO : Make the error code compatible with Bitcoin.
    if (transaction.inputs.head.isCoinBaseInput()) {
      throw new TransactionSignException( ErrorCode.UnableToSignCoinbaseTransaction )
    }

    val signedTranasction = signInputsFrom(transaction, inputIndex = 0, privateKeys, sigHash, blockIndex)

    val signedResult : SignedTransaction = mergeSignatures(transaction, signedTranasction, blockIndex)
    signedResult
  }
}
