package io.scalechain.util

import java.util

import io.scalechain.util.HexUtil.scalaHex

/**
  * Created by kangmo on 1/30/16.
  */
class ComparableArray[T](val array : Array[T]) {
  //def this(length : Int) = this( new scala.Array[T](length) )
  override def hashCode() = {
    array.toList.hashCode()
  }
  override def equals(o : Any) = {
    o match {
      case another : ComparableArray[T] => {
        if (array.length == another.array.length) {
          (0 until array.length).forall { i =>
            array(i) == another.array(i)
          }
        } else {
          false
        }
      }
      case _ => false
    }
  }
  def length = array.length
}

object ComparableArray {
  implicit def comparableArrayToArray[T](carray : ComparableArray[T]) = carray.array
  implicit def arrayToComparableArray[T](array:Array[T]) = new ComparableArray[T](array)
}


class ByteArray(override val array : Array[Byte]) extends ComparableArray[Byte](array) {
  override def toString = s"ComparableArray(${scalaHex(array)})"
}

object ByteArray {
  implicit def byteArrayToArray(barray : ByteArray) = barray.array
  implicit def arrayToByteArray(array:Array[Byte]) = new ByteArray(array)
}
