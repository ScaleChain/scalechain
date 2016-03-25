package io.scalechain.wallet

import io.scalechain.crypto.ECKey

// [ Wallet layer ] An account, which is a group of addresses.
case class Account(name:String) {

  private val RECEIVED_ADDRESS = "received"

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
    val key = new ECKey()
    println("key --> \n" + key)
    val address = new Address(0, key.getPubKeyHash)
    println("address --> \n" + address)
    CoinAddress(address.toString, RECEIVED_ADDRESS, key.getPubKeyHash, key.getPrivKeyBytes)
  }
}