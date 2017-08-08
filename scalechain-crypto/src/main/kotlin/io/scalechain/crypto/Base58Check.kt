package io.scalechain.crypto

import io.netty.buffer.ByteBuf

import io.scalechain.blockchain.*
import io.scalechain.util.Base58Util
import java.util.*

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
  fun checksum(data: ByteArray) : ByteArray = HashFunctions.hash256(data).value.array.take(4).toByteArray()

  /**
    * Encode data in Base58Check format.
    * For example, to create an address from a public key you could use:
    *
    * @param prefix version prefix (one byte)
    * @param data date to be encoded
    * @return a Base58 string
    */
  fun encode(prefix: Byte, data: ByteArray) : String {
    return encode(ByteArray(1, {prefix}), data)
  }

  /**
    *
    * @param prefix version prefix (several bytes, as used with BIP32 ExtendedKeys for example)
    * @param data data to be encoded
    * @return a Base58 String
    */
  fun encode(prefix: ByteArray, data: ByteArray) : String {
    val prefixAndData = prefix + data
    return Base58Util.encode(prefixAndData + checksum(prefixAndData))
  }

  /**
    * Decodes Base58 data that has been encoded with a single byte prefix
    *
    * @param encoded encoded data
    * @return a (prefix, data) tuple
    * @throws RuntimeException if the checksum that is part of the encoded data cannot be verified
    */
  fun decode(encoded: String) : Pair<Byte, ByteArray> {
    val raw = Base58Util.decode(encoded)
    val versionAndHash : ByteArray = raw.dropLast(4).toByteArray()
    val checksum : ByteArray = raw.takeLast(4).toByteArray()
    if (!Arrays.equals(checksum, Base58Check.checksum(versionAndHash))) {
      throw GeneralException(ErrorCode.InvalidChecksum)
    }
    return Pair(versionAndHash.get(0), versionAndHash.drop(1).toByteArray())
  }
}
