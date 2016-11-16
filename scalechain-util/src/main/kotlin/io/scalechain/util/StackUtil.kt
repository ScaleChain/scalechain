package io.scalechain.util

import java.io.PrintWriter
import java.io.StringWriter


/**
 * Created by kangmo on 3/22/16.
 */
object StackUtil {
    @JvmStatic
    fun getStackTrace(e : Throwable) : String {
        val stringWriter = StringWriter()
        val writer = PrintWriter(stringWriter)
        e.printStackTrace(writer)
        return stringWriter.toString()
    }

    @JvmStatic
    fun getCurrentStack() : String {
        val stacktrace = Thread.currentThread().getStackTrace()
        return stacktrace.joinToString("\n")
    }
}
