package io.scalechain.util

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */
case class UInt64(value: Long) {
  //override def toString = s"UInt64(${UInt64.longToBigInt(value).toString})"
  override def toString = s"UInt64(${value}L)"
}

object UInt64 {

  def longToBigInt(unsignedLong: Long): BigInt =
    (BigInt(unsignedLong >>> 1) << 1) + (unsignedLong & 1)

  def bigIntToLong(n: BigInt): Long = {
    val smallestBit = (n & 1).toLong
    ((n >> 1).toLong << 1) | smallestBit
  }
}
