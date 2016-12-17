package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith


/**
  * Covers all methods in BlockDatabaseForRecordStorage as well as its super class, BlockDatabase
  */
@RunWith(KTestJUnitRunner::class)
class BlockDatabaseForRecordStorageSpec : FlatSpec(), Matchers {
  init {
    Storage.initialize()
  }

  lateinit var db : KeyValueDatabase
  lateinit var blockDb : BlockDatabaseForRecordStorage

  val testPath = File("./target/unittests-BlockDatabaseSpec")

  override fun beforeEach() {
    testPath.deleteRecursively()
    testPath.mkdir()

    db = DatabaseFactory.create( testPath )
    blockDb  = object : BlockDatabaseForRecordStorage {}

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
    testPath.deleteRecursively()
  }


  val BLOCK_LOCATOR = FileRecordLocator(
    fileIndex = 1,
    recordLocator = RecordLocator( 10, 15)
  )

  val DUMMY_HASH = Hash.ALL_ZERO
/*
  "putTransactions" should "successfully put transactions onto database" {
    // At first, we should not have any tranasctions on the database.
    for( transaction <- TestData.block.transactions) {
      db.getTransactionDescriptor(transaction.hash) shouldBe null
    }

    var i = 1

    // Create (transaction hash, transaction locator pair )
    val txLocators = for(
      transaction <- TestData.block.transactions;
      txLocator = BLOCK_LOCATOR.copy(recordLocator = BLOCK_LOCATOR.recordLocator.copy(offset = BLOCK_LOCATOR.recordLocator.offset + i * 100))
    ) yield {
      i += 1
      TransactionLocator(transaction.hash,txLocator)
    }

    db.putTransactions(TestData.block.transactions zip txLocators)

    // Now, we have transactions on the database.
    for( transaction <- TestData.block.transactions) {
      val txDesc = db.getTransactionDescriptor(transaction.hash)

      assert(txDesc.isDefined)
      // The block file number should be same for the transaction locator and block locator, as the transaction is in the block.
      txDesc.get.transactionLocatorOption.get.fileIndex shouldBe BLOCK_LOCATOR.fileIndex
      // The transaction comes after the block header on the block file.
      txDesc.get.transactionLocatorOption.get.recordLocator.offset should be > BLOCK_LOCATOR.recordLocator.offset
    }
  }
  */

