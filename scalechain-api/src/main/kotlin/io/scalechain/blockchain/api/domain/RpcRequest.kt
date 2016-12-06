package io.scalechain.blockchain.api.domain

// BUGBUG : A Int value may come as an element of params.
// Ex> <"abc", 1>
data class RpcRequest(val jsonrpc:String?, val id:Long, val method:String, val params:RpcParams)
