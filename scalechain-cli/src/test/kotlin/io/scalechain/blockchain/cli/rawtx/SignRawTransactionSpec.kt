package io.scalechain.blockchain.cli.rawtx

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.rawtx.SignRawTransaction
import io.scalechain.blockchain.api.command.rawtx.SignRawTransactionResult
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.cli.APITestSuite
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
@RunWith(KTestJUnitRunner::class)
class SignRawTransactionSpec : APITestSuite() {

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
  fun sign(transaction : Transaction) : Transaction {
    val rawTransaction = TransactionCodec.encode(transaction)
    val privateKeys = JsonArray()
    listOf(Data.Addr1, Data.Addr2, Data.Addr3).forEach{
      privateKeys.add(JsonPrimitive(it.privateKey.base58()))
    }

    val response = invoke(SignRawTransaction, listOf<JsonElement>( JsonPrimitive( HexUtil.hex(rawTransaction ) ), JsonArray()/*dependency*/, privateKeys ) )
    val result = response.right()!! as SignRawTransactionResult
    assert( result.complete )
    return TransactionCodec.decode(HexUtil.bytes(result.hex))!!
  }

  init {
    // The test does not pass yet. Will make it pass soon.
    "SignRawTransaction" should "" {
      // TODO : Implement.
    }

    "SignRawTransaction" should "return an error if no parameter was specified." {
      val response = invoke(SignRawTransaction)
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }
  }
}
