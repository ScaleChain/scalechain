package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.domain._
import org.scalatest.{Suite, ShouldMatchers, FlatSpec}
import spray.json.JsString

/**
  * Created by mijeong on 2016. 3. 7..
  */
class GetNewAddressSpec extends FlatSpec with ShouldMatchers {
  this: Suite =>

  "invoke" should "return Some RpcResult if the parameter number is one" in {

    val jsonrpcValue = "1.0"
    val id = "test"
    val method = "getnewaddress"
    val params = RpcParams(List(JsString("test")))

    val request = RpcRequest(jsonrpcValue, id, method, params)

    GetNewAddress.invoke(request) shouldBe a [Right[Option[RpcResult], Option[RpcResult]]]
  }

  "invoke" should "return RpcError if the parameter is invalid" in {

    val jsonrpcValue = "1.0"
    val id = "test"
    val method = "getnewaddress"
    val params = RpcParams(List(JsString("*")))

    val request = RpcRequest(jsonrpcValue, id, method, params)

    GetNewAddress.invoke(request) shouldBe a [Left[RpcError, RpcError]]
  }

}
