package io.scalechain.blockchain.cli.rawtx

import com.google.gson.JsonPrimitive
import io.scalechain.blockchain.api.command.rawtx.RawTransaction
import io.scalechain.blockchain.api.command.rawtx.GetRawTransaction
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.cli.APITestSuite

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
class GetRawTransactionSpec : APITestSuite() {

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  // The test does not pass yet. Will make it pass soon.
  val TRANSACTION_ID = JsonPrimitive("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b")

  init {
    "GetRawTransaction" should "return a serialized transaction if the Verose(2nd) parameter was 0" {
      val response = invoke(GetRawTransaction, listOf(TRANSACTION_ID, JsonPrimitive(0)))
      val result = response.right()!! as StringResult
    }

    "GetRawTransaction" should "return a serialized transaction if the Verose(2nd) parameter was 1" {
      val response = invoke(GetRawTransaction, listOf(TRANSACTION_ID, JsonPrimitive(1)))
      val result = response.right()!! as RawTransaction
      // TODO : Copy-paste the transaction object from unittest output.
      result shouldBe null
    }

    "GetRawTransaction" should "return an error if no parameter was specified." {
      val response = invoke(GetRawTransaction)
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }
  }
}
