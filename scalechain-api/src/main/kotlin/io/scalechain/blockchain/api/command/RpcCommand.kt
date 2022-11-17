package io.scalechain.blockchain.api.command

import io.scalechain.util.StackUtil
import io.scalechain.blockchain.RpcException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ExceptionWithErrorCode
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.Bytes
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import org.slf4j.LoggerFactory

interface ParameterConverter {
  fun getHash(hexString : String, hashSize : Int) : Hash {
    val headerHash = Hash( Bytes.from(hexString) )
    if (headerHash.value.array.size != 32) throw RpcException(ErrorCode.RpcInvalidParameter)
    return headerHash
  }
}

abstract class RpcCommand : ParameterConverter {
  private val logger = LoggerFactory.getLogger(RpcCommand::class.java)

  abstract fun invoke(request : RpcRequest) : Either<RpcError, RpcResult?>
  abstract fun help() : String

  fun handlingException( block : (() -> Either<RpcError, RpcResult?>)) : Either<RpcError, RpcResult?> {
    try {
      return block()
    } catch(e : ExceptionWithErrorCode) {
      if (e is RpcException ) {
        // We had an invalid RPC call such as missing mandatory parameters.
        // do not log anything about this, because the cause of this exception is not ScaleChain server, but users calling RPCs.
      } else {
        logger.error("ExceptionWithErrorCode, while executing a command. exception : $e, message : ${e.message}, occurred at : ${StackUtil.getStackTrace(e)}")
      }
      val rpcError = RpcCommand.ERROR_MAP.get(e.code)
      return if (rpcError == null) {
        val rpcInternalError = RpcError.RPC_INTERNAL_ERROR
        logger.error("No mapping to RPC error for exception : $e, message : ${e.message}, occurred at : ${StackUtil.getStackTrace(e)}")
        Left(RpcError( rpcInternalError.code, rpcInternalError.messagePrefix, "[" + e.code.code + "] " + e.message ))
      } else {
        Left(RpcError(rpcError.code, rpcError.messagePrefix, "[" + e.code.code + "] " + e.message ))
      }
    } catch ( e : Exception )  { // In case any error happens, return as RpcError.
      logger.error("Internal Error, while executing a command. exception : $e, message : ${e.message}, occurred at : ${StackUtil.getStackTrace(e)}")
      val rpcError = RpcError.RPC_INTERNAL_ERROR
      return Left(RpcError( rpcError.code, rpcError.messagePrefix, e.message  ?: ""))
    }
    // AssertionError is an Error, not an Exception, so it is not catched here.
  }


  companion object {
    // TODO : Make sure the conversion is compatible with Bitcoin core implementation
    // to achieve protocol level compatability with Bitcoin.
    val ERROR_MAP = mapOf(
      ErrorCode.RpcInvalidParameter to RpcError.RPC_INVALID_PARAMETER,
      ErrorCode.RpcRequestParseFailure to RpcError.RPC_INVALID_REQUEST,
      ErrorCode.RpcParameterTypeConversionFailure to RpcError.RPC_INVALID_PARAMETER,
      ErrorCode.RpcMissingRequiredParameter to RpcError.RPC_INVALID_REQUEST,
      ErrorCode.RpcArgumentLessThanMinValue to RpcError.RPC_INVALID_PARAMETER,
      ErrorCode.RpcArgumentGreaterThanMaxValue to RpcError.RPC_INVALID_PARAMETER,
      ErrorCode.RpcInvalidAddress to RpcError.RPC_INVALID_ADDRESS_OR_KEY,
      ErrorCode.RpcInvalidKey     to RpcError.RPC_INVALID_ADDRESS_OR_KEY,
      // used by signrawtranasction : while decoding the raw tranasction parameter.
      ErrorCode.RemainingNotEmptyAfterDecoding to RpcError.RPC_DESERIALIZATION_ERROR,
      ErrorCode.DecodeFailure  to RpcError.RPC_DESERIALIZATION_ERROR,
      // used by getblockheight.
      // TODO : Bitcoin Compatibility : Need to use the same RPC error when the height parameter has an invalid value.
      ErrorCode.InvalidBlockHeight to RpcError.RPC_INVALID_PARAMETER,
      ErrorCode.InvalidTransactionOutPoint to RpcError.RPC_INVALID_PARAMETER,
      ErrorCode.ParentTransactionNotFound to RpcError.RPC_INVALID_PARAMETER,
      ErrorCode.BusyWithInitialBlockDownload to RpcError.RPC_CLIENT_IN_INITIAL_DOWNLOAD
    )
  }
}

