package io.scalechain.blockchain.storage.index

import io.kotlintest.matchers.Matchers

/**
  * Common util methods for testing key/value store.
  */
interface KeyValueCommonTrait : Matchers {
  /** Convert a string to a byte array.
    *
    * @param value The string to convert to a byte array.
    * @return The converted byte array.
    */
  fun B(value : String) = value.toByteArray()

  /** Convert an option of a byte array to an option of a byte list.
    * Because Scala uses referential equality check on arrays,
    * we need to convert arrays to lists.
    *
    * For lists, Scala checks each items in the list for the equality check.
    *
    * @param arrayOption The option of an array to convert.
    */
  fun L(arrayOption : ByteArray?) =
    arrayOption?.toList()

  fun PREFIX1() : Byte = '1'.code.toByte()
  fun PREFIX2() : Byte = '2'.code.toByte()
}
