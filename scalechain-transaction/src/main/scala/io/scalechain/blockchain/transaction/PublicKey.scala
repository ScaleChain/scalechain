package io.scalechain.blockchain.transaction

import io.scalechain.crypto.ECKey
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
}
