package io.scalechain.wallet

import io.scalechain.util.{HashUtil, Base58Util}



// [ Wallet layer ] A coin address for a specific account.
case class CoinAddress(address:String, purpose:String) {

  /**
    *
    * @return true or false (whether or not bitcoin address is valid)
    */
  def isValid : Boolean = {

    val addressSize = address.length

    if(addressSize < 26 || addressSize > 35)
      false
    else {
      val decodeAddress = Base58Util.decode(address, 25)
      val hashAddress = HashUtil.sha256(HashUtil.sha256(decodeAddress, 0, 21), 0, 32)
      hashAddress.slice(0, 4).sameElements(decodeAddress.slice(21,25))
    }
  }

}