package io.scalechain.blockchain.api.command

import io.scalechain.util.StackUtil
import io.scalechain.blockchain.{ErrorCode, ExceptionWithErrorCode}
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}
import org.slf4j.LoggerFactory

trait RpcCommand {
  private val logger = LoggerFactory.getLogger(classOf[RpcCommand])

  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]]
  def help() : String

  def handlingException( block : => Either[RpcError, Option[RpcResult]]) : Either[RpcError, Option[RpcResult]] = {
    try {
      block
    } catch {
      case e : ExceptionWithErrorCode => {
        logger.error(s"ExceptionWithErrorCode, while executing a command. exception : $e, message : ${e.message}, occurred at : ${StackUtil.getStackTrace(e)}")
        val rpcError = RpcCommand.ERROR_MAP(e.code)
        Left(RpcError( rpcError.code, rpcError.messagePrefix, e.getMessage))
      }
      // In case any error happens, return as RpcError.
      case e : Exception => {
        logger.error(s"Internal Error, while executing a command. exception : $e, message : ${e.getMessage}, occurred at : ${StackUtil.getStackTrace(e)}")
        val rpcError = RpcError.RPC_INTERNAL_ERROR
        Left(RpcError( rpcError.code, rpcError.messagePrefix, e.getMessage))
      }
      // AssertionError is an Error, not an Exception, so it is not catched here.
    }
  }
}

object RpcCommand {
  // TODO : Make sure the conversion is compatible with Bitcoin core implementation
  // to achieve protocol level compatability with Bitcoin.
  val ERROR_MAP = Map(
    ErrorCode.RpcRequestParseFailure -> RpcError.RPC_INVALID_REQUEST,
    ErrorCode.RpcParameterTypeConversionFailure -> RpcError.RPC_INVALID_PARAMETER,
    ErrorCode.RpcMissingRequiredParameter -> RpcError.RPC_INVALID_REQUEST,
    ErrorCode.RpcArgumentLessThanMinValue -> RpcError.RPC_INVALID_PARAMETER,
    ErrorCode.RpcArgumentGreaterThanMaxValue -> RpcError.RPC_INVALID_PARAMETER,
    ErrorCode.RpcInvalidAddress -> RpcError.RPC_INVALID_ADDRESS_OR_KEY,
    ErrorCode.RpcInvalidKey     -> RpcError.RPC_INVALID_ADDRESS_OR_KEY,
    // used by signrawtranasction : while decoding the raw tranasction parameter.
    ErrorCode.RemainingNotEmptyAfterDecoding -> RpcError.RPC_DESERIALIZATION_ERROR,
    ErrorCode.DecodeFailure  -> RpcError.RPC_DESERIALIZATION_ERROR,
    // used by getblockheight.
    // TODO : Bitcoin Compatibility : Need to use the same RPC error when the height parameter has an invalid value.
    ErrorCode.InvalidBlockHeight -> RpcError.RPC_INVALID_PARAMS
  )
}