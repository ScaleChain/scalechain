package io.scalechain.util

import java.security.MessageDigest



/**
  * Created by mijeong on 2016. 3. 4..
  */
object HashUtil {

  /**
    *
    * @param data
    * @param start
    * @param len
    * @return SHA256 hash value
    */
  def sha256(data: Array[Byte], start: Int, len: Int) : Array[Byte] = {

    val md = MessageDigest.getInstance("SHA-256")
    md.update(data.slice(start, start+len))
    md.digest()
  }

}
