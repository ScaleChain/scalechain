package io.scalechain.wallet

import io.scalechain.blockchain.proto.codec.*
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.WalletException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.transaction.*
import kotlin.io.use

/////////////////////////////////////////////////////////////////////////////////////////////////
// Account -> Output Ownerships
/////////////////////////////////////////////////////////////////////////////////////////////////
// Keys and Values (K, V) :
// A. (Account + '\0' + OutputOwnership, Dummy) => for Search 2, 3
// B. (OutputOwnership, OwnershipDescriptor) => for Search case 1
// C. (Account, OutputOwnership) => For keeping the receiving address. For Modification 3 and Search 4
//
// OwnershipDescriptor has the following fields.
// 1. Account     : String
// 1. privateKeys : List<PrivateKey>
//
// Modifications :
// 1. Add an output ownership to an account. Create an account if it does not exist.
// 2. Put the private key into an address.
// 3. Mark an address of an account as the receiving address.
//
// Searches :
// 1. Get an account by an address.
// 2. Iterate for each output ownerships for all accounts.
// 3. Iterate private keys for all accounts.
// 4. Get the receiving address of an account.
// 5. Get a private key for an address.


/////////////////////////////////////////////////////////////////////////////////////////////////
// Output Ownership -> Transactions
/////////////////////////////////////////////////////////////////////////////////////////////////
// Keys and Values (K, V) :
// A. ( OutputOwnership + '\0' + (transaction)Hash ) => For Search 1,2
//
// Modifications :
// 1. Put a transaction into the output ownership.
// 2. Remove a transaction from the output ownership by transaction hash.
//
// Searches :
// 1. Iterate transactions by providing an account and the skip count. Include watch-only ownerships.
// 2. Iterate transactions by providing an account and the skip count. Exclude watch-only ownerships.


/////////////////////////////////////////////////////////////////////////////////////////////////
// Output Ownership -> UTXOs
/////////////////////////////////////////////////////////////////////////////////////////////////
// Keys and Values (K, V) :
// A. ( OutputOwnership + '\0' + OutPoint, None ) => For Search 1, 2, 3
// B. ( OutPoint, WalletOutput ) => For Modification 2
//
// WalletOutput has the following fields :
// 1. spent : Boolean
// 1. transactionOutput : TransactionOutput
//
// Modifications :
// 1. Put a UTXO into the output ownership.
// 2. Mark a UTXO spent searching by OutPoint.
// 3. Remove a UTXO from the output ownership.
//
// Searches :
// 1. Iterate UTXOs for an output ownership.
// 2. Iterate UTXOs for all output ownerships. Filter UTXOs based on confirmations.
// 3. Iterate UTXOs for a given addresses. Filter UTXOs based on confirmations.


/////////////////////////////////////////////////////////////////////////////////////////////////
// (transaction)Hash -> Transaction
/////////////////////////////////////////////////////////////////////////////////////////////////
// Keys and Values (K, V) :
// A. ((transaction)Hash, WalletTransaction)
//
// Modifications :
// 1. Add a transaction.
// 2. Remove a transaction.
//
// Searches :
// 1. Search a transaction by the hash.


/** A storage for the wallet.
  *
  *   The wallet store stores transactions and unspent outputs for a given output ownership.
  *   An example an output ownership is coin address. A coin address owns an output.
  *   Also a public key script can be an output ownership.
  *
  *   The wallet store also stores a list of accounts. Each account has a list of output ownership.
  *
  *   To summarize,
  *   1. An account has multiple output ownerships.
  *   2. An output ownership has multiple transactions(either receiving UTXOs or spending UTXOs).
  *   3. An output ownership has multiple unspent outputs.
  *
  *   We need to keep track of the statuses of outputs depending on whether it was spent or not.
  *
  * Why not have this class in the storage layer?
  *   The storage layer keeps data for maintaining blockchain itself.
  *   We plan to have different implementations of the storage layer such as
  *     (1) keeping all blocks in each peer.  Ex> keep all blocks for N peers.
  *     (2) keeping some blocks in each peer. Ex> keep 1/N blocks for N peers.
  */
