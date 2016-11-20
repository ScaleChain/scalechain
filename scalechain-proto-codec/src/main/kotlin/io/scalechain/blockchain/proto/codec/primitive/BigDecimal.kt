package io.scalechain.blockchain.proto.codec.primitive

import scodec.codecs.variableSizeBytes
import scodec.codecs.int32
import scodec.codecs.ascii
/**
  * Created by kangmo on 5/15/16.
  */
object BigDecimal {
  fun toBigDecimal(value:String) : scala.math.BigDecimal = scala.math.BigDecimal(value)
  fun toString(value:scala.math.BigDecimal) : String = value.toString

  val codec = variableSizeBytes(int32, ascii).xmap(toBigDecimal, toString)
}
