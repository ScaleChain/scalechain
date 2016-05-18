package io.scalechain.blockchain.transaction

import java.math.BigInteger
import java.security.SecureRandom

import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.crypto.Base58Check
import io.scalechain.util.Utils

object PrivateKey {
  /** Translate the wallet import format to get a private key from it.
    *
    * @param walletImportFormat A private key in the wallet import format.
    * @return The translated private key.
    */
  def from(walletImportFormat : String) : PrivateKey = {
    val (versionPrefix, privateKeyBytes) = Base58Check.decode(walletImportFormat)
    if (privateKeyBytes.length != 32) {
      throw new GeneralException(ErrorCode.RpcInvalidKey)
    }
    val privateKeyBigInt = new BigInteger(1, privateKeyBytes)
    PrivateKey(versionPrefix, privateKeyBigInt)
  }

  /** Generate a private key.
    *
    * TODO : Test automation.
    *
    * @return The generated private key.
    */
  def generate() : PrivateKey = {
    // Step 1 : Generate Random number. The random number is 32 bytes, and the range is [0 ~ 2^256)
    val random = new SecureRandom()
    random.setSeed( random.generateSeed(32) )
    val keyValue : Array[Byte] = new Array[Byte](32)
    assert(keyValue.length == 32)
    random.nextBytes(keyValue)

    // Step 2 : Get the chain environment to get the secret key version.
    val chainEnv = ChainEnvironmentFactory.getActive
    assert(chainEnv.isDefined)

    // Step 3 : Create the private key.
    PrivateKey(chainEnv.get.SecretKeyVersion, new BigInteger(keyValue))
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
