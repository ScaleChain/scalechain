package io.scalechain.blockchain.api

import io.scalechain.blockchain.api.command._
import io.scalechain.blockchain.api.command.blockchain.GetTxOut
import io.scalechain.blockchain.api.command.control.GetInfo
import io.scalechain.blockchain.api.domain.{RpcResult, RpcError, RpcResponse, RpcRequest}
import spray.json._

trait ServiceDispatcher {
  // A map from Json-Rpc method to the actual JsonRpcService object that handles it.
  val serviceMap = Map[String, RpcCommand](
    ("getinfo" -> GetInfo),
    ("gettxout" -> GetTxOut)
  )
  def dispatch(request : RpcRequest) : RpcResponse = {

    val methodName = request.method
    val serviceOption = serviceMap.get(methodName)
    if (serviceOption.isDefined) {
      val serviceResult = serviceOption.get.invoke(request)
      RpcResponse(
        result = Some(serviceResult),
        error = None,
        id = request.id
      )
    } else {
      RpcResponse(
        result = None,
        error = Some(RpcError(
          code = RpcError.METHOD_NOT_FOUND.code,
          message = RpcError.METHOD_NOT_FOUND.message,
          data = methodName
        )),
        id = request.id
      )
    }
  }
}


