package io.scalechain.util

import java.io.{PrintWriter, StringWriter}

/**
  * Created by kangmo on 3/22/16.
  */
object StackUtil {
  def getStackTrace(e : Throwable) = {
    val stringWriter = new StringWriter
    val writer = new PrintWriter(stringWriter)
    e.printStackTrace(writer)
    stringWriter.toString
  }
}
