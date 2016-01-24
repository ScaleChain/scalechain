package io.scalechain.blockchain.cli.api

import java.io.IOException

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import io.scalechain.blockchain.{HttpRequestException, ErrorCode}
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.util.HttpRequester
import spray.json._


object RpcInvoker extends SprayJsonSupport with DefaultJsonProtocol {
  // BUGBUG : This code is too dirty. Make it cleaner.
  def invoke(method : String, args : Array[String], host : String, port : Int, user : String, password : String) : String = {
    val rpcRequest = RpcRequest(
      jsonrpc = "1.0", id = "1", method = method, params = args
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


