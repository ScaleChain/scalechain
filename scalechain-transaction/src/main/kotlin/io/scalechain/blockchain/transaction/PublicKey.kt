package io.scalechain.blockchain.transaction

import io.scalechain.crypto.{Hash160, HashFunctions, ECKey}
import org.spongycastle.math.ec.ECPoint

/** The public key object that knows how to decode, encode public keys.
  *
  * TODO : Test Automation
  */
object PublicKey {
  /** Get a public key from an encoded one.
    *
    * @param encoded The encoded public key. Can be either a compressed one or uncompressed one.
    * @return The public key.
    */
  fun from(encoded : Array<Byte>) : PublicKey {
    val point : ECPoint = ECKey.decodePublicKey(encoded)
    PublicKey(point)
  }


  /** Get a public key from a private key.
    *
    * @param privateKey The private key to derive the public key.
    * @return The derived public key.
    */
  fun from(privateKey : PrivateKey) : PublicKey {
    val encodedPublicKey : Array<Byte> = ECKey.publicKeyFromPrivate(privateKey.value, false)
    val point : ECPoint = ECKey.decodePublicKey(encodedPublicKey)
    PublicKey(point, privateKey.isForCompressedPublicKey)
  }
}

/** The public key.
  *
  * TODO : Test Automation
  *
  * @param shouldUseCompressedFormat true to use the compressed format; false to use the uncompressed format.
  * @param point The point on the elliptic curve, which represents a public key.
  */
data class PublicKey(point : ECPoint, shouldUseCompressedFormat : Boolean = false) {

  /** Encode the public key in either compressed or uncompressed format.
    *
    * @return The encoded public key.
    */
  fun encode() : Array<Byte> {
    point.getEncoded(shouldUseCompressedFormat)
  }

  /** Get the hash of the public key
    *
    * @return the public key hash.
    */
  fun getHash() : Hash160 {
    HashFunctions.hash160(encode())
  }
}
