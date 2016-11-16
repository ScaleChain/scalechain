package io.scalechain.util

import java.util


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


case class ByteArray(override val array : Array[Byte]) extends ComparableArray[Byte](array) {
  // BUGBUG : Dirty, .map(_.asInstanceOf[java.lang.Byte])
  override def toString = s"${HexUtil.scalaHex(array.map(_.asInstanceOf[java.lang.Byte]))}"
}

object ByteArray {
  implicit def byteArrayToArray (barray : ByteArray   ) = barray.array
  implicit def arrayToByteArray (array  : Array[Byte] ) = ByteArray(array)

  // BUGBUG : Dirty, .map(_.asInstanceOf[scala.Byte])
  implicit def stringToByteArray(value : String)
    = ByteArray.arrayToByteArray(HexUtil.bytes(value).map(_.asInstanceOf[scala.Byte]))

  // BUGBUG : Dirty, .map(_.asInstanceOf[java.lang.Byte]), Some("")
  implicit def byteArrayToString(value : ByteArray)
    = HexUtil.hex( ByteArray.byteArrayToArray(value).map(_.asInstanceOf[java.lang.Byte]), Some("") )
}

object ByteArrayAndVectorConverter {
  implicit def byteArrayToVector(barray : ByteArray   ) = barray.array.toVector
  implicit def vectorToByteArray(vector : Vector[Byte]) = ByteArray(vector.toArray[Byte])
}