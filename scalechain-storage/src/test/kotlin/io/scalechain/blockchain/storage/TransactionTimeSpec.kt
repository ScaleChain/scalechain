package io.scalechain.blockchain.storage

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.CStringPrefixed
import java.io.File

import io.scalechain.blockchain.storage.TransactionTimeIndex.Companion.timeToString
import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.test.TestData.dummyHash
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactionTimeSpec  : FlatSpec(), Matchers {

  val testPath = File("./target/unittests-TransactionTimeSpec")

  lateinit var time: TransactionTimeIndex
  lateinit var db : KeyValueDatabase

  override fun beforeEach() {

    FileUtils.deleteDirectory(testPath)
    time = object : TransactionTimeIndex {}
    db = RocksDatabase(testPath)

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()

    FileUtils.deleteDirectory(testPath)
  }

  init {
    Storage.initialize()
    "getOldestTransactionHashes" should "get transactions that has been put" {
      time.getOldestTransactionHashes(db, 1).isEmpty() shouldBe true

      time.putTransactionTime(db, 1, dummyHash(1))

      time.getOldestTransactionHashes(db, 1) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(1), dummyHash(1))
      )

      time.putTransactionTime(db, 2, dummyHash(2))

      time.getOldestTransactionHashes(db, 2) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(1), dummyHash(1)),
          CStringPrefixed(TransactionTimeIndex.timeToString(2), dummyHash(2))
      )

      val currentNanoSec = System.nanoTime()

      time.putTransactionTime(db, currentNanoSec, dummyHash(3))
      time.getOldestTransactionHashes(db, 3) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(1), dummyHash(1)),
          CStringPrefixed(TransactionTimeIndex.timeToString(2), dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(currentNanoSec), dummyHash(3))
      )

      time.putTransactionTime(db, Long.MAX_VALUE, dummyHash(9))

      time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(1), dummyHash(1)),
          CStringPrefixed(TransactionTimeIndex.timeToString(2), dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(currentNanoSec), dummyHash(3)),
          CStringPrefixed(TransactionTimeIndex.timeToString(Long.MAX_VALUE), dummyHash(9))
      )
    }

    "delTransactionFromPool" should "delete a transaction in the pool" {
      time.putTransactionTime(db, 1, dummyHash(1))
      time.putTransactionTime(db, 2, dummyHash(2))
      time.putTransactionTime(db, 3, dummyHash(3))
      time.putTransactionTime(db, 4, dummyHash(4))

      time.delTransactionTime(db, 1, dummyHash(1))

      time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(3), dummyHash(3)),
          CStringPrefixed(TransactionTimeIndex.timeToString(4), dummyHash(4))
      )

      time.delTransactionTime(db, 3, dummyHash(3))

      time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(4), dummyHash(4))
      )

      time.delTransactionTime(db, 4, dummyHash(4))

      time.getOldestTransactionHashes(db, 1) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), dummyHash(2))
      )

      time.delTransactionTime(db, 2, dummyHash(2))

      shouldThrow<AssertionError> {
        time.getOldestTransactionHashes(db, 0)
      }
    }

    "delTransactionFromPool(CStringPrefixed)" should "delete a transaction in the pool" {
      time.putTransactionTime(db, 1, dummyHash(1))
      time.putTransactionTime(db, 2, dummyHash(2))
      time.putTransactionTime(db, 3, dummyHash(3))
      time.putTransactionTime(db, 4, dummyHash(4))

      time.delTransactionTime(db, CStringPrefixed(timeToString(1), dummyHash(1)))

      time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(3), dummyHash(3)),
          CStringPrefixed(TransactionTimeIndex.timeToString(4), dummyHash(4))
      )

      time.delTransactionTime(db, CStringPrefixed(timeToString(3), dummyHash(3)))

      time.getOldestTransactionHashes(db, 4) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), dummyHash(2)),
          CStringPrefixed(TransactionTimeIndex.timeToString(4), dummyHash(4))
      )

      time.delTransactionTime(db, CStringPrefixed(timeToString(4), dummyHash(4)))

      time.getOldestTransactionHashes(db, 1) shouldBe listOf(
          CStringPrefixed(TransactionTimeIndex.timeToString(2), dummyHash(2))
      )

      time.delTransactionTime(db, CStringPrefixed(timeToString(2), dummyHash(2)))

      shouldThrow <AssertionError> {
        time.getOldestTransactionHashes(db, 0)
      }
    }
  }
}