package io.scalechain.util

import java.util.*

/** Utility class for arrays.
 */
object ArrayUtil {

    /** pad an array with the given value.
     *
     * @param array The input array. At the end of this array, the given value is padded.
     * @param targetLength After padding, the length of the array becomes targetLength.
     * @param value The value to use for padding the input array.
     * @return The newly padded array whose length is targetLength.
     */
    @JvmStatic
    fun pad (array : ByteArray, targetLength : Int, value : Byte) : ByteArray {
        if ( targetLength > array.size ) {
            val padArray = ByteArray(targetLength - array.size, { value } )
            return array + padArray
        } else {
            return array
        }
    }

    /** Get rid of padded values at the end of the array.
     *
     * @param array The array to remove padding values at the end of it.
     * @param value The value to remove at the end of the input array.
     * @return The shortened array which does not have any padded values at the end of it.
     */
    @JvmStatic
    fun unpad(array : ByteArray, value : Byte) : ByteArray {

        return array.dropLastWhile { it == value }.toByteArray()
    }

    @JvmStatic
    fun isEqual(left : ByteArray, right : ByteArray) : Boolean {
        return Arrays.equals(left, right)
    }

    @JvmStatic
    fun compare(left : ByteArray, right : ByteArray) : Int {
        // get the minimum length of the two arrays.
        var minLength = if ( left.size < right.size ) left.size else right.size
        for ( i in 0 until minLength) {
            if ( left[i] == right[i] ) {
                continue
            }
            return left[i] - right[i]
        }
        // If one array starts with the contents of another array, the logner one is greater.
        if ( left.size < right.size )
            return -1
        else if (left.size > right.size)
            return 1
        else
            return 0
    }
}
