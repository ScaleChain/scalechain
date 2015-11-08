package io.scalechain.blockchain


object ErrorCode {
  val InvalidBlockMagic = ErrorCode("invalid_block_magic")
  val InvalidSriptOperation = ErrorCode("invalid_script_operation")
  val ScriptTypeMismatch = ErrorCode("script_type_mismatch")
  val DisabledScriptOperation = ErrorCode("disabled_script_operation")
  // We expect script numbers on stack are up to 4 bytes. In case hitting any number encoded in more than 4 bytes, raise this error.
  val TooBigScriptInteger = ErrorCode("too_big_script_integer")
}

case class ErrorCode(val code:String)

/**
 * Created by kangmo on 11/2/15.
 */
class FatalException(code:ErrorCode) extends Exception {
}

class ScriptEvalException(code:ErrorCode) extends Exception {
}