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
  // Account -> Output Ownership
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Create an account if it does not exist.
  // Get an account by an output ownership.
  // Put an output ownership into an account.
  // Add an output ownership to an account.
  // Iterate for each output ownerships.

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Output Ownership -> Private Keys
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Put the private key into an the address.
  // Iterate private keys for all accounts

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Output Ownership -> Transactions
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Put a transaction into the output ownership.
  // Remove a transaction from the output ownership by transaction hash
  // Iterate transactions by providing an account and the skip count. Include watch-only ownerships.
  // Iterate transactions by providing an account and the skip count. Exclude watch-only ownerships.

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Output Ownership -> UTXOs
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Put a UTXO into the output ownership.
  // Mark a UTXO spent searching by OutPoint.
  // Remove a UTXO from the output ownership.
  // Iterate UTXOs for an output ownership.
  // Iterate UTXOs for all output ownerships. Filter UTXOs based on confirmations.
  // Iterate UTXOs for a given addresses. Filter UTXOs based on confirmations.
}
