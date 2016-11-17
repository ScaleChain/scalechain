package io.scalechain.blockchain.cli.command

import java.io.IOException

import io.scalechain.blockchain.{ErrorCode, HttpRequestException}
import io.scalechain.util.HttpRequester

//import io.scalechain.util.HttpRequester
import spray.json._


// BUGBUG : We need to be able to pass Int, scala.math.BigDecimal, a json object to params array.
case class RpcRequest(jsonrpc:String, id:Int, method:String, params:Array[String])

object RpcInvoker extends DefaultJsonProtocol {
  // BUGBUG : This code is too dirty. Make it cleaner.
  def invoke(method : String, args : Array[String], host : String, port : Int, user : String, password : String) : String = {


    val rpcRequest = RpcRequest(
      jsonrpc = "1.0", id = 1, method = method, params = args
    )

    // BUGBUG : Find a better way to serialize scala values.
/*
    implicit object ScalaValueFormat extends RootJsonFormat[AnyRef] {
      def write(value : Any) = value match {
        case v : String => v.toJson
        case v : Int => v.toJson
        case _ => throw new IllegalArgumentException()
      }
      def read(value :JsValue) = ""
    }
  */
    implicit val implcitJsonRpcRequest = jsonFormat4(RpcRequest.apply)

    val jsonRequest = (rpcRequest.toJson).toString

    val result = HttpRequester.post(s"http://$host:$port/", jsonRequest, user, password)

    result
  }
}


