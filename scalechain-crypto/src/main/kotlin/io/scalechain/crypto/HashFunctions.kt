package io.scalechain.crypto

import io.netty.buffer.ByteBuf
import io.scalechain.util.Bytes
import java.security.MessageDigest
import org.spongycastle.crypto.digests.RIPEMD160Digest

interface HashValue {
  val value: Bytes
}

data class SHA1(override val value:Bytes) : HashValue

data class SHA256(override val value:Bytes) : HashValue

data class RIPEMD160(override val value:Bytes) : HashValue

data class Hash160(override val value:Bytes) : HashValue

data class Hash256(override val value:Bytes) : HashValue

/**
 * Created by kangmo on 11/11/15.
 */
object HashFunctions {
  /**
   *
   * @param input
   * @return
   */
  fun sha1(input: ByteArray) : SHA1 {
    val sha1md = MessageDigest.getInstance("SHA-1")
    return SHA1( Bytes( sha1md.digest(input) ) )
  }

  /**
   *
   * @param input
   * @return
   */
  fun sha256(input: ByteArray) : SHA256 {
    val sha256md = MessageDigest.getInstance("SHA-256")
    return SHA256( Bytes( sha256md.digest(input) ) )
  }

  fun sha256(input: ByteArray, offset : Int, length : Int) : SHA256 {
    val sha256md = MessageDigest.getInstance("SHA-256")
    sha256md.update(input, offset, length)
    return SHA256( Bytes( sha256md.digest() ) )
  }


  /**
   *
   * @param input
   * @return
   */
  fun ripemd160(input: ByteArray) : RIPEMD160 {
    val md = RIPEMD160Digest()
    md.update(input, 0, input.size)
    val out = ByteArray(md.getDigestSize(), {0})
    md.doFinal(out, 0)
    return RIPEMD160( Bytes(out) )
  }

  /** Return RIPEMD160(SHA256(x)) hash
   *
   * @param input
   * @return
   */
  fun hash160(input: ByteArray) : Hash160 {
    return Hash160( ripemd160(sha256(input).value.array ).value )
  }

  /** Return SHA256(SHA256(x)) hash
   *
   * @param input
   * @return
   */
  fun hash256(input: ByteArray) : Hash256 {
    return Hash256( sha256( sha256(input).value.array ).value )
  }

  fun hash256(input: ByteArray, offset : Int, length : Int) : Hash256 {
    return Hash256( sha256( sha256(input, offset, length).value.array ).value )
  }

}
