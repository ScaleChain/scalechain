package io.scalechain.util

/**
 * Created by kangmo on 11/3/15.
 */
object HexUtil {
  /** Convert hex string to a byte array.
   *
   * c.f.> The output of DumpChain produces strings such as bytes("cafebebe").
   * Because we have HexUtil.bytes, we can copy the output of DumpChain to Scala source codes, if we imported HexUtil._ .
   *
   * @param hexString The hex string such as "cafebebe"
   * @return A byte array, which is converted from the given hex string.
   */
  def bytes(hexString: String): Array[Byte] = {
    hexString.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  /** Convert a byte array to a hex string with an optional separator between each byte.
   * Ex> In case sep=Some(",") the hex string will look like ca,fe,be,be.
   *
   * @param data The byte array to convert to a hex string.
   * @param sep The separator between each byte.
   * @return The hex string.
   */
  def hex(data: Array[Byte], sep: Option[String] = None): String = {
    sep match {
      case None => data.map("%02x".format(_)).mkString
      case _ => data.map("%02x".format(_)).mkString(sep.get)
    }
  }

  /** Return a hex string in pretty format.
   * Ex> ca fe be be
   *
   * @param data the byte array.
   * @return the hex string.
   */
  def prettyHex(data : Array[Byte]) : String = {
    s"${hex(data, Some(" "))}"
  }

  /** Return a string with the hex data in a format that can be copied to Scala source code to produce a byte array from it.
   * toString method of classes that have a byte array can call this method to print hex values.
   * The output can be copied to Scala source code because we have bytes(hex:String) method that converts hex string to a byte array.
   * @param data The byte array.
   * @return A string in a format, hex(hex-data)
   */
  def scalaHex(data:Array[Byte]) : String = {
    s"""bytes(\"${HexUtil.hex(data)}\")"""
  }
}
