package io.scalechain.blockchain.transaction

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.crypto.Base58Check
import io.scalechain.util.Utils

object PrivateKey {
  def from(walletImportFormat : String) : PrivateKey = {
    val (versionPrefix, privateKeyBytes) = Base58Check.decode(walletImportFormat)
    if (privateKeyBytes.length != 32) {
      throw new GeneralException(ErrorCode.RpcInvalidKey)
    }
    val privateKeyBigInt = new BigInteger(1, privateKeyBytes)
    PrivateKey(versionPrefix, privateKeyBigInt)
  }
}


/** A private key.
  */
case class PrivateKey(version:Byte, value:BigInteger) {
  /** Return the address in base58 encoding format.
    *
    * @return The base 58 check encoded private key.
    */
  def base58(): String = {
    val privateKeyBytes = Utils.bigIntegerToBytes(value, 32)
    assert( privateKeyBytes.length == 32)
    Base58Check.encode(version, privateKeyBytes)
  }
}
