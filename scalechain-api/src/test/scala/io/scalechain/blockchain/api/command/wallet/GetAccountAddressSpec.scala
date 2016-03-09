package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.domain.{RpcResult, RpcError, RpcRequest, RpcParams}
import org.scalatest.{Suite, ShouldMatchers, FlatSpec}
import spray.json.JsString

/**
  * Created by mijeong on 2016. 3. 9..
  */
class GetAccountAddressSpec extends FlatSpec with ShouldMatchers {
  this: Suite =>

  "invoke" should "return Some RpcResult if the parameter exists" in {

    val jsonrpcValue = "1.0"
    val id = "test"
    val method = "getaccountaddress"
    val params = RpcParams(List(JsString("")))

    val request = RpcRequest(jsonrpcValue, id, method, params)

    GetAccountAddress.invoke(request) shouldBe a [Right[Option[RpcResult], Option[RpcResult]]]
  }

  "invoke" should "return Some RpcError if the parameter does not exist" in {

    val jsonrpcValue = "1.0"
    val id = "test"
    val method = "getaccountaddress"
    val params = RpcParams(List(null))

    val request = RpcRequest(jsonrpcValue, id, method, params)

    GetAccountAddress.invoke(request) shouldBe a [Left[RpcError, RpcError]]
  }

  "invoke" should "return RpcError if the parameter(account name) is invalid" in {

    val jsonrpcValue = "1.0"
    val id = "test"
    val method = "getaccountaddress"
    val params = RpcParams(List(JsString("*")))

    val request = RpcRequest(jsonrpcValue, id, method, params)

    GetNewAddress.invoke(request) shouldBe a [Left[RpcError, RpcError]]
  }

}
