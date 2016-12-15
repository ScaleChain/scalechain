package io.scalechain.util

import io.netty.buffer.ByteBuf
import java.util.*

/**
 * Created by kangmo on 15/12/2016.
 */
class Bytes(val array : ByteArray) {
  override fun toString() : String {
    return "Bytes(${HexUtil.kotlinHex(array)})"
  }

  // BUGBUG : Add test code
  override fun equals(other : Any?) : Boolean {
    when {
      other == null -> return false
      other is Bytes -> return Arrays.equals(this.array, other.array)
      else -> return false
    }
  }

  // BUGBUG : Add test code
  override fun hashCode() : Int {
    return Arrays.hashCode(this.array)
  }
}