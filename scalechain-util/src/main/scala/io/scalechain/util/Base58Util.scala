package io.scalechain.util



/**
  * Created by mijeong on 2016. 3. 4..
  */
object Base58Util {

  val base = 58
  val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

  /**
    *
    * @param input
    * @param len
    * @return decode value based on Base58
    */
  def decode(input: String, len: Int) : Array[Byte] = {

    // TODO: Refactoring
    val decodeValue = new Array[Byte](len)
    var check = true

    for(i <- 0 to input.length-1) {
      val c = input.charAt(i)
      var p = alphabet.indexOf(c)

      for(j <- len-1 to 1 by -1) {
        p = p + (base * (decodeValue(j) & 0xFF))
        decodeValue(j) = (p%256).toByte
        p = p / 256
      }

      if(p != 0)
        check = false
    }

    if (check)
      decodeValue
    else
      null
  }

}
