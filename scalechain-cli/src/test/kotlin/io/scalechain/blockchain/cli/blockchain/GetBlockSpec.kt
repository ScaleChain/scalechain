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
      result shouldBe StringResult("0100000000000000000000000000000000000000000000000000000000000000000000003ba3edfd7a7b12b27ac72c3e67768f617fc81bc3888a51323a9fb8aa4b1e5e4adae5494dffff001d1aa4ae180101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff4d04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73ffffffff0100f2052a01000000434104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac00000000")
    }

    "GetBlock" should "return an error if the block hash is not found." {
      val NON_EXISTENT_BLOCK_HASH = GENESIS_BLOCK_HASH.replace('a', 'f')
      val response = invoke(GetBlock, listOf(JsonPrimitive(NON_EXISTENT_BLOCK_HASH), JsonPrimitive(false)))

      val result = response.right()
      result shouldBe null // JSON null if an error occurred.
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
