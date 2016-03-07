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

  // Codec errors
  val CodecNotRegistered = ErrorCode("codec_not_registered")

  // Protocol Encode errors
  val EncodeFailure = ErrorCode("encode_failure")
  val RemainingNotEmptyAfterDecoding = ErrorCode("remaining_not_empty_after_decoding")

  // Protocol Decode errors
  val IncorrectMagicValue = ErrorCode("incorrect_magic_value")
  val DecodeFailure = ErrorCode("decode_failure")
  val PayloadLengthMismatch = ErrorCode("payload_length_mismatch")
  val PayloadChecksumMismatch = ErrorCode("payload_checksum_mismatch")

  // Block Storage errors
  val OutOfFileSpace = ErrorCode("out_of_file_space")
  val BlockFilePathNotExists = ErrorCode("block_file_path_not_exists")
  val InvalidFileNumber = ErrorCode("invalid_file_number")

  // RPC errors
  val RpcRequestParseFailure = ErrorCode("rpc_request_parse_failure")
  val RpcParameterTypeConversionFailure = ErrorCode("rpc_parameter_type_conversion_failure")
  val RpcMissingRequiredParameter = ErrorCode("rpc_missing_required_parameter")
  val RpcArgumentLessThanMinValue = ErrorCode("rpc_argument_less_than_min_value")
  val RpcArgumentGreaterThanMaxValue = ErrorCode("rpc_argument_greater_than_max_value")
  val RpcInvalidAddress = ErrorCode("rpc_invalid_address")
}


case class ErrorCode(val code:String)

trait ExceptionWithErrorCode extends Exception {
  val message : String = ""
  val code : ErrorCode
}

/**
 * Created by kangmo on 11/2/15.
 */
class FatalException(val code:ErrorCode) extends ExceptionWithErrorCode

class ScriptEvalException(val code:ErrorCode) extends ExceptionWithErrorCode

class ScriptParseException(val code:ErrorCode) extends ExceptionWithErrorCode

class ProtocolCodecException(val code : ErrorCode, override val message : String = "") extends ExceptionWithErrorCode

class HttpRequestException(val code:ErrorCode, httpCode : Int, reponse : String ) extends ExceptionWithErrorCode

class TransactionStorageException(val code : ErrorCode) extends ExceptionWithErrorCode

class BlockStorageException(val code : ErrorCode) extends ExceptionWithErrorCode

class TransactionVerificationException(val code:ErrorCode, override val message : String = "", val stackTraceElements : Array[StackTraceElement] = Array()) extends ExceptionWithErrorCode
{
  override def toString() = {
    s"TransactionVerificationException($code, $message, ${stackTraceElements.mkString(",\n")})"
  }
}

class RpcException(val code :ErrorCode, override val message : String = "") extends ExceptionWithErrorCode