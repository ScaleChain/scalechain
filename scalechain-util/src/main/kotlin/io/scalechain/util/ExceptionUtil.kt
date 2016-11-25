package io.scalechain.util

/**
 * Created by kangmo on 6/26/16.
 */
object ExceptionUtil {
    @JvmStatic
    fun describe(throwable : Throwable?) : Unit {
        if (throwable == null)
            ""
        else
            "{exception : ${throwable}, stack : ${StackUtil.getStackTrace(throwable)} }"
    }

}
