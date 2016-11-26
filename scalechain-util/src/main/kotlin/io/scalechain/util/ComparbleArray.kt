package io.scalechain.util


// Need to reimplement these classes according to the requirements of Kotlin.
/*
class ComparableArray<T>(val array : Array<T>) {
  //fun this(length : Int) = this( scala.Array<T>(length) )
  override fun hashCode() {
    array.toList.hashCode()
  }
  override fun equals(o : Any) {
    o match {
      case another : ComparableArray<T> => {
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
  fun length = array.length
}

object ComparableArray {
  implicit fun comparableArrayToArray<T>(carray : ComparableArray<T>) = carray.array
  implicit fun arrayToComparableArray<T>(array:Array<T>) = ComparableArray<T>(array)
}


data class ByteArray(override val array : ByteArray) : ComparableByteArray(array) {
  // BUGBUG : Dirty, .map(_.asInstanceOf<java.lang.Byte>)
  override fun toString = s"${HexUtil.scalaHex(array.map(_.asInstanceOf<java.lang.Byte>))}"
}

object ByteArray {
  implicit fun byteArrayToArray (barray : ByteArray   ) = barray.array
  implicit fun arrayToByteArray (array  : ByteArray ) = ByteArray(array)

  // BUGBUG : Dirty, .map(_.asInstanceOf<scala.Byte>)
  implicit fun stringToByteArray(value : String)
    = ByteArray.arrayToByteArray(HexUtil.bytes(value).map(_.asInstanceOf<scala.Byte>))

  // BUGBUG : Dirty, .map(_.asInstanceOf<java.lang.Byte>), Some("")
  implicit fun byteArrayToString(value : ByteArray)
    = HexUtil.hex( ByteArray.byteArrayToArray(value).map(_.asInstanceOf<java.lang.Byte>), Some("") )
}

object ByteArrayAndVectorConverter {
  implicit fun byteArrayToVector(barray : ByteArray   ) = barray.array.toVector
  implicit fun vectorToByteArray(vector : Vector<Byte>) = ByteArray(vector.toByteArray)
}
*/