class WalletStore {

  /*******************************************************************************************************
   * Category : <Account -> Output Ownerships>
   *******************************************************************************************************/

  /** Add an output ownership to an account. Create an account if it does not exist.
    *
    * Category : <Account -> Output Ownerships> - Modification
    *
    * Used by : RPCs adding a address to an account.
    *   1. importaddress RPC.
    *   2. getnewaddress RPC.
    *
    * @param accountName The name of the account to create.
    * @param outputOwnership The address or public key script to add to the account.
    */
  fun putOutputOwnership(db : KeyValueDatabase, accountName : String, outputOwnership : OutputOwnership ) : Unit {
    // We don't need a value mapped here. Just use one byte 0 for the value.
    // As we are iterating output ownerships for an account by using key prefix, we don't need any value here.
    db.putPrefixedObject(OutputOwnershipCodec, OneByteCodec, OWNERSHIPS, accountName, outputOwnership, OneByte(0))
    db.putObject(OutputOwnershipCodec, OwnershipDescriptorCodec, OWNERSHIP_DESC, outputOwnership, OwnershipDescriptor(accountName, privateKeys = listOf()))
  }


  /** Delete an output ownership.
    *
    * This method is not used by Wallet yet, but is likely to be used in the future.
    *
    * @param accountName The name of the account to create.
    * @param outputOwnership The address or public key script to add to the account.
    */
  fun delOutputOwnership(db : KeyValueDatabase, accountName : String, outputOwnership : OutputOwnership ) : Unit {
    // We don't need a value mapped here. Just use one byte 0 for the value.
    // As we are iterating output ownerships for an account by using key prefix, we don't need any value here.
    db.delPrefixedObject(OutputOwnershipCodec, OWNERSHIPS, accountName, outputOwnership)
    db.delObject(OutputOwnershipCodec, OWNERSHIP_DESC, outputOwnership)
  }
  /** Put the receiving address into an account.
    *
    * Category : <Account -> Output Ownerships> - Modification
    */

  /** Mark an address of an account as the receiving address.
    *
    * Category : <Account -> Output Ownerships> - Modification
    *
    * @throws WalletException(ErrorCode.AddressNotFound) if the address was not found.
    */
  fun putReceivingAddress(db : KeyValueDatabase, accountName : String, outputOwnership : OutputOwnership ) : Unit {
    db.putObject(AccountCodec, OutputOwnershipCodec, RECEIVING, Account(accountName), outputOwnership)
  }

  /** Find an account by coin address.
    *
    * Used by : getaccount RPC.
    *
    * Category : <Account -> Output Ownerships> - Search
    *
    * @param ownership The output ownership, which is attached to the account.
    * @return The found account.
    */
  fun getAccount(db : KeyValueDatabase, ownership : OutputOwnership) : String? {
    val account : OwnershipDescriptor? = db.getObject(OutputOwnershipCodec, OwnershipDescriptorCodec, OWNERSHIP_DESC, ownership)
    return account?.account
  }

  /** Get an iterator for each output ownerships for all accounts.
    *
    * Category : <Account -> Output Ownerships> - Search
    *
    * @param accountOption Some(account) to get ownerships for an account. None to get all ownerships for all accounts.
    */
  fun getOutputOwnerships(db : KeyValueDatabase, accountOption : String?) : List<OutputOwnership>{
    return (
      if (accountOption == null) {
        db.seekPrefixedObject(OutputOwnershipCodec, OneByteCodec, OWNERSHIPS)
      } else {
        db.seekPrefixedObject(OutputOwnershipCodec, OneByteCodec, OWNERSHIPS, accountOption)
      }
      // seekPrefixedObject returns (key, value) pairs, whereas we need the value only. map the pair to the value(2nd).
    ).use { iterator ->
      iterator.asSequence().map{ pair ->
        //case (CStringPrefixed(_, ownership : OutputOwnership), _) => ownership
        val cstringPrefixedKey = pair.first
        val ownership = cstringPrefixedKey.data
        ownership
      }.toList()
    }
  }

