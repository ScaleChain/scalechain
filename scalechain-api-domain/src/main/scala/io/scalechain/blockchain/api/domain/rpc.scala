package io.scalechain.blockchain.api.domain


case class RpcParams(paramValues:List[Any]) {
/*
  def get(index : Int) = paramValues(index)
  def getString(index : Int) =
  def getLong(index : Int)
  def getInt(index : Int)
  def getBigDeceimal(index : Int)
  def getObject[T](index : Int)
*/
}

// BUGBUG : A Int value may come as an element of params.
// Ex> ["abc", 1]
case class RpcRequest(jsonrpc:String, id:String, method:String, params:RpcParams)


// TODO : Make sure the format matches the one used by Bitcoin.
case class RpcError(code : Int, message : String, data : String)
object RpcError {
  case class ErrorDescription( code : Int, message : String)
  val METHOD_NOT_FOUND = ErrorDescription(1000, "Method not found")
}

trait RpcResult

case class StringResult(value : String) extends RpcResult
case class NumberResult(value : scala.math.BigDecimal) extends RpcResult

case class RpcResponse(result : Option[RpcResult], error:Option[RpcError], id:String)
