package io.scalechain.blockchain.proto

import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil

/**
 * Created by kangmo on 16/01/2017.
 */

data class Checksum(val value : Bytes) {
  init {
    assert(value.array.size == Checksum.VALUE_SIZE)
  }

  override fun toString() = "Checksum(${HexUtil.kotlinHex(value.array)})"

  companion object {
    val VALUE_SIZE = 4

    fun fromHex(hexString : String) = Checksum(Bytes.from(hexString))
  }
}

data class Magic(val value : Bytes) {
  init {
    assert(value.array.size == Magic.VALUE_SIZE )
  }
  override fun toString() = "Magic(${HexUtil.kotlinHex(value.array)})"

  companion object {
    val VALUE_SIZE = 4

    val MAIN     = fromHex("D9B4BEF9")
    val TESTNET  = fromHex("DAB5BFFA")
    val TESTNET3 = fromHex("0709110B")
    val NAMECOIN = fromHex("FEB4BEF9")

    fun fromHex(hexString : String) = Magic(Bytes.from(hexString))
  }
}
