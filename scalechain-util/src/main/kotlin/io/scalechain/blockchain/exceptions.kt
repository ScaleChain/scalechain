package io.scalechain.blockchain

data class ErrorCode(val code:String) {
  companion object {
    val InternalError = ErrorCode("internal_error")
    val UnsupportedFeature = ErrorCode("unsupported_feature")

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
    val TopValueFalse = ErrorCode("top_value_false")
    val ScriptParseFailure = ErrorCode("script_parse_failure")
    val ScriptEvalFailure = ErrorCode("script_eval_failure")
    val GeneralFailure= ErrorCode("general_failure")
    val UnsupportedHashType = ErrorCode("unsupported_hash_type")
    val NotEnoughStackValues = ErrorCode("not_enough_stack_values")

//  val InvalidOutputIndex = ErrorCode("invalid_output_index")
//  val InvalidOutputTransactionHash = ErrorCode("invalid_output_transaction")

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
    val RpcInvalidAddress = ErrorCode("invalid_address")
    val RpcInvalidKey = ErrorCode("invalid_key")
    val RpcInvalidParameter = ErrorCode("rpc_invalid_parameter")

    // Base58 encoding errors
    val InvalidChecksum = ErrorCode("invalid_checksum")


    // RocksDadatabase errors
    val NoMoreKeys = ErrorCode("no_more_keys")


    // Wallet Exceptions
    val OwnershipNotFound = ErrorCode("ownership_not_found")
    val WalletOutputNotFound = ErrorCode("wallet_output_not_found")

    // Transaction Builder exceptions.
    val NotEnoughTransactionInput      = ErrorCode("not_enough_transaction_input")
    val NotEnoughTransactionOutput     = ErrorCode("not_enough_transaction_output")
    val NotEnoughInputAmounts          = ErrorCode("not_enough_input_amounts")
    val GenerationInputWithOtherInputs = ErrorCode("generation_input_with_other_inputs")
    val SpendingOutputNotFound         = ErrorCode("spending_output_not_found")

    // Transaction Signer
    val UnableToSignCoinbaseTransaction = ErrorCode("unable_to_sign_coinbase_transaction")
    val InvalidTransactionInput = ErrorCode("invalid_transaction_input")

    // Chain layer
    val InvalidBlockHeight             = ErrorCode("invalid_block_height")
    val InvalidBlockHeightOnDatabase   = ErrorCode("invalid_block_height_on_database")
    val InvalidTransactionOutPoint     = ErrorCode("invalid_transaction_out_point")
    val TransactionOutputAlreadySpent  = ErrorCode("transaction_output_already_spent")
    val TransactionOutputSpentByUnexpectedInput  = ErrorCode("transaction_output_spent_by_unexpected_input")
    val ParentTransactionNotFound      = ErrorCode("parent_transaction_not_found")

    // Net Layer
    // HeadersMessageHandler
    val NonContinuousBlockHeaders      = ErrorCode("non_continuous_block_headers")
    val NoCoinForBlockSigning          = ErrorCode("no_coin_for_block_signing")

    val BusyWithInitialBlockDownload   = ErrorCode("busy_with_initial_block_download")
  }
}


abstract class ExceptionWithErrorCode() : Exception() {
  abstract val code : ErrorCode
}

class UnsupportedFeature(override val code : ErrorCode) : ExceptionWithErrorCode()

/**
 * Created by kangmo on 11/2/15.
 */
class GeneralException(override val code:ErrorCode) : ExceptionWithErrorCode()

class FatalException(override val code:ErrorCode) : ExceptionWithErrorCode()

class ScriptEvalException(override val code:ErrorCode, override val message : String = "") : ExceptionWithErrorCode()

class ScriptParseException(override val code:ErrorCode) : ExceptionWithErrorCode()

class ProtocolCodecException(override val code : ErrorCode, override val message : String = "") : ExceptionWithErrorCode()

class HttpRequestException(override val code:ErrorCode, httpCode : Int, reponse : String ) : ExceptionWithErrorCode()

class TransactionStorageException(override val code : ErrorCode) : ExceptionWithErrorCode()

class BlockStorageException(override val code : ErrorCode) : ExceptionWithErrorCode()

class ChainException(override val code : ErrorCode, override val message : String = "") : ExceptionWithErrorCode()

class NetException(override val code : ErrorCode, override val message : String = "") : ExceptionWithErrorCode()

class WalletException(override val code : ErrorCode) : ExceptionWithErrorCode()

class TransactionVerificationException(override val code:ErrorCode, override val message : String = "", val stackTraceElements : Array<StackTraceElement> = arrayOf(), var debuggingInfo : Any? = null) : ExceptionWithErrorCode()
{
  override fun toString() =
    "TransactionVerificationException($code, $message, ${stackTraceElements.joinToString(",\n")})"

}

class TransactionSignException(override val code:ErrorCode, override val message : String = "") : ExceptionWithErrorCode()

class BlockVerificationException(override val code : ErrorCode) : ExceptionWithErrorCode()

class RpcException(override val code :ErrorCode, override val message : String = "") : ExceptionWithErrorCode()
