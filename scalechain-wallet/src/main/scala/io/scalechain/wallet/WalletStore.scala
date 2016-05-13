package io.scalechain.wallet

/** A storage for the wallet.
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
    */
  def putOutputOwnership(accountName : String, outputOwnership : OutputOwnership) : Unit = {
    // TODO : Implement
    assert(false)
  }

}
