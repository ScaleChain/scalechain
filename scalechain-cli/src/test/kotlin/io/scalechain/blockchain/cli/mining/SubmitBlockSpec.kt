package io.scalechain.blockchain.cli.mining

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.mining.SubmitBlock
import io.scalechain.blockchain.api.command.rawtx.SendRawTransaction
import io.scalechain.blockchain.api.command.rawtx.SignRawTransaction
import io.scalechain.blockchain.api.command.rawtx.SignRawTransactionResult
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.StringListResult
import io.scalechain.blockchain.api.domain.StringResult
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.TransactionSampleData
import io.scalechain.blockchain.cli.APITestSuite
import io.scalechain.blockchain.net.handler.TxMessageHandler
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.script.hash
import org.junit.runner.RunWith
import io.scalechain.blockchain.proto.codec.BlockCodec
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.transaction.SignedTransaction
import io.scalechain.util.HexUtil
import io.scalechain.util.HexUtil.bytes
import io.scalechain.util.HexUtil.hex

/**
  * Created by kangmo on 11/2/15.
  */

// The test does not pass yet. Will make it pass soon.
@RunWith(KTestJUnitRunner::class)
class SubmitBlockSpec : APITestSuite() {

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

    "SubmitBlock" should "put the first block" {
      val B = Data.Block

      val rawBlockData = JsonPrimitive(hex(BlockCodec.encode(B.BLK01)))
      val parameters = JsonObject()
      val response = invoke(SubmitBlock, listOf(rawBlockData, parameters))
      response.right() shouldBe null
    }

    "SubmitBlock" should "put the second block" {
      val B = Data.Block

      val rawBlockData = JsonPrimitive(hex(BlockCodec.encode(B.BLK02)))
      val parameters = JsonObject()
      val response = invoke(SubmitBlock, listOf(rawBlockData, parameters))
      response.right() shouldBe null
    }

    "SubmitBlock" should "put the third block" {
      val B = Data.Block

      val rawBlockData = JsonPrimitive(hex(BlockCodec.encode(B.BLK03)))
      val parameters = JsonObject()
      val response = invoke(SubmitBlock, listOf(rawBlockData, parameters))
      response.right() shouldBe null
    }

    "SubmitBlock" should "get 'duplicate' for the duplicate block." {
      val B = Data.Block

      // Send BLK03 once more.
      val rawBlockData = JsonPrimitive(hex(BlockCodec.encode(B.BLK03)))
      val parameters = JsonObject()
      val response = invoke(SubmitBlock, listOf(rawBlockData, parameters))
      val result = response.right()!! as StringResult
      result shouldBe StringResult("duplicate")
    }

    "SubmitBlock" should "return an error if no parameter was specified." {
      val response = invoke(SubmitBlock)
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }

    "SendRawTransaction" should "send a serialized transaction. (Without allowHighFees argument)" {
      val T = Data.Tx
      val tx = T.TX04_01.transaction
      val serializedTx = JsonPrimitive( hex(TransactionCodec.encode( tx )))
      val response = invoke(SendRawTransaction, listOf( serializedTx ))
      val result = response.right()!! as StringResult

      // The result should have the transaction hash.
      result.value shouldBe hex(tx.hash().value.array)
    }

    "SendRawTransaction" should "send a serialized transaction. (with allowHighFees argument true)" {
      val T = Data.Tx
      val tx = T.TX04_02.transaction
      val serializedTx = JsonPrimitive( hex(TransactionCodec.encode(tx)))
      val response = invoke(SendRawTransaction, listOf( serializedTx, JsonPrimitive(true)))
      val result = response.right()!! as StringResult

      // The result should have the transaction hash.
      result.value shouldBe hex(tx.hash().value.array)
    }

    "SendRawTransaction" should "send a serialized transaction. (with allowHighFees argument false)" {
      val T = Data.Tx
      val tx = T.TX04_03.transaction
      val serializedTx = JsonPrimitive( hex(TransactionCodec.encode(tx)))
      val response = invoke(SendRawTransaction, listOf( serializedTx, JsonPrimitive(false)))
      val result = response.right()!! as StringResult

      // The result should have the transaction hash.
      result.value shouldBe hex(tx.hash().value.array)
    }

    "SendRawTransaction" should "send two serialized transactions. " {
      val T = Data.Tx
      val tx1 = T.TX04_04.transaction
      val tx2 = T.TX04_05_01.transaction
      val serializedTxs = JsonPrimitive( hex(TransactionCodec.encode(tx1) + TransactionCodec.encode(tx2)))
      val response = invoke(SendRawTransaction, listOf( serializedTxs ))
      val result = response.right()!! as StringListResult

      // The result should have the transaction hash.
      result.value shouldBe listOf( hex(tx1.hash().value.array), hex(tx2.hash().value.array) )
    }


    "SendRawTransaction" should "return an error if no parameter was specified." {
      val response = invoke(SendRawTransaction)
      println("response.right() => " + response.right())
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }

  }
}
