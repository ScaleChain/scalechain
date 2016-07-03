package io.scalechain.util

/**
  * Created by kangmo on 6/26/16.
  */
object ExceptionUtil {
  def describe(throwable : Throwable) =
    if (throwable == null)
      ""
    else
      s"{ exception : ${throwable}, stack : ${StackUtil.getStackTrace(throwable)} }"

}
