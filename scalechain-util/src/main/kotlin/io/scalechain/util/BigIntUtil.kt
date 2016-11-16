package io.scalechain.util

import scala.math.BigInt

/**
 * Created by kangmo on 2/3/16.
 */
object BigIntUtil {
    // BUGBUG : Get rid of scala type, BigInt some day.
    @JvmStatic
    fun bint(value : BigInt) : String {
        return "BigInt(\"$value\")"
    }
}
