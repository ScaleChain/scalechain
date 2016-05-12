package io.scalechain.blockchain.cli.blockchain

import io.scalechain.blockchain.api.command.blockchain.{GetBlock, GetBlockHash, GetBestBlockHash}
import io.scalechain.blockchain.api.domain.{RpcError, StringResult}
import io.scalechain.blockchain.cli.APITestSuite
import org.scalatest._
import spray.json.JsNumber

/**
  * Created by kangmo on 11/2/15.
  */
class GetBlockHashSpec extends FlatSpec with BeforeAndAfterEach with APITestSuite {
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

  // The test does not pass yet. Will make it pass soon.
  /*
  "GetBlockHash" should "return the genesis block hash if the height argument is 0" in {
    val response = invoke(GetBlockHash, List(JsNumber(GENESIS_BLOCK_HEIGHT)))
    val result = response.right.get.get.asInstanceOf[StringResult].value shouldBe GENESIS_BLOCK_HASH
  }

  "GetBlockHash" should "return an error if the height is a negative value." in {
    val response = invoke(GetBlockHash, List(JsNumber(-1)))
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
  }

  "GetBlockHash" should "return an error if the height is a too big value." in {
    val response = invoke(GetBlockHash, List(JsNumber(Integer.MAX_VALUE)))
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_PARAMETER.code
  }

  "GetBlockHash" should "return an error if no parameter was specified." in {
    val response = invoke(GetBlockHash)
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
  }
  */
}
