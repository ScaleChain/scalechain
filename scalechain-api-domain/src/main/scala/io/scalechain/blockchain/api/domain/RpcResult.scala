package io.scalechain.blockchain.api.domain

trait RpcResult
case class StringResult(value : String) extends RpcResult
case class NumberResult(value : scala.math.BigDecimal) extends RpcResult



case class RpcResponse(result : Option[RpcResult], error:Option[RpcError], id:String)