  /** Get an iterator private keys for an address or all accounts.
    *
    * TODO : Instead of getting a CoinAddress, how about getting an OutputOwnership?
    *
    * Category : <Account -> Output Ownerships> - Search
    *
    * @param addressOption Some(address) to get private keys for an address. A Multisig address may have multiple keys for it.
    *                      None to get private keys for all accounts.
    */
  fun getPrivateKeys(db : KeyValueDatabase, addressOption : OutputOwnership?) : List<PrivateKey> {
    if (addressOption == null) {
      return getOutputOwnerships(db, null).flatMap{ outputOwnership : OutputOwnership ->
        getPrivateKeys(db, outputOwnership)
      }
    } else {
      val ownershipDescriptorOption : OwnershipDescriptor? =
        db.getObject(OutputOwnershipCodec, OwnershipDescriptorCodec, OWNERSHIP_DESC, addressOption)
      // Get PrivateKey object from base58 encoded private key string.
      val privateKeyListOption = ownershipDescriptorOption?.privateKeys?.map { PrivateKey.from(it) }
      // Get rid of the Option wrapper.
      return privateKeyListOption ?: listOf()
    }
  }

  /** Get the receiving address of an account.
    *
    * Category : <Account -> Output Ownerships> - Search
    */
  fun getReceivingAddress( db : KeyValueDatabase, account:String) : OutputOwnership? {
    return db.getObject(AccountCodec, OutputOwnershipCodec, RECEIVING, Account(account))
  }


  /** Put private keys for a coin address.
    *
    * After putting the private keys, we can sign transaction inputs
    * which are pointing to an output whose locking script has public key hashes,
    * which matches the private keys.
    *
    * We need to be able to put multiple private keys for multisig addresses.
    *
    * @param ownership The output ownership(ex>address) generated from the private key.
    * @param privateKeys The private key to put under the coin address.
    * @throws WalletException(ErrorCode.AddressNotFound) if the address was not found.
    */
  fun putPrivateKeys(db : KeyValueDatabase, ownership : OutputOwnership, privateKeys : List<PrivateKey>) : Unit {
    val ownershipDescriptor : OwnershipDescriptor? =
      db.getObject(OutputOwnershipCodec, OwnershipDescriptorCodec, OWNERSHIP_DESC, ownership)
    if (ownershipDescriptor == null) {
      throw WalletException(ErrorCode.OwnershipNotFound)
    } else {
      db.putObject(OutputOwnershipCodec, OwnershipDescriptorCodec, OWNERSHIP_DESC, ownership,
        ownershipDescriptor.copy(privateKeys = privateKeys.map{ it.base58() } )
      )
    }
  }

  /** Check if an output exists.
    *
    * @param outputOwnership The output ownership to check.
    * @return true if the ownership exists; false otherwise.
    */
  fun ownershipExists(db : KeyValueDatabase, outputOwnership : OutputOwnership) : Boolean {
    val ownershipOption = db.getObject(OutputOwnershipCodec, OwnershipDescriptorCodec, OWNERSHIP_DESC, outputOwnership)
    return ownershipOption != null
  }
  /*******************************************************************************************************
   * Category : <Output Ownership -> TransactionHashes>
   *******************************************************************************************************/

  /** Put a transaction into the output ownership.
    *
    * Category : <Output Ownership -> Transactions> - Modification
    *
    * @throws WalletException(ErrorCode.OwnershipNotFound) if the output ownership was not found.
    */
  fun putTransactionHash(db : KeyValueDatabase, outputOwnership : OutputOwnership, transactionHash : Hash) : Unit {
    if (!ownershipExists(db, outputOwnership)) {
      throw WalletException(ErrorCode.OwnershipNotFound)
    }

    db.putPrefixedObject(HashCodec, OneByteCodec, TXHASHES, outputOwnership.stringKey(), transactionHash, OneByte(0))
  }

  /** Remove a transaction from the output ownership by transaction hash.
    *
    * Category : <Output Ownership -> Transactions> - Modification
    */
  fun delTransactionHash(db : KeyValueDatabase, outputOwnership : OutputOwnership, transactionHash : Hash) : Unit {
    db.delPrefixedObject(HashCodec, TXHASHES, outputOwnership.stringKey(), transactionHash)
  }

