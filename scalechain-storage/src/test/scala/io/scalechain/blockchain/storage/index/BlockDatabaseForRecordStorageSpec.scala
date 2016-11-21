package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, CodecTestUtil}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.test.TestData
import io.scalechain.blockchain.storage.{TransactionLocator, Storage}
import io.scalechain.io.HexFileLoader
import io.scalechain.util.HexUtil._
import org.apache.commons.io.FileUtils
import org.scalatest._
import scodec.bits.BitVector

/**
  * Covers all methods in BlockDatabaseForRecordStorage as well as its super class, BlockDatabase
  */
class BlockDatabaseForRecordStorageSpec extends FlatSpec with Matchers with CodecTestUtil with BeforeAndAfterEach  {
  this: Suite =>

  Storage.initialize()

  implicit var db : RocksDatabase = null
  var blockDb : BlockDatabaseForRecordStorage = null

  val testPath = new File("./target/unittests-BlockDatabaseSpec")

  override def beforeEach() {
    FileUtils.deleteDirectory( testPath )
    testPath.mkdir()

    blockDb = new BlockDatabaseForRecordStorage {}
    db = new RocksDatabase( testPath )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
    db = null
    FileUtils.deleteDirectory( testPath )
  }


  val BLOCK_LOCATOR = FileRecordLocator(
    fileIndex = 1,
    RecordLocator( 10, 15)
  )

  val DUMMY_HASH = Hash(bytes("0"*64))
/*
  "putTransactions" should "successfully put transactions onto database" in {
    // At first, we should not have any tranasctions on the database.
    for( transaction <- TestData.block.transactions) {
      db.getTransactionDescriptor(transaction.hash) shouldBe None
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

  "putBlockFileInfo/getBlockFileInfo" should "successfully put/get data" in {
    val FILE_NUMBER = FileNumber(1)
    blockDb.getBlockFileInfo(FILE_NUMBER) shouldBe None

    val blockFileInfo = BlockFileInfo(
      blockCount = 10,
      fileSize = 101,
      firstBlockHeight = 0,
      lastBlockHeight = 3,
      firstBlockTimestamp = 1234567890L,
      lastBlockTimestamp = 9876543210L
    )

    blockDb.putBlockFileInfo(FILE_NUMBER, blockFileInfo)
    blockDb.getBlockFileInfo(FILE_NUMBER) shouldBe Some(blockFileInfo)

    // Update block file info with more blocks
    val newBlockFileInfo = blockFileInfo.copy(
      blockCount = blockFileInfo.blockCount + 1,
      fileSize = blockFileInfo.fileSize + 1000,
      lastBlockHeight = blockFileInfo.lastBlockHeight + 1,
      lastBlockTimestamp = blockFileInfo.lastBlockTimestamp + 2000
    )

    blockDb.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo)
    blockDb.getBlockFileInfo(FILE_NUMBER) shouldBe Some(newBlockFileInfo)
  }


  "putBlockFileInfo/getBlockFileInfo" should "successfully put/get data with two blocks" in {
    val FILE_NUMBER_1 = FileNumber(1)
    val FILE_NUMBER_2 = FileNumber(2)
    blockDb.getBlockFileInfo(FILE_NUMBER_1) shouldBe None
    blockDb.getBlockFileInfo(FILE_NUMBER_2) shouldBe None

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

    blockDb.putBlockFileInfo(FILE_NUMBER_1, blockFileInfo1)
    blockDb.putBlockFileInfo(FILE_NUMBER_2, blockFileInfo2)

    blockDb.getBlockFileInfo(FILE_NUMBER_1) shouldBe Some(blockFileInfo1)
    blockDb.getBlockFileInfo(FILE_NUMBER_2) shouldBe Some(blockFileInfo2)
  }


  "putBlockFileInfo" should "hit an assertion if the new block info is incorrect." in {
    val FILE_NUMBER = FileNumber(1)
    blockDb.getBlockFileInfo(FILE_NUMBER) shouldBe None

    val blockFileInfo = BlockFileInfo(
      blockCount = 10,
      fileSize = 101,
      firstBlockHeight = 0,
      lastBlockHeight = 3,
      firstBlockTimestamp = 1234567890L,
      lastBlockTimestamp = 9876543210L
    )

    blockDb.putBlockFileInfo(FILE_NUMBER, blockFileInfo)
    blockDb.getBlockFileInfo(FILE_NUMBER) shouldBe Some(blockFileInfo)

    // Can't put the same block info twice.
    intercept[AssertionError] {
      blockDb.putBlockFileInfo(FILE_NUMBER, blockFileInfo)
    }

    val newBlockFileInfo = blockFileInfo.copy(
      blockCount = blockFileInfo.blockCount + 1,
      fileSize = blockFileInfo.fileSize + 1000,
      lastBlockHeight = blockFileInfo.lastBlockHeight + 1,
      lastBlockTimestamp = blockFileInfo.lastBlockTimestamp + 2000
    )

    // First block height can't be changed.
    intercept[AssertionError] {
      blockDb.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          firstBlockHeight = blockFileInfo.firstBlockHeight + 1
        )
      )
    }

    // First block timestamp can't be changed.
    intercept[AssertionError] {
      blockDb.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          firstBlockTimestamp = blockFileInfo.firstBlockTimestamp + 1
        )
      )
    }

    // Block count should not be decreased.
    intercept[AssertionError] {
      blockDb.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          blockCount = blockFileInfo.blockCount - 1
        )
      )
    }

    // Block count should increase
    intercept[AssertionError] {
      blockDb.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          blockCount = blockFileInfo.blockCount
        )
      )
    }

    // File size should not be decreased
    intercept[AssertionError] {
      blockDb.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          fileSize = blockFileInfo.fileSize -1
        )
      )
    }

    // File size should increase
    intercept[AssertionError] {
      blockDb.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          fileSize = blockFileInfo.fileSize
        )
      )
    }

    // If a block on a fork is added, the last block might not increase.
/*
    // The last block height should not be decreased.
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          lastBlockHeight = blockFileInfo.lastBlockHeight - 1
        )
      )
    }

    // The last block height should increase.
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          lastBlockHeight = blockFileInfo.lastBlockHeight
        )
      )
    }
*/
    // Caution : The last block timestamp can decrease.
  }

  "putLastBlockFile/getLastBlockFile" should "successfully put/get data" in {
    val FILE_NUMBER_1 = FileNumber(1)
    val FILE_NUMBER_2 = FileNumber(2)

    blockDb.getLastBlockFile() shouldBe None

    blockDb.putLastBlockFile(FILE_NUMBER_1)
    blockDb.getLastBlockFile() shouldBe Some(FILE_NUMBER_1)

    blockDb.putLastBlockFile(FILE_NUMBER_2)
    blockDb.getLastBlockFile() shouldBe Some(FILE_NUMBER_2)
  }

  "putLastBlockFile" should "fail with incorrect data" in {
    val FILE_NUMBER_1 = FileNumber(1)
    val FILE_NUMBER_2 = FileNumber(2)

    blockDb.getLastBlockFile() shouldBe None

    blockDb.putLastBlockFile(FILE_NUMBER_2)
    blockDb.getLastBlockFile() shouldBe Some(FILE_NUMBER_2)

    // The file number should increase.
    intercept[AssertionError] {
      blockDb.putLastBlockFile(FILE_NUMBER_1)
    }

    // The file number should increase.
    intercept[AssertionError] {
      blockDb.putLastBlockFile(FILE_NUMBER_2)
    }
  }
}
