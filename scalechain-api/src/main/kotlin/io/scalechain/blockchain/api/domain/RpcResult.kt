package io.scalechain.blockchain.api.domain
import com.google.gson.*

interface RpcResult
// Return JsNull. (for submitblock)
//data class NullResult() : RpcResult
data class StringResult(val value : String) : RpcResult
data class StringListResult(val value : List<String>) : RpcResult
data class NumberResult(val value : java.math.BigDecimal) : RpcResult

data class RpcResponse(val result : RpcResult?, val error : RpcError?, val id : Long)

