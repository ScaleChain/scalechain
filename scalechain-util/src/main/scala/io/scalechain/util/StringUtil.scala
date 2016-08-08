package io.scalechain.util

/**
  * Created by kangmo on 5/24/16.
  */
object StringUtil {
  def getBrief(string : String, maxLength : Int)
    = s"${if (string.length() >= maxLength) string.take(maxLength)+"..." else string}"
}
