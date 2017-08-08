package io.scalechain.blockchain.cli.rawtx

import com.google.gson.JsonPrimitive
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.rawtx.*
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.cli.APITestSuite
import io.scalechain.blockchain.proto.Hash
import io.scalechain.util.Bytes
import org.junit.runner.RunWith
import java.math.BigDecimal

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
@RunWith(KTestJUnitRunner::class)
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

// This test fails in our docker environment. Not sure why.
/*
    "GetRawTransaction" should "return a serialized transaction if the Verose(2nd) parameter was 1" {
      val response = invoke(GetRawTransaction, listOf(TRANSACTION_ID, JsonPrimitive(1)))
      val result = response.right()!! as RawTransaction
      // BUGBUG : Make sure the expected transaction value is correct. It is copied from the actual result.

      // BUGBUG : confirmation field depends on other test cases. Ex> If a block was mined, the confirmation count increases.
      result shouldBe
        RawTransaction(
          hex="01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff4d04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73ffffffff0100f2052a01000000434104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac00000000",
          txid= Hash(Bytes.from("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b")),
          version=1,
          locktime=0,
          vin=listOf<RawTransactionInput>(
            RawGenerationTransactionInput(coinbase="04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73", sequence=4294967295)
          ),
          vout=listOf(
            RawTransactionOutput(
              value= BigDecimal(5000000000),
              n=0,
              scriptPubKey=RawScriptPubKey(hex="4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac")
            )
          ),
          blockhash=Hash(Bytes.from("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943")),
          confirmations=4,
          time=1296688602,
          blocktime=1296688602)
    }
*/
    "GetRawTransaction" should "return an error if no parameter was specified." {
      val response = invoke(GetRawTransaction)
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }
  }
}
