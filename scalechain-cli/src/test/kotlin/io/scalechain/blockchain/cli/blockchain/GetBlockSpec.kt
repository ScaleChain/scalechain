package io.scalechain.blockchain.cli.blockchain

import com.google.gson.JsonPrimitive
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.blockchain.GetBlockResult
import io.scalechain.blockchain.api.command.blockchain.GetBlock
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.cli.APITestSuite
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
@RunWith(KTestJUnitRunner::class)
class GetBlockSpec : APITestSuite()  {

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
    "GetBlock" should "return a GetBlockResult for a genesis block ( format = true )" {
      val response = invoke(GetBlock, listOf(JsonPrimitive(GENESIS_BLOCK_HASH), JsonPrimitive(true)))
      val result = response.right()!! as GetBlockResult
      result.previousblockhash!! shouldBe ALL_ZERO_HASH
    }

    "GetBlock" should "return a GetBlockResult for a genesis block ( format = false )" {
      val response = invoke(GetBlock, listOf(JsonPrimitive(GENESIS_BLOCK_HASH), JsonPrimitive(false)))
      val result = response.right()!! as StringResult
      result shouldBe "COPY-PAST-DATA"
    }

    "GetBlock" should "return an error if the block hash is not found." {
      val NON_EXISTENT_BLOCK_HASH = GENESIS_BLOCK_HASH.replace('a', 'f')
      val response = invoke(GetBlock, listOf(JsonPrimitive(NON_EXISTENT_BLOCK_HASH), JsonPrimitive(false)))
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
    }


    "GetBlock" should "return an error if the block hash is invalid." {
      val INVALID_BLOCK_HASH = "a"
      val response = invoke(GetBlock, listOf(JsonPrimitive(INVALID_BLOCK_HASH), JsonPrimitive(false)))
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
    }

    "GetBlock" should "return an error if no parameter was specified." {
      val response = invoke(GetBlock)
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }
  }
}