  /** Get an iterator of transaction hashes searched by an optional account.
    *
    * Category : <Output Ownership -> Transactions> - Search
    *
    * @param outputOwnershipOption Some(ownership) to get transactions hashes related to an output ownership
    *                              None to get all transaction hashes for all output ownerships.
    */
  fun getTransactionHashes(db : KeyValueDatabase, outputOwnershipOption : OutputOwnership?) : List<Hash> {
    return (
      if (outputOwnershipOption == null) {
        // seekPrefixedObject returns (key, value) pairs, whereas we need the value only. map the pair to the value(2nd).
        db.seekPrefixedObject(HashCodec, OneByteCodec, TXHASHES)
      } else {
        db.seekPrefixedObject(HashCodec, OneByteCodec, TXHASHES, outputOwnershipOption.stringKey())
      }
    ).use { iterator ->
      iterator.asSequence().map{ pair ->
        //case (CStringPrefixed(_, hash : Hash), _) => hash
        val cstringPrefixedKey = pair.first
        val hash = cstringPrefixedKey.data
        hash
      }.toList()
    }
  }

  /*******************************************************************************************************
   * Category : Category : <Output Ownership -> OutPoint(UTXOs)>
   *******************************************************************************************************/
  /** Put a UTXO into the output ownership.
    *
    * Category : <Output Ownership -> UTXOs> - Modification
    *
    * @throws WalletException(ErrorCode.OwnershipNotFound) if the output ownership was not found.
    */
  fun putTransactionOutPoint(db : KeyValueDatabase, outputOwnership: OutputOwnership, output : OutPoint) : Unit {
    if (!ownershipExists(db, outputOwnership)) {
      throw WalletException(ErrorCode.OwnershipNotFound)
    }

    db.putPrefixedObject(OutPointCodec, OneByteCodec, OUTPOINTS, outputOwnership.stringKey(), output, OneByte(0))
  }

  /** Remove a UTXO from the output ownership.
    *
    * Category : <Output Ownership -> UTXOs> - Modification
    */
  fun delTransactionOutPoint(db : KeyValueDatabase, outputOwnership: OutputOwnership, output : OutPoint) : Unit {
    db.delPrefixedObject(OutPointCodec, OUTPOINTS, outputOwnership.stringKey(), output)
  }


  /** Get an iterator for transaction outpoints
    *
    * @param outputOwnershipOption Some(ownership) to iterate UTXOs for a specific output ownership.
    *                              None to iterate UTXOs for all output ownership.
    * @return The iterator for outpoints.
    */
  fun getTransactionOutPoints(db : KeyValueDatabase, outputOwnershipOption : OutputOwnership?) : List<OutPoint> {
    return (
      if (outputOwnershipOption == null) {
        // seekPrefixedObject returns (key, value) pairs, whereas we need the value only. map the pair to the value(2nd).
        db.seekPrefixedObject(OutPointCodec, OneByteCodec, OUTPOINTS)
      } else {
        db.seekPrefixedObject(OutPointCodec, OneByteCodec, OUTPOINTS, outputOwnershipOption.stringKey())
      }
    ).use {
      it.asSequence().map { pair ->
        // case (CStringPrefixed(_, outPoint : OutPoint), _) => outPoint
        val cstringPrefixedKey = pair.first
        val outPoint = cstringPrefixedKey.data
        outPoint
      }.toList()
    }
  }

  /*******************************************************************************************************
   * Category : <(transaction)Hash -> Transaction>
   *******************************************************************************************************/

  /** Add a transaction.
    *
    * Category : <(transaction)Hash -> Transaction> - Modification
    *
    */
  fun putWalletTransaction(db : KeyValueDatabase, transactionHash : Hash, transaction : WalletTransaction) : Unit {
    db.putObject(HashCodec, WalletTransactionCodec, WALLETTX, transactionHash, transaction)
  }

