package io.scalechain.wallet

// [ Wallet layer ] Store/Retrieve accounts
class AccountStore {
  /** Find an account by coin address.
    *
  * Used by : getaccount RPC.
  *
  * @param address The coin address, which is attached to the account.
  * @return The found account.
    */
  def getAccount(address : CoinAddress) : Account = {

    // TODO : Implement
    Account("getaccount")
  }
}
