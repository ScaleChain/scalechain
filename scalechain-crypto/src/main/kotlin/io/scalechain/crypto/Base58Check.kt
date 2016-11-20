package io.scalechain.crypto

import java.util

import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.util.Base58Util

/**
  * https://en.bitcoin.it/wiki/Base58Check_encoding
  * Base58Check is a format based on Base58 and used a lot in bitcoin, for encoding addresses and private keys for
  * example. It includes a prefix (usually a single byte) and a checksum so you know what has been encoded, and that it has
  * been transmitted correctly.
  * For example, to create an address for a public key you could write:
  * {{{
  *   val pub: BinaryData = "0202a406624211f2abbdc68da3df929f938c3399dd79fac1b51b0e4ad1d26a47aa"
  *   val address = Base58Check.encode(version, Crypto.hash160(pub))
  * }}}
  * And to decode a private key you could write:
  * {{{
  *   // check that is it a mainnet private key
  *   val (Base58.Prefix.SecretKey, priv) = Base58Check.decode("5J3mBbAH58CpQ3Y5RNJpUKPE62SQ5tfcvU2JpbnkeyhfsYB1Jcn")
  * }}}
  *
  */
object Base58Check {
  fun checksum(data: Array<Byte>) = HashFunctions.hash256(data).value.take(4)

  /**
    * Encode data in Base58Check format.
    * For example, to create an address from a public key you could use:
    *
    * @param prefix version prefix (one byte)
    * @param data date to be encoded
    * @return a Base58 string
    */
  fun encode(prefix: Byte, data: Array<Byte>) : String {
    val prefixAndData = prefix +: data
    Base58Util.encode(prefixAndData ++ checksum(prefixAndData))
  }

  /**
    *
    * @param prefix version prefix (several bytes, as used with BIP32 ExtendedKeys for example)
    * @param data data to be encoded
    * @return a Base58 String
    */
  fun encode(prefix: Array<Byte>, data: Array<Byte>) : String {
    val prefixAndData = prefix ++ data
    Base58Util.encode(prefixAndData ++ checksum(prefixAndData))
  }

  /**
    * Decodes Base58 data that has been encoded with a single byte prefix
    *
    * @param encoded encoded data
    * @return a (prefix, data) tuple
    * @throws RuntimeException if the checksum that is part of the encoded data cannot be verified
    */
  fun decode(encoded: String) : (Byte, Array<Byte>) {
    val raw = Base58Util.decode(encoded)
    val versionAndHash = raw.dropRight(4)
    val checksum = raw.takeRight(4)
    if (!util.Arrays.equals(checksum, Base58Check.checksum(versionAndHash))) {
      throw GeneralException(ErrorCode.InvalidChecksum)
    }
    (versionAndHash(0), versionAndHash.tail)
  }
}
