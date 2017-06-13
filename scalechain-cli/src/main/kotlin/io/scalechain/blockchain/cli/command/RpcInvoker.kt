package io.scalechain.blockchain.cli.command

import io.scalechain.blockchain.api.Json
import io.scalechain.util.HttpRequester


// BUGBUG : We need to be able to pass Int, java.math.BigDecimal, a json object to params array.
data class RpcRequest(val jsonrpc:String, val id:Int, val method:String, val params:Array<String>)

object RpcInvoker {
  // BUGBUG : This code is too dirty. Make it cleaner.
  fun invoke(method : String, args : Array<String>, host : String, port : Int, user : String, password : String) : String {


    val rpcRequest = RpcRequest(
      jsonrpc = "1.0", id = 1, method = method, params = args
    )

    // BUGBUG : Find a better way to serialize scala values.
/*
    implicit object ScalaValueFormat : RootJsonFormat<AnyRef> {
      fun write(value : Any) = value match {
        case v : String => v.toJson
        case v : Int => v.toJson
        case _ => throw IllegalArgumentException()
      }
      fun read(value :JsValue) = ""
    }
  */
    val jsonRequest = Json.get().toJson(rpcRequest)

    val result = HttpRequester.post("http://$host:$port/", jsonRequest, user, password)

    return result
  }
}


