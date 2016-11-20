package io.scalechain.crypto

import java.security.MessageDigest
import org.spongycastle.crypto.digests.RIPEMD160Digest

trait HashValue {
  val value: Array<Byte>
}

data class SHA1(bytes:Array<Byte>) : HashValue {
  override val value = bytes
}

data class SHA256(bytes:Array<Byte>) : HashValue {
  override val value = bytes
}

data class RIPEMD160(bytes:Array<Byte>) : HashValue {
  override val value = bytes
}

data class Hash160(bytes:Array<Byte>) : HashValue {
  override val value = bytes
}

data class Hash256(bytes:Array<Byte>) : HashValue {
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
  fun sha1(input: Array<Byte>) : SHA1 {
    val sha1md = MessageDigest.getInstance("SHA-1")
    SHA1( sha1md.digest(input) )
  }

  /**
   *
   * @param input
   * @return
   */
  fun sha256(input: Array<Byte>) : SHA256 {
    val sha256md = MessageDigest.getInstance("SHA-256")
    SHA256( sha256md.digest(input) )
  }

  /**
   *
   * @param input
   * @return
   */
  fun ripemd160(input: Array<Byte>) : RIPEMD160 {
    val md = RIPEMD160Digest()
    md.update(input, 0, input.length)
    val out = Array.fill<Byte>(md.getDigestSize())(0)
    md.doFinal(out, 0)
    RIPEMD160(out)
  }

  /** Return RIPEMD160(SHA256(x)) hash
   *
   * @param input
   * @return
   */
  fun hash160(input: Array<Byte>) : Hash160 {
    Hash160( ripemd160( sha256(input).value ).value )
  }

  /** Return SHA256(SHA256(x)) hash
   *
   * @param input
   * @return
   */
  fun hash256(input: Array<Byte>) : Hash256 {
    Hash256( sha256( sha256(input).value ).value )
  }
}
