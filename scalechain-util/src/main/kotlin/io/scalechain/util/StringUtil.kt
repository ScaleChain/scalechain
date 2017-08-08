package io.scalechain.util

/**
 * Created by kangmo on 5/24/16.
 */
object StringUtil {
    @JvmStatic
    fun getBrief(string : String, maxLength : Int) : String
        = "${if (string.length > maxLength) string.take(maxLength)+"..." else string}"
}
