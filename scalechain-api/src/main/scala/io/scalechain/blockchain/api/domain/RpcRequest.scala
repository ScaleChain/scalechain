package io.scalechain.blockchain.api.domain

// BUGBUG : A Int value may come as an element of params.
// Ex> ["abc", 1]
case class RpcRequest(jsonrpc:Option[String], id:Long, method:String, params:RpcParams)
