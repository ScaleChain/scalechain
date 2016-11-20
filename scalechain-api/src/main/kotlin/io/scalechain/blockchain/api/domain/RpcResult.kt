package io.scalechain.blockchain.api.domain

trait RpcResult
// Return JsNull. (for submitblock)
//data class NullResult() : RpcResult
data class StringResult(value : String) : RpcResult
data class StringListResult(value : List<String>) : RpcResult
data class NumberResult(value : scala.math.BigDecimal) : RpcResult



data class RpcResponse(result : Option<RpcResult>, error:Option<RpcError>, id:Long)

