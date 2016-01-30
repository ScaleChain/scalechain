package io.scalechain.io

import java.nio.charset.CodingErrorAction

import io.scalechain.util.HexUtil
import scala.io.{Codec, Source}
/**
  * Created by kangmo on 1/30/16.
  */
object HexFileLoader {
  def load(path:String) : Array[Byte] = {
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    val buffer = new StringBuffer()

    for( line <- Source.fromFile(path).getLines()) {
      buffer.append(line.substring(10,60))
      buffer.append("\n")
    }
    HexUtil.bytes(buffer.toString)
  }
}


