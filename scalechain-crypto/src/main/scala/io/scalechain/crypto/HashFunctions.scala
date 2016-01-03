package io.scalechain.crypto

import java.security.MessageDigest
import org.spongycastle.crypto.digests.RIPEMD160Digest

trait HashValue {
  val value: Array[Byte]
}

case class SHA1(bytes:Array[Byte]) extends HashValue {
  override val value = bytes
}

case class SHA256(bytes:Array[Byte]) extends HashValue {
  override val value = bytes
}

case class RIPEMD160(bytes:Array[Byte]) extends HashValue {
  override val value = bytes
}

case class Hash160(bytes:Array[Byte]) extends HashValue {
  override val value = bytes
}

case class Hash256(bytes:Array[Byte]) extends HashValue {
  override val value = bytes
}


/**
 * Created by kangmo on 11/11/15.
 */
object HashFunctions {
  /**
   *
   * @param input
   * @return
   */
  def sha1(input: Array[Byte]) : SHA1 = {
    val md = MessageDigest.getInstance("SHA-1")
    SHA1( md.digest(input) )
  }

  /**
   *
   * @param input
   * @return
   */
  def sha256(input: Array[Byte]) : SHA256 = {
    val md = MessageDigest.getInstance("SHA-256")
    SHA256( md.digest(input) )
  }

  /**
   *
   * @param input
   * @return
   */
  def ripemd160(input: Array[Byte]) : RIPEMD160 = {
    val md = new RIPEMD160Digest()
    md.update(input, 0, input.length)
    val out = Array.fill[Byte](md.getDigestSize())(0)
    md.doFinal(out, 0)
    RIPEMD160(out)
  }

  /** Return RIPEMD160(SHA256(x)) hash
   *
   * @param input
   * @return
   */
  def hash160(input: Array[Byte]) : Hash160 = {
    Hash160( ripemd160( sha256(input).value ).value )
  }

  /** Return SHA256(SHA256(x)) hash
   *
   * @param input
   * @return
   */
  def hash256(input: Array[Byte]) : Hash256 = {
    Hash256( sha256( sha256(input).value ).value )
  }
}