  init {

    "putBlockFileInfo/getBlockFileInfo" should "successfully put/get data" {
      val FILE_NUMBER = FileNumber(1)
      blockDb.getBlockFileInfo(db, FILE_NUMBER) shouldBe null

      val blockFileInfo = BlockFileInfo(
          blockCount = 10,
          fileSize = 101,
          firstBlockHeight = 0,
          lastBlockHeight = 3,
          firstBlockTimestamp = 1234567890L,
          lastBlockTimestamp = 9876543210L
      )

      blockDb.putBlockFileInfo(db, FILE_NUMBER, blockFileInfo)
      blockDb.getBlockFileInfo(db, FILE_NUMBER) shouldBe blockFileInfo

      // Update block file info with more blocks
      val newBlockFileInfo = blockFileInfo.copy(
          blockCount = blockFileInfo.blockCount + 1,
          fileSize = blockFileInfo.fileSize + 1000,
          lastBlockHeight = blockFileInfo.lastBlockHeight + 1,
          lastBlockTimestamp = blockFileInfo.lastBlockTimestamp + 2000
      )

      blockDb.putBlockFileInfo(db, FILE_NUMBER, newBlockFileInfo)
      blockDb.getBlockFileInfo(db, FILE_NUMBER) shouldBe newBlockFileInfo
    }


    "putBlockFileInfo/getBlockFileInfo" should "successfully put/get data with two blocks" {
      val FILE_NUMBER_1 = FileNumber(1)
      val FILE_NUMBER_2 = FileNumber(2)
      blockDb.getBlockFileInfo(db, FILE_NUMBER_1) shouldBe null
      blockDb.getBlockFileInfo(db, FILE_NUMBER_2) shouldBe null

      val blockFileInfo1 = BlockFileInfo(
          blockCount = 10,
          fileSize = 101,
          firstBlockHeight = 0,
          lastBlockHeight = 3,
          firstBlockTimestamp = 1234567890L,
          lastBlockTimestamp = 9876543210L
      )

      val blockFileInfo2 = BlockFileInfo(
          blockCount = 10,
          fileSize = 201,
          firstBlockHeight = 4,
          lastBlockHeight = 7,
          firstBlockTimestamp = 9876543210L,
          lastBlockTimestamp = 9976543210L
      )

      blockDb.putBlockFileInfo(db, FILE_NUMBER_1, blockFileInfo1)
      blockDb.putBlockFileInfo(db, FILE_NUMBER_2, blockFileInfo2)

      blockDb.getBlockFileInfo(db, FILE_NUMBER_1) shouldBe blockFileInfo1
      blockDb.getBlockFileInfo(db, FILE_NUMBER_2) shouldBe blockFileInfo2
    }


    "putBlockFileInfo" should "hit an assertion if the new block info is incorrect." {
      val FILE_NUMBER = FileNumber(1)
      blockDb.getBlockFileInfo(db, FILE_NUMBER) shouldBe null

      val blockFileInfo = BlockFileInfo(
          blockCount = 10,
          fileSize = 101,
          firstBlockHeight = 0,
          lastBlockHeight = 3,
          firstBlockTimestamp = 1234567890L,
          lastBlockTimestamp = 9876543210L
      )

      blockDb.putBlockFileInfo(db, FILE_NUMBER, blockFileInfo)
      blockDb.getBlockFileInfo(db, FILE_NUMBER) shouldBe blockFileInfo

      // Can't put the same block info twice.
      shouldThrow<AssertionError> {
        blockDb.putBlockFileInfo(db, FILE_NUMBER, blockFileInfo)
      }

      val newBlockFileInfo = blockFileInfo.copy(
          blockCount = blockFileInfo.blockCount + 1,
          fileSize = blockFileInfo.fileSize + 1000,
          lastBlockHeight = blockFileInfo.lastBlockHeight + 1,
          lastBlockTimestamp = blockFileInfo.lastBlockTimestamp + 2000
      )

      // First block height can't be changed.
      shouldThrow<AssertionError> {
        blockDb.putBlockFileInfo(db, FILE_NUMBER, newBlockFileInfo.copy(
            firstBlockHeight = blockFileInfo.firstBlockHeight + 1
          )
        )
      }

      // First block timestamp can't be changed.
      shouldThrow<AssertionError> {
        blockDb.putBlockFileInfo(db, FILE_NUMBER, newBlockFileInfo.copy(
            firstBlockTimestamp = blockFileInfo.firstBlockTimestamp + 1
          )
        )
      }

      // Block count should not be decreased.
      shouldThrow<AssertionError> {
        blockDb.putBlockFileInfo(db, FILE_NUMBER, newBlockFileInfo.copy(
            blockCount = blockFileInfo.blockCount - 1
          )
        )
      }

      // Block count should increase
      shouldThrow<AssertionError> {
        blockDb.putBlockFileInfo(db, FILE_NUMBER, newBlockFileInfo.copy(
            blockCount = blockFileInfo.blockCount
          )
        )
      }

      // File size should not be decreased
      shouldThrow<AssertionError> {
        blockDb.putBlockFileInfo(db, FILE_NUMBER, newBlockFileInfo.copy(
            fileSize = blockFileInfo.fileSize - 1
          )
        )
      }

      // File size should increase
      shouldThrow<AssertionError> {
        blockDb.putBlockFileInfo(db, FILE_NUMBER, newBlockFileInfo.copy(
            fileSize = blockFileInfo.fileSize
          )
        )
      }

      // If a block on a fork is added, the last block might not increase.
      /*
      // The last block height should not be decreased.
      shouldThrow<AssertionError> {
        db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
            lastBlockHeight = blockFileInfo.lastBlockHeight - 1
          )
        )
      }

      // The last block height should increase.
      shouldThrow<AssertionError> {
        db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
            lastBlockHeight = blockFileInfo.lastBlockHeight
          )
        )
      }
  */
      // Caution : The last block timestamp can decrease.
    }

    "putLastBlockFile/getLastBlockFile" should "successfully put/get data" {
      val FILE_NUMBER_1 = FileNumber(1)
      val FILE_NUMBER_2 = FileNumber(2)

      blockDb.getLastBlockFile(db) shouldBe null

      blockDb.putLastBlockFile(db, FILE_NUMBER_1)
      blockDb.getLastBlockFile(db) shouldBe FILE_NUMBER_1

      blockDb.putLastBlockFile(db, FILE_NUMBER_2)
      blockDb.getLastBlockFile(db) shouldBe FILE_NUMBER_2
    }

    "putLastBlockFile" should "fail with incorrect data" {
      val FILE_NUMBER_1 = FileNumber(1)
      val FILE_NUMBER_2 = FileNumber(2)

      blockDb.getLastBlockFile(db) shouldBe null

      blockDb.putLastBlockFile(db, FILE_NUMBER_2)
      blockDb.getLastBlockFile(db) shouldBe FILE_NUMBER_2

      // The file number should increase.
      shouldThrow<AssertionError> {
        blockDb.putLastBlockFile(db, FILE_NUMBER_1)
      }

      // The file number should increase.
      shouldThrow<AssertionError> {
        blockDb.putLastBlockFile(db, FILE_NUMBER_2)
      }
    }
  }
}
