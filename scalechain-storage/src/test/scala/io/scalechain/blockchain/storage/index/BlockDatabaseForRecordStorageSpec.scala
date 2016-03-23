package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, CodecTestUtil}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.{TestData, TransactionLocator, Storage}
import io.scalechain.io.HexFileLoader
import io.scalechain.util.HexUtil._
import org.apache.commons.io.FileUtils
import org.scalatest._
import scodec.bits.BitVector

/**
  * Covers all methods in BlockDatabaseForRecordStorage as well as its super class, BlockDatabase
  */
class BlockDatabaseForRecordStorageSpec extends FlatSpec with ShouldMatchers with CodecTestUtil with BeforeAndAfterEach  {
  this: Suite =>

  Storage.initialize()

  var db : BlockDatabaseForRecordStorage = null
  override def beforeEach() {
    val testPath = new File("./target/unittests-BlockDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    db = new BlockDatabaseForRecordStorage( new RocksDatabase( testPath ) )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }


  val BLOCK_LOCATOR = FileRecordLocator(
    fileIndex = 1,
    RecordLocator( 10, 15)
  )

  val DUMMY_HASH = Hash(bytes("0"*64))

  "putTransactions" should "successfully put transactions onto database" in {
    // At first, we should not have any tranasctions on the database.
    for( transaction <- TestData.block.transactions) {
      val txHash = Hash (HashCalculator.transactionHash(transaction))
      db.getTransactionLocator(txHash) shouldBe None
    }

    var i = 1

    // Create (transaction hash, transaction locator pair )
    val txLocators = for(
      transaction <- TestData.block.transactions;
      txHash = Hash (HashCalculator.transactionHash(transaction));
      txLocator = BLOCK_LOCATOR.copy(recordLocator = BLOCK_LOCATOR.recordLocator.copy(offset = BLOCK_LOCATOR.recordLocator.offset + i * 100))
    ) yield {
      i += 1
      TransactionLocator(txHash,txLocator)
    }

    db.putTransactions(txLocators)

    // Now, we have transactions on the database.
    for( transaction <- TestData.block.transactions) {
      val txHash = Hash (HashCalculator.transactionHash(transaction))
      val txLocator = db.getTransactionLocator(txHash)

      assert(txLocator.isDefined)
      // The block file number should be same for the transaction locator and block locator, as the transaction is in the block.
      txLocator.get.fileIndex shouldBe BLOCK_LOCATOR.fileIndex
      // The transaction comes after the block header on the block file.
      txLocator.get.recordLocator.offset should be > BLOCK_LOCATOR.recordLocator.offset
    }
  }

  "putBlockFileInfo/getBlockFileInfo" should "successfully put/get data" in {
    val FILE_NUMBER = FileNumber(1)
    db.getBlockFileInfo(FILE_NUMBER) shouldBe None

    val blockFileInfo = BlockFileInfo(
      blockCount = 10,
      fileSize = 101,
      firstBlockHeight = 0,
      lastBlockHeight = 3,
      firstBlockTimestamp = 1234567890L,
      lastBlockTimestamp = 9876543210L
    )

    db.putBlockFileInfo(FILE_NUMBER, blockFileInfo)
    db.getBlockFileInfo(FILE_NUMBER) shouldBe Some(blockFileInfo)

    // Update block file info with more blocks
    val newBlockFileInfo = blockFileInfo.copy(
      blockCount = blockFileInfo.blockCount + 1,
      fileSize = blockFileInfo.fileSize + 1000,
      lastBlockHeight = blockFileInfo.lastBlockHeight + 1,
      lastBlockTimestamp = blockFileInfo.lastBlockTimestamp + 2000
    )

    db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo)
    db.getBlockFileInfo(FILE_NUMBER) shouldBe Some(newBlockFileInfo)
  }


  "putBlockFileInfo/getBlockFileInfo" should "successfully put/get data with two blocks" in {
    val FILE_NUMBER_1 = FileNumber(1)
    val FILE_NUMBER_2 = FileNumber(2)
    db.getBlockFileInfo(FILE_NUMBER_1) shouldBe None
    db.getBlockFileInfo(FILE_NUMBER_2) shouldBe None

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

    db.putBlockFileInfo(FILE_NUMBER_1, blockFileInfo1)
    db.putBlockFileInfo(FILE_NUMBER_2, blockFileInfo2)

    db.getBlockFileInfo(FILE_NUMBER_1) shouldBe Some(blockFileInfo1)
    db.getBlockFileInfo(FILE_NUMBER_2) shouldBe Some(blockFileInfo2)
  }


  "putBlockFileInfo" should "hit an assertion if the new block info is incorrect." in {
    val FILE_NUMBER = FileNumber(1)
    db.getBlockFileInfo(FILE_NUMBER) shouldBe None

    val blockFileInfo = BlockFileInfo(
      blockCount = 10,
      fileSize = 101,
      firstBlockHeight = 0,
      lastBlockHeight = 3,
      firstBlockTimestamp = 1234567890L,
      lastBlockTimestamp = 9876543210L
    )

    db.putBlockFileInfo(FILE_NUMBER, blockFileInfo)
    db.getBlockFileInfo(FILE_NUMBER) shouldBe Some(blockFileInfo)

    // Can't put the same block info twice.
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, blockFileInfo)
    }

    val newBlockFileInfo = blockFileInfo.copy(
      blockCount = blockFileInfo.blockCount + 1,
      fileSize = blockFileInfo.fileSize + 1000,
      lastBlockHeight = blockFileInfo.lastBlockHeight + 1,
      lastBlockTimestamp = blockFileInfo.lastBlockTimestamp + 2000
    )

    // First block height can't be changed.
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          firstBlockHeight = blockFileInfo.firstBlockHeight + 1
        )
      )
    }

    // First block timestamp can't be changed.
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          firstBlockTimestamp = blockFileInfo.firstBlockTimestamp + 1
        )
      )
    }

    // Block count should not be decreased.
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          blockCount = blockFileInfo.blockCount - 1
        )
      )
    }

    // Block count should increase
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          blockCount = blockFileInfo.blockCount
        )
      )
    }

    // File size should not be decreased
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          fileSize = blockFileInfo.fileSize -1
        )
      )
    }

    // File size should increase
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          fileSize = blockFileInfo.fileSize
        )
      )
    }


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

    // Caution : The last block timestamp can decrease.
  }

  "putLastBlockFile/getLastBlockFile" should "successfully put/get data" in {
    val FILE_NUMBER_1 = FileNumber(1)
    val FILE_NUMBER_2 = FileNumber(2)

    db.getLastBlockFile() shouldBe None

    db.putLastBlockFile(FILE_NUMBER_1)
    db.getLastBlockFile() shouldBe Some(FILE_NUMBER_1)

    db.putLastBlockFile(FILE_NUMBER_2)
    db.getLastBlockFile() shouldBe Some(FILE_NUMBER_2)
  }

  "putLastBlockFile" should "fail with incorrect data" in {
    val FILE_NUMBER_1 = FileNumber(1)
    val FILE_NUMBER_2 = FileNumber(2)

    db.getLastBlockFile() shouldBe None

    db.putLastBlockFile(FILE_NUMBER_2)
    db.getLastBlockFile() shouldBe Some(FILE_NUMBER_2)

    // The file number should increase.
    intercept[AssertionError] {
      db.putLastBlockFile(FILE_NUMBER_1)
    }

    // The file number should increase.
    intercept[AssertionError] {
      db.putLastBlockFile(FILE_NUMBER_2)
    }
  }
}
