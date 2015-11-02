package io.scalechain.blockchain


object ErrorCode {
  val InvalidBlockMagic = ErrorCode("invalid_block_magic")
}

case class ErrorCode(val code:String)

/**
 * Created by kangmo on 11/2/15.
 */
class FatalException(code:ErrorCode) extends Exception {
}
