package io.scalechain.util

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
  def pad (array : Array[Byte], targetLength : Int, value : Byte) : Array[Byte] = {
    array.padTo(targetLength, value)
  }

  /** Get rid of padded values at the end of the array.
    *
    * @param array The array to remove padding values at the end of it.
    * @param value The value to remove at the end of the input array.
    * @return The shortened array which does not have any padded values at the end of it.
    */
  def unpad(array : Array[Byte], value : Byte) : Array[Byte] = {
    array.take(array.lastIndexWhere(_ != 0)+1)
  }
}
