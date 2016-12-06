package io.scalechain.blockchain.api

import io.scalechain.blockchain.api.command.*
import io.scalechain.blockchain.api.command.blockchain.p1.GetTxOut
import io.scalechain.blockchain.api.command.control.p1.GetInfo
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcResponse
import io.scalechain.blockchain.api.domain.RpcRequest
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right

interface ServiceDispatcher {

  // A map from Json-Rpc method to the actual JsonRpcService object that handles it.
  fun dispatch(request : RpcRequest) : RpcResponse {
    //logger.info(s"RPC request : ${request}")
    //println(s"RPC request : ${request}")

    val methodName = request.method
    val serviceOption = Services.serviceByCommand.get(methodName)

    if (serviceOption != null) {
      val serviceResult = serviceOption.invoke(request)
      when {
        serviceResult is Left -> {
          return RpcResponse(
            result = null,
            error = serviceResult.value,
            id = request.id
          )
        }
        serviceResult is Right -> {
          return RpcResponse(
              result = serviceResult.value,
              error = null,
              id = request.id
          )
        }
        else -> throw AssertionError()
      }
    } else {
      return RpcResponse(
        result = null,
        error = RpcError(
          code = RpcError.RPC_METHOD_NOT_FOUND.code,
          message = RpcError.RPC_METHOD_NOT_FOUND.messagePrefix,
          data = methodName
        ),
        id = request.id
      )
    }
  }
  companion object {
    private val logger = LoggerFactory.getLogger(ServiceDispatcher::class.java)
  }
}


