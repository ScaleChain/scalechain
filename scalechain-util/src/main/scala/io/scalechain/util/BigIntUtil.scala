package io.scalechain.util

/**
  * Created by kangmo on 2/3/16.
  */
object BigIntUtil {
  def bint(value : BigInt) = {
    s"""BigInt(\"$value\")"""
  }
}
