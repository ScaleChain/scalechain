package io.scalechain.blockchain.cli.rawtx

import io.scalechain.blockchain.api.command.rawtx.{RawTransaction, GetRawTransaction}
import io.scalechain.blockchain.api.domain.{RpcError, StringResult}
import io.scalechain.blockchain.cli.APITestSuite
import org.scalatest._
import spray.json.{JsNumber, JsString}

/**
  * Created by kangmo on 11/2/15.
  */
class GetRawTransactionSpec extends FlatSpec with BeforeAndAfterEach with APITestSuite {
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

  val TRANSACTION_ID = JsString("ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e")

  "GetRawTransaction" should "return a serialized transaction if the Verose(2nd) parameter was 0" in {
    val response = invoke(GetRawTransaction, List(TRANSACTION_ID, JsNumber(0)))
    val result = response.right.get.get.asInstanceOf[StringResult]
  }

  "GetRawTransaction" should "return a serialized transaction if the Verose(2nd) parameter was 0" in {
    val response = invoke(GetRawTransaction, List(TRANSACTION_ID, JsNumber(1)))
    val result = response.right.get.get.asInstanceOf[RawTransaction]
    // TODO : Copy-paste the transaction object from unittest output.
    result shouldBe None
  }

  "GetRawTransaction" should "return an error if no parameter was specified." in {
    val response = invoke(GetRawTransaction)
    val result = response.left.get
    result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
  }

}
