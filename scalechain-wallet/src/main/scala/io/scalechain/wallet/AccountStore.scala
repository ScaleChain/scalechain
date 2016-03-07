package io.scalechain.wallet

import io.scalechain.blockchain.{ErrorCode, WalletException}

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

    if(address.isValid) {
      val account = Some("getAccount") // TODO: Replace findAccount in storage layer
      Account(account.getOrElse(""))
    } else {
      throw new WalletException(ErrorCode.AddressNotValid)
    }
  }
}
