package io.scalechain.wallet

// [ Wallet layer ] An account, which is a group of addresses.
case class Account(name:String) {
  /** Returns the current address for receiving payments to this account.
    *
    * Used by : getaccountaddress RPC.
    *
    * @return The coin address for receiving payments.
    */
  def getReceivingAddress : CoinAddress = {

    // TODO : Implement
    assert(false)
    null
  }

  /** Returns a new coin address for receiving payments.
    *
    * Used by : newaddress RPC.
    *
    * @return the new address for receiving payments.
    */
  def newAddress : CoinAddress = {

    // TODO : Implement
    assert(false)
    null
  }
}
