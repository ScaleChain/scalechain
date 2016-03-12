package io.scalechain.blockchain.storage.db

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, CodecTestUtil}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.{Storage, BlockDatabase}
import io.scalechain.io.HexFileLoader
import io.scalechain.util.HexUtil
import org.apache.commons.io.FileUtils
import org.scalatest._
import scodec.bits.BitVector
import HexUtil._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockDatabaseSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with CodecTestUtil {
  this: Suite =>

  Storage.initialize()

  var db : BlockDatabase = null
  override def beforeEach() {
    val testPath = "./target/rocksdb-unittests"
    FileUtils.deleteDirectory(new File(testPath))
    db = new BlockDatabase( new RocksDatabase(testPath) )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    // tear-down code
    //
  }


  val rawBlockBytes = HexFileLoader.load("data/unittest/codec/block-size231721.hex")
  val rawBlockBits = BitVector.view(rawBlockBytes)

  implicit val codec = BlockCodec.codec

  val block : Block = decodeFully(rawBlockBits)
  val blockHash = Hash (HashCalculator.blockHeaderHash(block.header))

  val BLOCK_LOCATOR = FileRecordLocator(
    fileIndex = 1,
    RecordLocator( 10, 15)
  )

  val DUMMY_HASH = Hash(bytes("0"*32))


  "putBlockInfo/getBlockInfo" should "successfully put/get data" in {
    db.getBlockInfo(blockHash) shouldBe None

    val blockInfo = BlockInfo(
      height = 1,
      transactionCount = 0,
      status = 0,
      blockHeader = block.header,
      blockLocatorOption = None
    )

    db.putBlockInfo(blockHash, blockInfo)
    db.getBlockInfo(blockHash) shouldBe Some(blockInfo)

    val newBlockInfo = blockInfo.copy(
      transactionCount = 10,
      blockLocatorOption = Some (
        BLOCK_LOCATOR
      )
    )

    db.putBlockInfo(blockHash, newBlockInfo)
    db.getBlockInfo(blockHash) shouldBe Some(newBlockInfo)
  }


  "putTransactions" should "successfully put transactions onto database" in {
    // At first, we should not have any tranasctions on the database.
    for( transaction <- block.transactions) {
      val txHash = Hash (HashCalculator.transactionHash(transaction))
      db.getTransactionLocator(txHash) shouldBe None
    }

    db.putTransactions(BLOCK_LOCATOR, block.transactions)

    // Now, we have transactions on the database.
    for( transaction <- block.transactions) {
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
    val FILE_NUMBER = 1
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
    val FILE_NUMBER_1 = 1
    val FILE_NUMBER_2 = 2
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


  "putBlockFileInfo/getBlockFileInfo" should "hit an assertion if the new block info is incorrect." in {
    val FILE_NUMBER = 1
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

    // File size should not be decreased
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          fileSize = blockFileInfo.fileSize -1
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

    // The last block timestamp should not be decreased.
    intercept[AssertionError] {
      db.putBlockFileInfo(FILE_NUMBER, newBlockFileInfo.copy(
          lastBlockTimestamp = blockFileInfo.lastBlockTimestamp -1
        )
      )
    }
  }

  "putLastBlockFile/getLastBlockFile" should "successfully put/get data" in {
    val FILE_NUMBER_1 = 1
    val FILE_NUMBER_2 = 2

    db.getLastBlockFile() shouldBe None

    db.putLastBlockFile(FILE_NUMBER_1)
    db.getLastBlockFile() shouldBe Some(FILE_NUMBER_1)

    db.putLastBlockFile(FILE_NUMBER_2)
    db.getLastBlockFile() shouldBe Some(FILE_NUMBER_2)
  }

  "putLastBlockFile/getLastBlockFile" should "fail with incorrect data" in {
    val FILE_NUMBER_1 = 1
    val FILE_NUMBER_2 = 2

    db.getLastBlockFile() shouldBe None

    db.putLastBlockFile(FILE_NUMBER_2)
    db.getLastBlockFile() shouldBe Some(FILE_NUMBER_2)

    // The file number should increase.
    intercept[AssertionError] {
      db.putLastBlockFile(FILE_NUMBER_1)
    }
  }



  "putBestBlockHash/getBestBlockHash" should "successfully put/get data" in {
    db.getBestBlockHash() shouldBe None

    db.putBestBlockHash(DUMMY_HASH)
    db.getBestBlockHash() shouldBe Some(DUMMY_HASH)

    db.putBestBlockHash(blockHash)
    db.getBestBlockHash() shouldBe Some(blockHash)
  }
}
