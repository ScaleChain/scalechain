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
  def from(encoded : Array[Byte]) : PublicKey = {
    val point : ECPoint = ECKey.decodePublicKey(encoded)
    PublicKey(point)
  }


  /** Get a public key from a private key.
    *
    * @param privateKey The private key to derive the public key.
    * @return The derived public key.
    */
  def from(privateKey : PrivateKey, compressed : Boolean = false ) : PublicKey = {
    val encodedPublicKey : Array[Byte] = ECKey.publicKeyFromPrivate(privateKey.value, compressed)
    from(encodedPublicKey)
  }
}

/** The public key.
  *
  * TODO : Test Automation
  *
  * @param point The point on the elliptic curve, which represents a public key.
  */
case class PublicKey(point : ECPoint) {

  /** Encode the public key in either compressed or uncompressed format.
    *
    * @param compressed true to use the compressed format; false to use the uncompressed format.
    * @return The encoded public key.
    */
  def encode(compressed:Boolean) : Array[Byte] = {
    point.getEncoded(compressed)
  }

  /** Get the hash of the public key
    *
    * @return the public key hash.
    */
  def getHash() : Hash160 = {
    HashFunctions.hash160(encode(false/*uncompressed*/))
  }
}
