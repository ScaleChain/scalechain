package io.scalechain.util

/**
 * Created by kangmo on 6/26/16.
 */
object ExceptionUtil {
    @JvmStatic
    fun describe(throwable : Throwable?) : String {
        if (throwable == null)
            return ""
        else
            return "{exception : ${throwable}, stack : ${StackUtil.getStackTrace(throwable)} }"
    }

}
