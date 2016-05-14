package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.transaction.{PrivateKey, UnspentTransactionOutput}

/////////////////////////////////////////////////////////////////////////////////////////////////
// Account -> Output Ownerships
/////////////////////////////////////////////////////////////////////////////////////////////////
// Keys and Values (K, V) :
// A. (Account + '\0' + OutputOwnership, OwnershipDescriptor) => for Search 2, 3
// B. (OutputOwnership, Account) => for Search case 1
// C. (Account, OutputOwnership) => For keeping the receiving address. For Modification 3 and Search 4
//
// OwnershipDescriptor has the following fields.
// 1. privateKeys : List[PrivateKey]
//
// Modifications :
// 1. Add an output ownership to an account. Create an account if it does not exist.
// 2. Put the private key into an the address.
// 3. Mark an address of an account as the receiving address.
//
// Searches :
// 1. Get an account by an address.
// 2. Iterate for each output ownerships for all accounts.
// 3. Iterate private keys for all accounts.
// 4. Get the receiving address of an account.


/////////////////////////////////////////////////////////////////////////////////////////////////
// Output Ownership -> Transactions
/////////////////////////////////////////////////////////////////////////////////////////////////
// Keys and Values (K, V) :
// A. ( OutputOwnership + '\0' + TransactionHash ) => For Search 1,2
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
// B. ( OutPoint, OutputDescriptor ) => For Modification 2
//
// OutputDescriptor has the following fields :
// 1. Spent : Boolean
// 2. Confirmations : Long
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
// TransactionHash -> Transaction
/////////////////////////////////////////////////////////////////////////////////////////////////
// Keys and Values (K, V) :
// A. (TransactionHash, WalletTransaction)
//
// Modifications :
// 1. Add a transaction.
// 2. Remove a transaction.
//
// Searches :
// 1. Search a transaction by the hash.


/////////////////////////////////////////////////////////////////////////////////////////////////
// OutPoint -> TransactionOutput
/////////////////////////////////////////////////////////////////////////////////////////////////
// Keys and Values (K, V) :
// A. (OutPoint, TransactionOutput)
//
// Modifications :
// 1. Add a transaction output.
// 2. Remove a transaction output.
//
// Searches :
// 1. Search a transaction output by the outpoint.

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
  *   2. An output ownership has multiple transactions(either receiving new UTXOs or spending UTXOs).
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
class WalletStore(walletFolder : File) {
  /*******************************************************************************************************
   * Category : [Account -> Output Ownerships]
   *******************************************************************************************************/

  /** Add an output ownership to an account. Create an account if it does not exist.
    *
    * Category : [Account -> Output Ownerships] - Modification
    *
    * Used by : RPCs adding a new address to an account.
    *   1. importaddress RPC.
    *   2. getnewaddress RPC.
    *
    * @param accountName The name of the account to create.
    * @param outputOwnership The address or public key script to add to the account.
    */
  def putOutputOwnership(accountName : String, outputOwnership : OutputOwnership ) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Put the private key into an the address.
    *
    * Category : [Account -> Output Ownerships] - Modification
    */

  /** Mark an address of an account as the receiving address.
    *
    * Category : [Account -> Output Ownerships] - Modification
    */
  def putReceivingAddress(accountName : String, outputOwnership : CoinAddress ) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Find an account by coin address.
    *
    * Used by : getaccount RPC.
    *
    * Category : [Account -> Output Ownerships] - Search
    *
    * @param address The coin address, which is attached to the account.
    * @return The found account.
    */
  def getAccount(address : CoinAddress) : String = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Iterate for each output ownerships for all accounts.
    *
    * Category : [Account -> Output Ownerships] - Search
    */
  def getOutputOwnerships() : Iterator[OutputOwnership]= {
    // TODO : Implement
    assert(false)
    null
  }

