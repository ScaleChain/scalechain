package io.scalechain.util

import io.scalechain.util.HexUtil
import io.scalechain.util.HexUtil.kotlinHex

/**
 * A wrapper of ByteArray that implements equals and hashCode.
 * Why? ByteArray does not compare each element in the array when we call barray1 == barray2.
 */
data class Bytes(val array : ByteArray) {
    constructor(hexString : String) : this(HexUtil.bytes(hexString))

        override fun hashCode() : Int {
        // BUGBUG : OPTIMIZE : It is highly likely that we can optimize this code.
        return array.toList().hashCode()
    }

    override operator fun equals(other : Any?) : Boolean {
        when {
            other === null -> return false
            other === this -> return true
            other is Bytes -> {
                if (array.size == other.array.size) {
                    for ( i in 0 until array.size) {
                        if ( array[i] != other.array[i] ) {
                            return false
                        }
                    }
                    return true
                } else {
                    return false
                }

            }
            else -> {
                return false
            }
        }
    }

    override fun toString() = "Bytes(${kotlinHex(array)})"

    operator fun get(i : Int) = array.get(i)

    val size : Int
        get() = array.size
}
/*
object ComparableArray {
    implicit fun comparableArrayToArray<T>(carray : ComparableArray<T>) = carray.array
    implicit fun arrayToComparableArray<T>(array:Array<T>) = ComparableArray<T>(array)
}
*/

/*
object ByteArray {
    implicit fun byteArrayToArray (barray : ByteArray   ) = barray.array
    implicit fun arrayToByteArray (array  : Array<Byte> ) = ByteArray(array)

    implicit fun stringToByteArray(value : String)
    = ByteArray.arrayToByteArray(HexUtil.bytes(value))
    implicit fun byteArrayToString(value : ByteArray)
    = HexUtil.hex( ByteArray.byteArrayToArray(value) )
}

object ByteArrayAndVectorConverter {
    implicit fun byteArrayToVector(barray : ByteArray   ) = barray.array.toVector
    implicit fun vectorToByteArray(vector : Vector<Byte>) = ByteArray(vector.toArray<Byte>)
}
*/