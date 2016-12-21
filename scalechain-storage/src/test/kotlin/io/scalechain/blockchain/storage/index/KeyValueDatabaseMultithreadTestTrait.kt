package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.codec.FileNumberCodec
import io.scalechain.test.ShouldSpec

/**
 * Created by kangmo on 21/12/2016.
 */
interface KeyValueDatabaseMultithreadTestTrait : ShouldSpec, KeyValueCommonTrait {
  var db: KeyValueDatabase

  fun addTests() {
    "getObject(rawKey)" should "return the value which was put even within a different thread" {
      val C = FileNumberCodec

      db.getObject(C, B("k1")) shouldBe null
      db.putObject(C, B("k1"), FileNumber(1))

      class GetValue : Runnable {
        lateinit var value : FileNumber
        override fun run() {
          value = db.getObject(C, B("k1"))!!
        }
      }
      val getValue = GetValue()

      val thread = Thread( getValue )
      thread.start()
      thread.join()

      // BUGBUG : An uncomitted transaction's data should not be able to seen by other threads, but it is. Need to check.
      // This test should fail for TransactingRocksDatabase and TransactingMapDatabase (transaction is committed in afterEach), but it passes.
      getValue.value shouldBe FileNumber(1)
    }
  }
}