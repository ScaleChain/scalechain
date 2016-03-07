package io.scalechain.blockchain.api.command.wallet

import io.scalechain.blockchain.api.domain.{RpcParams, RpcResult, RpcError, RpcRequest}
import org.scalatest.{Suite, ShouldMatchers, FlatSpec}
import spray.json.{JsString}

/**
  * Created by mijeong on 2016. 3. 7..
  */
class GetAccountSpec extends FlatSpec with ShouldMatchers {
  this: Suite =>

  "invoke" should "return Some RpcResult if the parameter exists" in {

    val jsonrpcValue = "1.0"
    val id = "test"
    val method = "getaccount"
    val params = RpcParams(List(JsString("mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN")))

    val request = RpcRequest(jsonrpcValue, id, method, params)

    GetAccount.invoke(request) shouldBe a [Either[RpcError, Option[RpcResult]]]
  }



}
