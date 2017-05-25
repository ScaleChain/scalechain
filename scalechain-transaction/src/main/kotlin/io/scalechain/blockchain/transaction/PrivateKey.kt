package io.scalechain.blockchain.transaction

import java.math.BigInteger
import java.security.SecureRandom

import io.scalechain.crypto.Base58Check
import io.scalechain.util.Utils


/** A private key.
  *
  * Details :
  * https://en.bitcoin.it/wiki/Wallet_import_format
  */
data class PrivateKey(val version:Byte, val value:BigInteger, val isForCompressedPublicKey : Boolean) {
  /** Return the address in base58 encoding format.
    *
    * @return The base 58 check encoded private key.
    */
  fun base58(): String {
    val privateKeyBytes = Utils.bigIntegerToBytes(value, 32)
    assert( privateKeyBytes.size == 32)
    return Base58Check.encode(version, privateKeyBytes)
  }

  companion object {
    /** Translate the wallet import format to get a private key from it.
     *
     * @param walletImportFormat A private key in the wallet import format.
     * @return The translated private key.
     */
    @JvmStatic
    fun from(walletImportFormat : String) : PrivateKey {
      val (versionPrefix, rawPrivateKeyBytes) = Base58Check.decode(walletImportFormat)
      // TODO : Investigate : Bitcoin allows the private keys whose lengths are not 32

      val (isCompressed, privateKeyBytes) = if (rawPrivateKeyBytes.size == 33) {
        Pair(true, rawPrivateKeyBytes.dropLast(1).toByteArray())
      } else {
        Pair(false, rawPrivateKeyBytes)
      }

      val privateKeyBigInt = Utils.bytesToBigInteger(privateKeyBytes)
      return PrivateKey(versionPrefix, privateKeyBigInt, isCompressed)
    }

    /** Generate a private key.
     *
     * TODO : Test automation.
     *
     * @return The generated private key.
     */
    @JvmStatic
    fun generate() : PrivateKey {
      // Step 1 : Generate Random number. The random number is 32 bytes, and the range is [0 ~ 2^256)
      val random = SecureRandom()
      // On Java 8 this hangs. Need more investigation.
//    random.setSeed( random.generateSeed(32) )
      val keyValue : ByteArray = ByteArray(32)
      assert(keyValue.size == 32)
      random.nextBytes(keyValue)

      // Step 2 : Get the chain environment to get the secret key version.
      val chainEnv = ChainEnvironment.get()

      // BUGBUG : Use compressed private key by default.

      // Step 3 : Create the private key.
      return PrivateKey(chainEnv.SecretKeyVersion, Utils.bytesToBigInteger(keyValue), false)
    }
  }
}
