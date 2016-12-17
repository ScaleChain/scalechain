package io.scalechain.blockchain.cli.rawtx

import com.google.gson.JsonPrimitive
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.rawtx.*
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.cli.APITestSuite
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.TransactionInput
import io.scalechain.util.Bytes
import org.junit.runner.RunWith
import java.math.BigDecimal

/**
  * Created by kangmo on 11/2/15.
  */
// The test does not pass yet. Will make it pass soon.
@RunWith(KTestJUnitRunner::class)
class DecodeRawTransactionSpec : APITestSuite() {

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
    "DecodeRawTransaction" should "should decode a serialized transaction data." {
      val SERIALIZED_TX = JsonPrimitive("0100000001268a9ad7bfb21d3c086f0ff28f73a064964aa069ebb69a9e437da85c7e55c7d7000000006b483045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326ffffffff0350ac6002000000001976a91456847befbd2360df0e35b4e3b77bae48585ae06888ac80969800000000001976a9142b14950b8d31620c6cc923c5408a701b1ec0a02088ac002d3101000000001976a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac00000000")
      val response = invoke(DecodeRawTransaction, listOf(SERIALIZED_TX))

      val result = response.right()!! as DecodedRawTransaction

      // BUGBUG The expected output is copied from the test result. Need to make sure that it is correct.
      result shouldBe
        DecodedRawTransaction(
          txid=Hash(Bytes.from("ef7c0cbf6ba5af68d2ea239bba709b26ff7b0b669839a63bb01c2cb8e8de481e")),
          version=1,
          locktime=0,
          vin=listOf<RawTransactionInput>(
            RawNormalTransactionInput(
              txid=Hash(Bytes.from("d7c7557e5ca87d439e9ab6eb69a04a9664a0738ff20f6f083c1db2bfd79a8a26")),
              vout=0,
              scriptSig=
              RawScriptSig(
                hex="483045022100ee69171016b7dd218491faf6e13f53d40d64f4b40123a2de52560feb95de63b902206f23a0919471eaa1e45a0982ed288d374397d30dff541b2dd45a4c3d0041acc0012103a7c1fd1fdec50e1cf3f0cc8cb4378cd8e9a2cee8ca9b3118f3db16cbbcf8f326"
              ),
              sequence=4294967295)
          ),
          vout=listOf<RawTransactionOutput>(
            RawTransactionOutput(
              value= BigDecimal(39890000),
              n=0,
              scriptPubKey=RawScriptPubKey(hex="76a91456847befbd2360df0e35b4e3b77bae48585ae06888ac")
            ),
            RawTransactionOutput(
              value=BigDecimal(10000000),
              n=1,
              scriptPubKey=RawScriptPubKey(hex="76a9142b14950b8d31620c6cc923c5408a701b1ec0a02088ac")),
            RawTransactionOutput(
              value=BigDecimal(20000000),
              n=2,
              scriptPubKey=RawScriptPubKey(hex="76a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac")
            )
          )
        )

    }

    "DecodeRawTransaction" should "return an error if no parameter was specified." {
      val response = invoke(DecodeRawTransaction)
      val result = response.left()!!
      result.code shouldBe RpcError.RPC_INVALID_REQUEST.code
    }
  }
}
