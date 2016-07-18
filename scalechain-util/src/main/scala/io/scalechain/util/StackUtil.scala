package io.scalechain.util

import java.io.{PrintWriter, StringWriter}

/**
  * Created by kangmo on 3/22/16.
  */
object StackUtil {
  def getStackTrace(e : Throwable) : String = {
    val stringWriter = new StringWriter
    val writer = new PrintWriter(stringWriter)
    e.printStackTrace(writer)
    stringWriter.toString
  }

  def getCurrentStack() : String = {
    val stacktrace = Thread.currentThread().getStackTrace()
    stacktrace.mkString("\n")
  }
}
