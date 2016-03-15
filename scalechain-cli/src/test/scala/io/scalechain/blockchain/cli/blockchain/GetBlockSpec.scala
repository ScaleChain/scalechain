package io.scalechain.blockchain.cli.blockchain

import io.scalechain.blockchain.api.command.blockchain.{GetBlockHash, GetBlockResult, GetBlock}
import io.scalechain.blockchain.api.domain.{RpcError, StringResult}
import io.scalechain.blockchain.cli.APITestSuite
import org.scalatest._
import spray.json.{JsBoolean, JsString, JsNumber}

/**
  * Created by kangmo on 11/2/15.
  */
class GetBlockSpec extends FlatSpec with BeforeAndAfterEach with APITestSuite {
  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  "GetBlock" should "return a GetBlockResult for a genesis block ( format = true )" in {
    val response = invoke(GetBlock, List(JsString(GENESIS_BLOCK_HASH), JsBoolean(true)))
    val result = response.right.get.get.asInstanceOf[GetBlockResult]
    result.previousblockhash.get shouldBe ALL_ZERO_HASH
  }

  "GetBlock" should "return a GetBlockResult for a genesis block ( format = false )" in {
    val response = invoke(GetBlock, List(JsString(GENESIS_BLOCK_HASH), JsBoolean(false)))
    val result = response.right.get.get.asInstanceOf[StringResult]
    result shouldBe "COPY-PAST-DATA"
  }

  "GetBlock" should "return an error if the block hash is not found." in {
    val NON_EXISTENT_BLOCK_HASH = GENESIS_BLOCK_HASH.replace('a','f')
    val response = invoke(GetBlock, List(JsString(NON_EXISTENT_BLOCK_HASH), JsBoolean(false)))
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
  }


  "GetBlock" should "return an error if the block hash is invalid." in {
    val INVALID_BLOCK_HASH = "a"
    val response = invoke(GetBlock, List(JsString(INVALID_BLOCK_HASH), JsBoolean(false)))
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
  }

  "GetBlock" should "return an error if no parameter was specified." in {
    val response = invoke(GetBlock)
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
  }

}
