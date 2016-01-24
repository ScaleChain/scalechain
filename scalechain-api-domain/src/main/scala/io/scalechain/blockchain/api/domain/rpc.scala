package io.scalechain.blockchain.api.domain

trait RpcResult

case class RpcRequest(jsonrpc:String, id:String, method:String, params:Array[AnyRef])

// TODO : Make sure the format matches the one used by Bitcoin.
case class RpcError(code : Int, message : String, data : String)
object RpcError {
  case class ErrorDescription( code : Int, message : String)
  val METHOD_NOT_FOUND = ErrorDescription(1000, "Method not found")
}

case class RpcResponse(result : Option[RpcResult], error:Option[RpcError], id:String)
