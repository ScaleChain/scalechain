package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.{ExceptionWithErrorCode, ErrorCode}
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

trait RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, Option[RpcResult]]
  def help() : String

  def handlingException( block : => Either[RpcError, Option[RpcResult]]) : Either[RpcError, Option[RpcResult]] = {
    try {
      block
    } catch {
      case e : ExceptionWithErrorCode => {
        val rpcError = RpcCommand.ERROR_MAP(e.code)
        Left(RpcError( rpcError.code, rpcError.messagePrefix, e.message))
      }
      // In case any error happens, return as RpcError.
      case e : Throwable => {
        val rpcError = RpcError.RPC_INTERNAL_ERROR
        Left(RpcError( rpcError.code, rpcError.messagePrefix, e.getMessage))
      }
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
    ErrorCode. RpcArgumentGreaterThanMaxValue -> RpcError.RPC_INVALID_PARAMETER,
    ErrorCode.RpcInvalidAddress -> RpcError.RPC_INVALID_ADDRESS_OR_KEY
  )
}