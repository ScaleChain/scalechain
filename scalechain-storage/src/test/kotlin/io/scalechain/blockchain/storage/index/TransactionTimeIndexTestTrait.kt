package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.CStringPrefixed
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.blockchain.storage.TransactionPoolIndex
import io.scalechain.blockchain.storage.TransactionTimeIndex
import io.scalechain.blockchain.storage.test.TestData
import io.scalechain.test.ShouldSpec
import io.scalechain.test.ShouldSpec.Companion.shouldThrow
import org.junit.runner.RunWith

/**
 * Created by kangmo on 15/12/2016.
 */
@RunWith(KTestJUnitRunner::class)
interface TransactionTimeIndexTestTrait : ShouldSpec, KeyValueCommonTrait, ProtoTestData {
  var db: KeyValueDatabase

  fun times() = listOf( object : TransactionTimeIndex {},
    object : TransactionTimeIndex {
      override fun getTxTimePrefix() = DB.TEMP_TRANSACTION_TIME
    } )

  fun addTests() {
    "getOldestTransactionHashes" should "get transactions that has been put" {
      times().forEach { time ->
        time.getOldestTransactionHashes(db, 1).isEmpty() shouldBe true

        time.putTransactionTime(db, 1, TestData.dummyHash(1))

        val oldestTxHashes = time.getOldestTransactionHashes(db, 1)
        println("time.getOldestTransactionHashes(db, 1): ${oldestTxHashes.first().prefix}, ${oldestTxHashes.first().data}")

        oldestTxHashes shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(1), TestData.dummyHash(1))
        )

        time.putTransactionTime(db, 2, TestData.dummyHash(2))

        time.getOldestTransactionHashes(db, 2) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(1), TestData.dummyHash(1)),
          CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2))
        )

        val currentNanoSec = System.nanoTime()

        time.putTransactionTime(db, currentNanoSec, TestData.dummyHash(3))
        time.getOldestTransactionHashes(db, 3) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(1), TestData.dummyHash(1)),
          CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(currentNanoSec), TestData.dummyHash(3))
        )

        time.putTransactionTime(db, Long.MAX_VALUE, TestData.dummyHash(9))

        time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(1), TestData.dummyHash(1)),
          CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(currentNanoSec), TestData.dummyHash(3)),
          CStringPrefixed(TransactionTimeIndex.timeToString(Long.MAX_VALUE), TestData.dummyHash(9))
        )
      }
    }

    "delTransactionFromPool" should "delete a transaction in the pool" {

      times().forEach { time ->
        time.putTransactionTime(db, 1, TestData.dummyHash(1))
        time.putTransactionTime(db, 2, TestData.dummyHash(2))
        time.putTransactionTime(db, 3, TestData.dummyHash(3))
        time.putTransactionTime(db, 4, TestData.dummyHash(4))

        time.delTransactionTime(db, 1, TestData.dummyHash(1))

        time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(3), TestData.dummyHash(3)),
          CStringPrefixed(TransactionTimeIndex.timeToString(4), TestData.dummyHash(4))
        )

        time.delTransactionTime(db, 3, TestData.dummyHash(3))

        time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(4), TestData.dummyHash(4))
        )

        time.delTransactionTime(db, 4, TestData.dummyHash(4))

        time.getOldestTransactionHashes(db, 1) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2))
        )

        time.delTransactionTime(db, 2, TestData.dummyHash(2))

        shouldThrow<AssertionError> {
          time.getOldestTransactionHashes(db, 0)
        }
      }
    }

    "delTransactionFromPool(CStringPrefixed)" should "delete a transaction in the pool" {
      times().forEach { time ->
        time.putTransactionTime(db, 1, TestData.dummyHash(1))
        time.putTransactionTime(db, 2, TestData.dummyHash(2))
        time.putTransactionTime(db, 3, TestData.dummyHash(3))
        time.putTransactionTime(db, 4, TestData.dummyHash(4))

        time.delTransactionTime(db, CStringPrefixed(TransactionTimeIndex.timeToString(1), TestData.dummyHash(1)))

        time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(3), TestData.dummyHash(3)),
          CStringPrefixed(TransactionTimeIndex.timeToString(4), TestData.dummyHash(4))
        )

        time.delTransactionTime(db, CStringPrefixed(TransactionTimeIndex.timeToString(3), TestData.dummyHash(3)))

        time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(4), TestData.dummyHash(4))
        )

        time.delTransactionTime(db, CStringPrefixed(TransactionTimeIndex.timeToString(4), TestData.dummyHash(4)))

        time.getOldestTransactionHashes(db, 1) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2))
        )

        time.delTransactionTime(db, CStringPrefixed(TransactionTimeIndex.timeToString(2), TestData.dummyHash(2)))

        shouldThrow <AssertionError> {
          time.getOldestTransactionHashes(db, 0)
        }
      }
    }
  }
}