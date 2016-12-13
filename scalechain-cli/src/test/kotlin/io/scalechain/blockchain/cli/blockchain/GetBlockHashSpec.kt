package io.scalechain.blockchain.cli.blockchain

import com.google.gson.JsonPrimitive
import io.scalechain.blockchain.api.command.blockchain.GetBlockHash
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.cli.APITestSuite

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
class GetBlockHashSpec : APITestSuite()  {

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

  init {
    // The test does not pass yet. Will make it pass soon.
    "GetBlockHash" should "return the genesis block hash if the height argument is 0" {
      val response = invoke(GetBlockHash, listOf(JsonPrimitive(GENESIS_BLOCK_HEIGHT)))
      (response.right()!! as StringResult).value shouldBe GENESIS_BLOCK_HASH
    }

    "GetBlockHash" should "return an error if the height is a negative value." {
      val response = invoke(GetBlockHash, listOf(JsonPrimitive(-1)))
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
    }

    "GetBlockHash" should "return an error if the height is a too big value." {
      val response = invoke(GetBlockHash, listOf(JsonPrimitive(Integer.MAX_VALUE)))
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
    }

    "GetBlockHash" should "return an error if no parameter was specified." {
      val response = invoke(GetBlockHash)
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }
  }
}
