package io.scalechain.wallet

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
class WalletStore {
  /** Find an account by coin address.
    *
    * Used by : getaccount RPC.
    *
    * @param address The coin address, which is attached to the account.
    * @return The found account.
    */
  def getAccount(address : CoinAddress) : String = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Find an account by account name.
    *
    * Used by : RPCs having the account name as a parameter.
    *
    * @param accountName The name of the account to find.
    * @return The found account.
    */
  def getAccount(accountName : String) : String = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Put an address into an account. Create an account if not exists.
    *
    * Used by : RPCs adding a new address to an account.
    *   1. importaddress RPC.
    *   2. getnewaddress RPC.
    *
    * @param accountName The name of the account to create.
    * @param outputOwnership The address or public key script to add to the account.
    * @param isReceivingAddress Whether the output ownership is the receiving address of the account.
    */
  def putOutputOwnership(accountName : String, outputOwnership : OutputOwnership, isReceivingAddress : Boolean ) : Unit = {
    // TODO : Implement
    assert(false)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Account -> Output Ownerships
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Keys and Values (K, V) :
  // A. (Account + '\0' + OutputOwnership, Option[PrivateKey]) => for Search 2, 3
  // B. (OutputOwnership, Account) => for Search case 1
  //
  // Modifications :
  // 1. Add an output ownership to an account. Create an account if it does not exist.
  // 2. Put the private key into an the address.
  //
  // Searches :
  // 1. Get an account by an output ownership.
  // 2. Iterate for each output ownerships for all accounts.
  // 3. Iterate private keys for all accounts.


  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Output Ownership -> Transactions
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Keys and Values (K, V) :
  // A. Including watch-only : ( OutputOwnership + '\0' + TransactionHash ) => For Search 1
  // B. Excluding watch-only : ( OutputOwnership + '\0' + TransactionHash ) => For Search 2
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
  // A. ( OutputOwnership + '\0' + OutPoint, Confirmation ) => For Search 1, 2, 3
  // B. ( OutPoint, OutputDescriptor ) => For Modification 2, and For Search 2
  //
  // OutputDescriptor has following fields :
  // 1. Spent : Boolean
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
  // A. (TransactionHash, Transaction)
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

}
