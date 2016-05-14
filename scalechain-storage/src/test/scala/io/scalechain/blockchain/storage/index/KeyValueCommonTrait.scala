package io.scalechain.blockchain.storage.index

/**
  * Common util methods for testing key/value store.
  */
trait KeyValueCommonTrait {
  /** Convert a string to a byte array.
    *
    * @param value The string to convert to a byte array.
    * @return The converted byte array.
    */
  def B(value : String) = value.getBytes

  val PREFIX1 : Byte = '1'
  val PREFIX2 : Byte = '2'
}
