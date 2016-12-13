package io.scalechain.blockchain.cli.rawtx

import com.google.gson.JsonPrimitive
import io.scalechain.blockchain.api.command.rawtx.SendRawTransaction
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.cli.APITestSuite

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
class SendRawTransactionSpec : APITestSuite() {

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

  val SERIALIZED_TRANSACTION = JsonPrimitive("01000000011da9283b4ddf8d89eb996988b89ead56cecdc44041ab38bf787f1206cd90b51e000000006a47304402200ebea9f630f3ee35fa467ffc234592c79538ecd6eb1c9199eb23c4a16a0485a20220172ecaf6975902584987d295b8dddf8f46ec32ca19122510e22405ba52d1f13201210256d16d76a49e6c8e2edc1c265d600ec1a64a45153d45c29a2fd0228c24c3a524ffffffff01405dc600000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac0000000")

  init {
    "SendRawTransaction" should "send a serialized transaction. (Without allowHighFees argument)" {
      val response = invoke(SendRawTransaction, listOf(SERIALIZED_TRANSACTION))
      val result = response.right()!! as StringResult

      // The result should have the transaction hash.
      result.value.length shouldBe 64
    }

    "SendRawTransaction" should "send a serialized transaction. (with allowHighFees argument true)" {
      val response = invoke(SendRawTransaction, listOf(SERIALIZED_TRANSACTION, JsonPrimitive(true)))
      val result = response.right()!! as StringResult

      // The result should have the transaction hash.
      result.value.length shouldBe 64
    }

    "SendRawTransaction" should "send a serialized transaction. (with allowHighFees argument false)" {
      val response = invoke(SendRawTransaction, listOf(SERIALIZED_TRANSACTION, JsonPrimitive(false)))
      val result = response.right()!! as StringResult

      // The result should have the transaction hash.
      result.value.length shouldBe 64
    }

    "SendRawTransaction" should "return an error if no parameter was specified." {
      val response = invoke(SendRawTransaction)
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }
  }
}
