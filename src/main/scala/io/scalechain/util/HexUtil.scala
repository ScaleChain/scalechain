package io.scalechain.util

/**
 * Created by kangmo on 11/3/15.
 */
object HexUtil {
  def hex2bytes(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  def bytes2hex(bytes: Array[Byte], sep: Option[String] = None): String = {
    sep match {
      case None => bytes.map("%02x".format(_)).mkString
      case _ => bytes.map("%02x".format(_)).mkString(sep.get)
    }
    // bytes.foreach(println)
  }

  /** Return a hex string in pretty format.
   * Ex> CA FE BE BE
   *
   * @param bytes
   * @return
   */
  def prettyHex(bytes : Array[Byte]) : String = {
    s"${bytes2hex(bytes, Some(" "))}"
  }
}
