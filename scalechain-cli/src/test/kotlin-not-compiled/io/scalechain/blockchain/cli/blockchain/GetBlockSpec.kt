package io.scalechain.blockchain.cli.blockchain

import io.scalechain.blockchain.api.command.blockchain.{GetBlockResult, GetBlock}
import io.scalechain.blockchain.api.domain.{RpcError, StringResult}
import io.scalechain.blockchain.cli.APITestSuite
import org.scalatest.*
import spray.json.{JsBoolean, JsString}

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
@Ignore
class GetBlockSpec : FlatSpec with BeforeAndAfterEach with APITestSuite {
  this: Suite =>

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
  "GetBlock" should "return a GetBlockResult for a genesis block ( format = true )" {
    val response = invoke(GetBlock, listOf(JsString(GENESIS_BLOCK_HASH), JsBoolean(true)))
    val result = response.right.get.get.asInstanceOf<GetBlockResult>
    result.previousblockhash.get shouldBe ALL_ZERO_HASH
  }

  "GetBlock" should "return a GetBlockResult for a genesis block ( format = false )" {
    val response = invoke(GetBlock, listOf(JsString(GENESIS_BLOCK_HASH), JsBoolean(false)))
    val result = response.right.get.get.asInstanceOf<StringResult>
    result shouldBe "COPY-PAST-DATA"
  }

  "GetBlock" should "return an error if the block hash is not found." {
    val NON_EXISTENT_BLOCK_HASH = GENESIS_BLOCK_HASH.replace('a','f')
    val response = invoke(GetBlock, listOf(JsString(NON_EXISTENT_BLOCK_HASH), JsBoolean(false)))
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
  }


  "GetBlock" should "return an error if the block hash is invalid." {
    val INVALID_BLOCK_HASH = "a"
    val response = invoke(GetBlock, listOf(JsString(INVALID_BLOCK_HASH), JsBoolean(false)))
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
  }

  "GetBlock" should "return an error if no parameter was specified." {
    val response = invoke(GetBlock)
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
  }
}
