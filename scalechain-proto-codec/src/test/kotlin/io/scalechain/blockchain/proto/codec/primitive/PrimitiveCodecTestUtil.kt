package io.scalechain.blockchain.proto.codec.primitive

import java.math.BigInteger

/**
 * Created by kangmo on 14/12/2016.
 */
object PrimitiveCodecTestUtil {
  val ONE = BigInteger("1")
  fun maxShort(bits:Int) = (ONE.shiftLeft(bits) - ONE ).toShort()
  fun maxInt(bits:Int)   = (ONE.shiftLeft(bits) - ONE).toInt()
  fun maxLong(bits:Int)  = (ONE.shiftLeft(bits) - ONE).toLong()
  fun maxBigInteger(bits:Int) = (ONE.shiftLeft(bits) - ONE)
}