  /** Remove a transaction.
    *
    * Category : <(transaction)Hash -> Transaction> - Modification
    */
  fun delWalletTransaction(db : KeyValueDatabase, transactionHash : Hash) : Unit {
    db.delObject(HashCodec, WALLETTX, transactionHash)
  }

  /** Search a transaction by the hash.
    *
    * Category : <(transaction)Hash -> Transaction> - Search
    */
  fun getWalletTransaction(db : KeyValueDatabase, transactionHash : Hash) : WalletTransaction? {
    return db.getObject(HashCodec, WalletTransactionCodec, WALLETTX, transactionHash)
  }


  /*******************************************************************************************************
   * Category : <OutPoint -> TransactionOutput>
   *******************************************************************************************************/

  /** Add a transaction output.
    *
    * Category : <OutPoint -> TransactionOutput> - Modifications
    */
  fun putWalletOutput(db : KeyValueDatabase, outPoint : OutPoint, walletOutput : WalletOutput) : Unit {
    db.putObject(OutPointCodec, WalletOutputCodec, WALLETOUTPUT, outPoint, walletOutput)
  }

  /** Remove a transaction output.
    *
    * Category : <OutPoint -> TransactionOutput> - Modifications
    */
  fun delWalletOutput(db : KeyValueDatabase, outPoint : OutPoint) : Unit {
    db.delObject(OutPointCodec, WALLETOUTPUT, outPoint)
  }

  /** Search a transaction output by the outpoint.
    *
    * Category : <OutPoint -> TransactionOutput> - Search
    */
  fun getWalletOutput(db : KeyValueDatabase, outPoint : OutPoint) : WalletOutput? {
    return db.getObject(OutPointCodec, WalletOutputCodec, WALLETOUTPUT, outPoint)
  }

  /** Mark a UTXO spent searching by OutPoint.
    *
    * Category : <Output Ownership -> UTXOs> - Modification
    *
    * @return true if the output was found in the wallet; false otherwise.
    */
  fun markWalletOutputSpent(db : KeyValueDatabase, outPoint : OutPoint, spent : Boolean) : Boolean {
    val outPointOption : WalletOutput? = db.getObject(OutPointCodec, WalletOutputCodec, WALLETOUTPUT, outPoint)
    if (outPointOption == null) {
      return false
    } else {
      db.putObject(OutPointCodec, WalletOutputCodec, WALLETOUTPUT, outPoint,
        outPointOption.copy(
          spent = spent
        )
      )
      return true
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // OutPoint -> WalletOutput
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Keys and Values (K, V) :
  // A. (OutPoint, WalletOutput)
  //
  // Modifications :
  // 1. Add a transaction output.
  // 2. Remove a transaction output.
  //
  // Searches :
  // 1. Search a transaction output by the outpoint.
  companion object {
    // Naming convention rules
    // 1. Name the prefix with the name of data we store.
    //    Ex> OWNERSHIPS stores onerships for an account.
    // 2. Use plural if we keep multiple entities under an entity.
    //    Ex> OWNERSHIPS, TXHASHES, OUTPOINTS


    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Account -> Output Ownerships
    /////////////////////////////////////////////////////////////////////////////////////////////////

    // A. (Account + '\0' + OutputOwnership, Dummy)
    val OWNERSHIPS = 'O'.toByte()

    // B. (OutputOwnership, OwnershipDescriptor)
    val OWNERSHIP_DESC = 'D'.toByte()

    // C. (Account, OutputOwnership)
    val RECEIVING = 'R'.toByte()

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Output Ownership -> Transactions
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // A. ( OutputOwnership + '\0' + (transaction)Hash )
    val TXHASHES = 'H'.toByte()

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Output Ownership -> UTXOs
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // A. ( OutputOwnership + '\0' + OutPoint, None )
    val OUTPOINTS = 'P'.toByte()

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // (transaction)Hash -> Transaction
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Keys and Values (K, V) :
    // A. ((transaction)Hash, WalletTransaction)
    val WALLETTX = 'T'.toByte()

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // OutPoint -> WalletOutput
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Keys and Values (K, V) :
    // A. (OutPoint, WalletOutput)
    val WALLETOUTPUT = 'U'.toByte()
  }

}
