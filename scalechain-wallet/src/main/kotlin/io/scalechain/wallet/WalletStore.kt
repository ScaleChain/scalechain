package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixed
import io.scalechain.blockchain.storage.index.TransactingRocksDatabase
import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.WalletException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.transaction.*
import io.scalechain.util.Using.*

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
  import WalletStore.PREFIXES._

  implicit val accountCodec = AccountCodec
  implicit val outputOwnershipCodec = OutputOwnershipCodec
  implicit val outByteCodec = OneByteCodec
  implicit val outPointCodec = OutPointCodec
  implicit val walletOutputCodec = WalletOutputCodec
  implicit val walletTransactionCodec = WalletTransactionCodec
  implicit val ownershipDescriptorCodec = OwnershipDescriptorCodec
  implicit val hashCodec = HashCodec

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
  fun putOutputOwnership(accountName : String, outputOwnership : OutputOwnership )(implicit db : KeyValueDatabase) : Unit {
    // We don't need a value mapped here. Just use one byte 0 for the value.
    // As we are iterating output ownerships for an account by using key prefix, we don't need any value here.
    db.putPrefixedObject(OWNERSHIPS, accountName, outputOwnership, OneByte(0))
    db.putObject(OWNERSHIP_DESC, outputOwnership, OwnershipDescriptor(accountName, privateKeys = List()))
  }


  /** Delete an output ownership.
    *
    * This method is not used by Wallet yet, but is likely to be used in the future.
    *
    * @param accountName The name of the account to create.
    * @param outputOwnership The address or public key script to add to the account.
    */
  fun delOutputOwnership(accountName : String, outputOwnership : OutputOwnership )(implicit db : KeyValueDatabase) : Unit {
    // We don't need a value mapped here. Just use one byte 0 for the value.
    // As we are iterating output ownerships for an account by using key prefix, we don't need any value here.
    db.delPrefixedObject(OWNERSHIPS, accountName, outputOwnership)
    db.delObject(OWNERSHIP_DESC, outputOwnership)
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
  fun putReceivingAddress(accountName : String, outputOwnership : OutputOwnership )(implicit db : KeyValueDatabase) : Unit {
    db.putObject(RECEIVING, Account(accountName), outputOwnership)
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
  fun getAccount(ownership : OutputOwnership)(implicit db : KeyValueDatabase) : Option<String> {
    val account : Option<OwnershipDescriptor> = db.getObject(OWNERSHIP_DESC, ownership)(OutputOwnershipCodec, OwnershipDescriptorCodec)
    account.map(_.account)
  }

  /** Get an iterator for each output ownerships for all accounts.
    *
    * Category : <Account -> Output Ownerships> - Search
    *
    * @param accountOption Some(account) to get ownerships for an account. None to get all ownerships for all accounts.
    */
  fun getOutputOwnerships(accountOption : Option<String>)(implicit db : KeyValueDatabase) : List<OutputOwnership>{
    (
      if (accountOption.isEmpty) {
        using ( db.seekPrefixedObject(OWNERSHIPS)(OutputOwnershipCodec, OneByteCodec) ) in { _.toList }

      } else {
        using( db.seekPrefixedObject(OWNERSHIPS, accountOption.get)(OutputOwnershipCodec, OneByteCodec) ) in (_.toList)
      }
      // seekPrefixedObject returns (key, value) pairs, whereas we need the value only. map the pair to the value(2nd).
    ).map{ case (CStringPrefixed(_, ownership : OutputOwnership), _) => ownership }
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
  fun getPrivateKeys(addressOption : Option<OutputOwnership>)(implicit db : KeyValueDatabase) : List<PrivateKey> {
    if (addressOption.isEmpty) {
      getOutputOwnerships(None).flatMap{ outputOwnership : OutputOwnership =>
        getPrivateKeys(Some(outputOwnership))
      }
    } else {
      val ownershipDescriptorOption:Option<OwnershipDescriptor> =
        db.getObject(OWNERSHIP_DESC, addressOption.get)(OutputOwnershipCodec, OwnershipDescriptorCodec)
      // Get PrivateKey object from base58 encoded private key string.
      val privateKeyListOption = ownershipDescriptorOption.map(_.privateKeys.map(PrivateKey.from(_)))
      // Get rid of the Option wrapper.
      privateKeyListOption.getOrElse(List())
    }
  }

  /** Get the receiving address of an account.
    *
    * Category : <Account -> Output Ownerships> - Search
    */
  fun getReceivingAddress(account:String)(implicit db : KeyValueDatabase) : Option<OutputOwnership> {
    db.getObject(RECEIVING, Account(account))(AccountCodec, OutputOwnershipCodec)
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
  fun putPrivateKeys(ownership : OutputOwnership, privateKeys : List<PrivateKey>)(implicit db : KeyValueDatabase) : Unit {
    val ownershipDescriptor : Option<OwnershipDescriptor> =
      db.getObject(OWNERSHIP_DESC, ownership)(OutputOwnershipCodec, OwnershipDescriptorCodec)
    if (ownershipDescriptor.isEmpty) {
      throw WalletException(ErrorCode.OwnershipNotFound)
    } else {
      db.putObject(OWNERSHIP_DESC, ownership,
        ownershipDescriptor.get.copy(privateKeys = privateKeys.map(_.base58()))
      )
    }
  }

  /** Check if an output exists.
    *
    * @param outputOwnership The output ownership to check.
    * @return true if the ownership exists; false otherwise.
    */
  fun ownershipExists(outputOwnership : OutputOwnership)(implicit db : KeyValueDatabase) : Boolean {
    val ownershipOption = db.getObject(OWNERSHIP_DESC, outputOwnership)(OutputOwnershipCodec, OwnershipDescriptorCodec)
    ownershipOption.isDefined
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
  fun putTransactionHash(outputOwnership : OutputOwnership, transactionHash : Hash)(implicit db : KeyValueDatabase) : Unit {
    if (!ownershipExists((outputOwnership))) {
      throw WalletException(ErrorCode.OwnershipNotFound)
    }

    db.putPrefixedObject(TXHASHES, outputOwnership.stringKey, transactionHash, OneByte(0))
  }

  /** Remove a transaction from the output ownership by transaction hash.
    *
    * Category : <Output Ownership -> Transactions> - Modification
    */
  fun delTransactionHash(outputOwnership : OutputOwnership, transactionHash : Hash)(implicit db : KeyValueDatabase) : Unit {
    db.delPrefixedObject(TXHASHES, outputOwnership.stringKey, transactionHash)
  }

  /** Get an iterator of transaction hashes searched by an optional account.
    *
    * Category : <Output Ownership -> Transactions> - Search
    *
    * @param outputOwnershipOption Some(ownership) to get transactions hashes related to an output ownership
    *                              None to get all transaction hashes for all output ownerships.
    */
  fun getTransactionHashes(outputOwnershipOption : Option<OutputOwnership>)(implicit db : KeyValueDatabase) : List<Hash> {
    (
      if (outputOwnershipOption.isEmpty) {
        // seekPrefixedObject returns (key, value) pairs, whereas we need the value only. map the pair to the value(2nd).
        using( db.seekPrefixedObject(TXHASHES)(HashCodec, OneByteCodec) ) in { _.toList }
      } else {
        using( db.seekPrefixedObject(TXHASHES, outputOwnershipOption.get.stringKey())(HashCodec, OneByteCodec) ) in { _.toList }
      }
    ).map{ case (CStringPrefixed(_, hash : Hash), _) => hash }
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
  fun putTransactionOutPoint(outputOwnership: OutputOwnership, output : OutPoint)(implicit db : KeyValueDatabase) : Unit {
    if (!ownershipExists((outputOwnership))) {
      throw WalletException(ErrorCode.OwnershipNotFound)
    }

    db.putPrefixedObject(OUTPOINTS, outputOwnership.stringKey, output, OneByte(0))
  }

  /** Remove a UTXO from the output ownership.
    *
    * Category : <Output Ownership -> UTXOs> - Modification
    */
  fun delTransactionOutPoint(outputOwnership: OutputOwnership, output : OutPoint)(implicit db : KeyValueDatabase) : Unit {
    db.delPrefixedObject(OUTPOINTS, outputOwnership.stringKey, output)
  }


  /** Get an iterator for transaction outpoints
    *
    * @param outputOwnershipOption Some(ownership) to iterate UTXOs for a specific output ownership.
    *                              None to iterate UTXOs for all output ownership.
    * @return The iterator for outpoints.
    */
  fun getTransactionOutPoints(outputOwnershipOption : Option<OutputOwnership>)(implicit db : KeyValueDatabase) : List<OutPoint> {
    (
      if (outputOwnershipOption.isEmpty) {
        // seekPrefixedObject returns (key, value) pairs, whereas we need the value only. map the pair to the value(2nd).
        using( db.seekPrefixedObject(OUTPOINTS)(OutPointCodec, OneByteCodec) ) in { _.toList }
      } else {
        using( db.seekPrefixedObject(OUTPOINTS, outputOwnershipOption.get.stringKey())(OutPointCodec, OneByteCodec) ) in { _.toList }
      }
    ).map{ case (CStringPrefixed(_, outPoint : OutPoint), _) => outPoint }
  }

  /*******************************************************************************************************
   * Category : <(transaction)Hash -> Transaction>
   *******************************************************************************************************/

  /** Add a transaction.
    *
    * Category : <(transaction)Hash -> Transaction> - Modification
    *
    */
  fun putWalletTransaction(transactionHash : Hash, transaction : WalletTransaction)(implicit db : KeyValueDatabase) : Unit {
    db.putObject(WALLETTX, transactionHash, transaction)
  }

  /** Remove a transaction.
    *
    * Category : <(transaction)Hash -> Transaction> - Modification
    */
  fun delWalletTransaction(transactionHash : Hash)(implicit db : KeyValueDatabase) : Unit {
    db.delObject(WALLETTX, transactionHash)
  }

  /** Search a transaction by the hash.
    *
    * Category : <(transaction)Hash -> Transaction> - Search
    */
  fun getWalletTransaction(transactionHash : Hash)(implicit db : KeyValueDatabase) : Option<WalletTransaction> {
    db.getObject(WALLETTX, transactionHash)(HashCodec, WalletTransactionCodec)
  }


  /*******************************************************************************************************
   * Category : <OutPoint -> TransactionOutput>
   *******************************************************************************************************/

  /** Add a transaction output.
    *
    * Category : <OutPoint -> TransactionOutput> - Modifications
    */
  fun putWalletOutput(outPoint : OutPoint, walletOutput : WalletOutput)(implicit db : KeyValueDatabase) : Unit {
    db.putObject(WALLETOUTPUT, outPoint, walletOutput)(OutPointCodec, WalletOutputCodec)
  }

  /** Remove a transaction output.
    *
    * Category : <OutPoint -> TransactionOutput> - Modifications
    */
  fun delWalletOutput(outPoint : OutPoint)(implicit db : KeyValueDatabase) : Unit {
    db.delObject(WALLETOUTPUT, outPoint)
  }

  /** Search a transaction output by the outpoint.
    *
    * Category : <OutPoint -> TransactionOutput> - Search
    */
  fun getWalletOutput(outPoint : OutPoint)(implicit db : KeyValueDatabase) : Option<WalletOutput> {
    db.getObject(WALLETOUTPUT, outPoint)(OutPointCodec, WalletOutputCodec)
  }

  /** Mark a UTXO spent searching by OutPoint.
    *
    * Category : <Output Ownership -> UTXOs> - Modification
    *
    * @return true if the output was found in the wallet; false otherwise.
    */
  fun markWalletOutputSpent(outPoint : OutPoint, spent : Boolean)(implicit db : KeyValueDatabase) : Boolean {
    val outPointOption : Option<WalletOutput> = db.getObject(WALLETOUTPUT, outPoint)(OutPointCodec, WalletOutputCodec)
    if (outPointOption.isEmpty) {
      false
    } else {
      db.putObject(WALLETOUTPUT, outPoint,
        outPointOption.get.copy(
          spent = spent
        )
      )
      true
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
    object PREFIXES {
      // Naming convention rules
      // 1. Name the prefix with the name of data we store.
      //    Ex> OWNERSHIPS stores onerships for an account.
      // 2. Use plural if we keep multiple entities under an entity.
      //    Ex> OWNERSHIPS, TXHASHES, OUTPOINTS


      /////////////////////////////////////////////////////////////////////////////////////////////////
      // Account -> Output Ownerships
      /////////////////////////////////////////////////////////////////////////////////////////////////

      // A. (Account + '\0' + OutputOwnership, Dummy)
      val OWNERSHIPS : Byte = 'O'

      // B. (OutputOwnership, OwnershipDescriptor)
      val OWNERSHIP_DESC : Byte = 'D'

      // C. (Account, OutputOwnership)
      val RECEIVING : Byte = 'R'

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // Output Ownership -> Transactions
      /////////////////////////////////////////////////////////////////////////////////////////////////
      // A. ( OutputOwnership + '\0' + (transaction)Hash )
      val TXHASHES : Byte = 'H'

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // Output Ownership -> UTXOs
      /////////////////////////////////////////////////////////////////////////////////////////////////
      // A. ( OutputOwnership + '\0' + OutPoint, None )
      val OUTPOINTS : Byte = 'P'

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // (transaction)Hash -> Transaction
      /////////////////////////////////////////////////////////////////////////////////////////////////
      // Keys and Values (K, V) :
      // A. ((transaction)Hash, WalletTransaction)
      val WALLETTX : Byte = 'T'

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // OutPoint -> WalletOutput
      /////////////////////////////////////////////////////////////////////////////////////////////////
      // Keys and Values (K, V) :
      // A. (OutPoint, WalletOutput)
      val WALLETOUTPUT : Byte = 'U'
    }
  }

}
