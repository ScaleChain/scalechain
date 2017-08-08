package io.scalechain.util

/**
 * Created by kangmo on 2/3/16.
 */
object BigIntUtil {
    // BUGBUG : Get rid of scala type, BigInt some day.
    @JvmStatic
    fun bint(value : java.math.BigInteger) : String {
        return "BigInt(\"$value\")"
    }
}