  /** Iterate private keys for all accounts.
    *
    * Category : [Account -> Output Ownerships] - Search
    */
  def getPrivateKeys : Iterator[PrivateKey] = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Get the receiving address of an account.
    *
    * Category : [Account -> Output Ownerships] - Search
    */
  def getReceivingAddress() : CoinAddress = {
    // TODO : Implement
    assert(false)
    null
  }


  /*******************************************************************************************************
   * Category : [Output Ownership -> Transactions]
   *******************************************************************************************************/

  /** Put a transaction into the output ownership.
    *
    * Category : [Output Ownership -> Transactions] - Modification
    */
  def putTransaction(outputOwnership : OutputOwnership, transactionHash : Hash) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Remove a transaction from the output ownership by transaction hash.
    *
    * Category : [Output Ownership -> Transactions] - Modification
    */
  def delTransaction(outputOwnership : OutputOwnership, transactionHash : Hash) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Iterate transactions by providing an account and the skip count.
    *
    * Category : [Output Ownership -> Transactions] - Search
    *
    * @param includeWatchOnly true to include watch-only ownerships; false otherwise.
    */
  def getTransactions(account : String, skipCount : Long, includeWatchOnly : Boolean) : Iterator[Transaction] = {
    // TODO : Implement
    assert(false)
    null
  }

  /*******************************************************************************************************
   * Category : Category : [Output Ownership -> UTXOs]
   *******************************************************************************************************/
  /** Put a UTXO into the output ownership.
    *
    * Category : [Output Ownership -> UTXOs] - Modification
    */
  def putTransactionOutput(outputOwnership: OutputOwnership, output : OutPoint) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Mark a UTXO spent searching by OutPoint.
    *
    * Category : [Output Ownership -> UTXOs] - Modification
    */
  def markOutputSpent(outPoint : OutPoint) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Remove a UTXO from the output ownership.
    *
    * Category : [Output Ownership -> UTXOs] - Modification
    */
  def delTransactionOutput(outputOwnership: OutputOwnership, output : OutPoint) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Iterate UTXOs.
    *
    * Category : [Output Ownership -> UTXOs] - Search
    *
    * @param outputOwnershipOption Some(ownership) to iterate UTXOs for a specific output ownership.
    *                              None to iterate UTXOs for all output ownership.
    */
  def getTransactionOutputs(outputOwnershipOption : Option[OutputOwnership]) : Iterator[UnspentCoin] = {
    // TODO : Implement
    assert(false)
    null
  }



  /*******************************************************************************************************
   * Category : [TransactionHash -> Transaction]
   *******************************************************************************************************/

  /** Add a transaction.
    *
    * Category : [TransactionHash -> Transaction] - Modification
    */
  def putTransaction(transactionHash : Hash, transaction : WalletTransaction) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Remove a transaction.
    *
    * Category : [TransactionHash -> Transaction] - Modification
    */
  def delTransaction(transactionHash : Hash, transaction : WalletTransaction) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Search a transaction by the hash.
    *
    * Category : [TransactionHash -> Transaction] - Search
    */
  def getTransaction(transactionHash : Hash) : WalletTransaction = {
    // TODO : Implement
    assert(false)
    null
  }


  /*******************************************************************************************************
   * Category : [OutPoint -> TransactionOutput]
   *******************************************************************************************************/

  /** Add a transaction output.
    *
    * Category : [OutPoint -> TransactionOutput] - Modifications
    */
  def putTransactionOutput(outPoint : OutPoint, transactionOutput : TransactionOutput) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Remove a transaction output.
    *
    * Category : [OutPoint -> TransactionOutput] - Modifications
    */
  def delTransactionOutput(outPoint : OutPoint, transactionOutput : TransactionOutput) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /** Search a transaction output by the outpoint.
    *
    * Category : [OutPoint -> TransactionOutput] - Search
    */
  def getTransactionOutput(outPoint : OutPoint) : TransactionOutput = {
    // TODO : Implement
    assert(false)
    null
  }
}
