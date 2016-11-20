package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixed
import io.scalechain.blockchain.storage.index.{RocksDatabase, KeyValueDatabase}
import org.apache.commons.io.FileUtils
import io.scalechain.util.HexUtil._
import org.scalatest._

class TransactionTimeSpec  : FlatSpec with Matchers with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  fun dummyHash(num: Int) {
    assert(num >= 0 && num <= 9)
    Hash(bytes(num.toString * 64))
  }


  var time: TransactionTimeIndex = null

  val testPath = File("./target/unittests-TransactionTimeSpec")

  implicit var db : KeyValueDatabase = null

  override fun beforeEach() {

    FileUtils.deleteDirectory(testPath)
    time = TransactionTimeIndex {}
    db = RocksDatabase(testPath)

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
    db = null

    FileUtils.deleteDirectory(testPath)
  }

  "getOldestTransactionHashes" should "get transactions that has been put" in {
    time.getOldestTransactionHashes(1) shouldBe List()

    time.putTransactionTime(1, dummyHash(1))

    time.getOldestTransactionHashes(1) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(1), dummyHash(1))
    )

    time.putTransactionTime(2, dummyHash(2))

    time.getOldestTransactionHashes(2) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(1), dummyHash(1)),
      CStringPrefixed( TransactionTimeIndex.timeToString(2), dummyHash(2))
    )

    val currentNanoSec = System.nanoTime()

    time.putTransactionTime(currentNanoSec, dummyHash(3))
    time.getOldestTransactionHashes(3) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(1), dummyHash(1)),
      CStringPrefixed( TransactionTimeIndex.timeToString(2), dummyHash(2)),
      CStringPrefixed( TransactionTimeIndex.timeToString(currentNanoSec), dummyHash(3))
    )

    time.putTransactionTime(Long.MaxValue, dummyHash(9))

    time.getOldestTransactionHashes(4) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(1), dummyHash(1)),
      CStringPrefixed( TransactionTimeIndex.timeToString(2), dummyHash(2)),
      CStringPrefixed( TransactionTimeIndex.timeToString(currentNanoSec), dummyHash(3)),
      CStringPrefixed( TransactionTimeIndex.timeToString(Long.MaxValue), dummyHash(9))
    )
  }

  "delTransactionFromPool" should "delete a transaction in the pool" in {
    time.putTransactionTime(1, dummyHash(1))
    time.putTransactionTime(2, dummyHash(2))
    time.putTransactionTime(3, dummyHash(3))
    time.putTransactionTime(4, dummyHash(4))

    time.delTransactionTime( 1, dummyHash(1))

    time.getOldestTransactionHashes(4) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(2), dummyHash(2)),
      CStringPrefixed( TransactionTimeIndex.timeToString(3), dummyHash(3)),
      CStringPrefixed( TransactionTimeIndex.timeToString(4), dummyHash(4))
    )

    time.delTransactionTime( 3, dummyHash(3))

    time.getOldestTransactionHashes(4) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(2), dummyHash(2)),
      CStringPrefixed( TransactionTimeIndex.timeToString(4), dummyHash(4))
    )

    time.delTransactionTime( 4, dummyHash(4))

    time.getOldestTransactionHashes(1) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(2), dummyHash(2))
    )

    time.delTransactionTime( 2, dummyHash(2))

    an<AssertionError> shouldBe thrownBy {
      time.getOldestTransactionHashes(0)
    }
  }

  "delTransactionFromPool(CStringPrefixed)" should "delete a transaction in the pool" in {
    time.putTransactionTime(1, dummyHash(1))
    time.putTransactionTime(2, dummyHash(2))
    time.putTransactionTime(3, dummyHash(3))
    time.putTransactionTime(4, dummyHash(4))

    import TransactionTimeIndex._
    time.delTransactionTime( CStringPrefixed(timeToString(1), dummyHash(1)))

    time.getOldestTransactionHashes(4) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(2), dummyHash(2)),
      CStringPrefixed( TransactionTimeIndex.timeToString(3), dummyHash(3)),
      CStringPrefixed( TransactionTimeIndex.timeToString(4), dummyHash(4))
    )

    time.delTransactionTime( CStringPrefixed(timeToString(3), dummyHash(3)))

    time.getOldestTransactionHashes(4) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(2), dummyHash(2)),
      CStringPrefixed( TransactionTimeIndex.timeToString(4), dummyHash(4))
    )

    time.delTransactionTime( CStringPrefixed(timeToString(4), dummyHash(4)))

    time.getOldestTransactionHashes(1) shouldBe List(
      CStringPrefixed( TransactionTimeIndex.timeToString(2), dummyHash(2))
    )

    time.delTransactionTime( CStringPrefixed(timeToString(2), dummyHash(2)))

    an<AssertionError> shouldBe thrownBy {
      time.getOldestTransactionHashes(0)
    }
  }

}