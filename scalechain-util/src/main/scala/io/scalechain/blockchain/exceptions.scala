package io.scalechain.blockchain

object ErrorCode {
  val InvalidBlockMagic = ErrorCode("invalid_block_magic")
  val InvalidScriptOperation = ErrorCode("invalid_script_operation")
  val ScriptTypeMismatch = ErrorCode("script_type_mismatch")
  val DisabledScriptOperation = ErrorCode("disabled_script_operation")
  // We expect script numbers on stack are up to 4 bytes. In case hitting any number encoded in more than 4 bytes, raise this error.
  val TooBigScriptInteger = ErrorCode("too_big_script_integer")
  val InvalidTransaction = ErrorCode("invalid_transaction")
  val NotEnoughInput = ErrorCode("not_enough_input")
  val NotEnoughScriptData = ErrorCode("not_enough_script_data")
  val TooManyPublicKeys = ErrorCode("too_many_public_keys")

  // Parse errors
  val NoDataAfterCodeSparator = ErrorCode("no_data_after_code_separator")
  val UnexpectedEndOfScript = ErrorCode("unexpected_end_of_script")
  val InvalidSignatureFormat= ErrorCode("invalid_signature_format")

  // Transaction verification errors
  val InvalidInputIndex = ErrorCode("invalid_input_index")
  val InvalidOutputIndex = ErrorCode("invalid_output_index")
  val TopValueFalse = ErrorCode("top_value_false")
  val ScriptParseFailure = ErrorCode("script_parse_failure")
  val ScriptEvalFailure = ErrorCode("script_eval_failure")
  val GeneralFailure= ErrorCode("general_failure")
  val InvalidOutputTransactionHash = ErrorCode("invalid_output_transaction")
  val UnsupportedHashType = ErrorCode("unsupported_hash_type")

  // HTTP errors
  val HttpRequestFailure = ErrorCode("http_request_failure")

  // Protocol Serialization errors.
  val SerializerNotRegistered = ErrorCode("serializer_not_registered")

  // Protocol Parse errors.
  val ParserNotRegistered = ErrorCode("parser_not_registered")
}

case class ErrorCode(val code:String)

/**
 * Created by kangmo on 11/2/15.
 */
class FatalException(val code:ErrorCode) extends Exception

class ScriptEvalException(val code:ErrorCode) extends Exception

class ScriptParseException(val code:ErrorCode) extends Exception

class ProtocolParseException(val code : ErrorCode) extends Exception
class ProtocolSerializeException(val code : ErrorCode) extends Exception

class HttpRequestException(val code:ErrorCode, httpCode : Int, reponse : String ) extends Exception

class TransactionVerificationException(val code:ErrorCode, val message : String = "", val stackTraceElements : Array[StackTraceElement] = Array()) extends Exception
{
  override def toString() = {
    s"TransactionVerificationException($code, $message, ${stackTraceElements.mkString(",\n")})"
  }
}