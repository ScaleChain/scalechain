package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class DiskBlockStorageSpec extends BlockStorageTestTrait with BeforeAndAfterEach  {
  this: Suite =>

  import TestData._

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var diskBlockStorage : DiskBlockStorage = null
  var storage : BlockStorage = null
  override def beforeEach() {

    val testPath = new File("./target/unittests-DiskBlockStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    diskBlockStorage = new DiskBlockStorage(testPath, TEST_RECORD_FILE_SIZE)
    storage = diskBlockStorage

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()
  }

  "checkBestBlockHash" should "pass case 1 : the block height of the new block is greater than the highest one." in {
    diskBlockStorage.getBestBlockHash() shouldBe None
    diskBlockStorage.checkBestBlockHash(blockHash1, 1)
    diskBlockStorage.getBestBlockHash() shouldBe Some(blockHash1)
    diskBlockStorage.checkBestBlockHash(blockHash2, 2)
    diskBlockStorage.getBestBlockHash() shouldBe Some(blockHash2)
  }

  "checkBestBlockHash" should "pass case 2 : the block height of the new block is less than the highest one." in {
    diskBlockStorage.getBestBlockHash() shouldBe None
    diskBlockStorage.checkBestBlockHash(blockHash2, 2)
    diskBlockStorage.getBestBlockHash() shouldBe Some(blockHash2)
    diskBlockStorage.checkBestBlockHash(blockHash1, 1)
    diskBlockStorage.getBestBlockHash() shouldBe Some(blockHash2)
  }

  "updateFileInfo" should "pass case 1 : a new record file was created." in {
    val FILE_NUMBER = 1
    diskBlockStorage.blockIndex.getLastBlockFile() shouldBe None
    diskBlockStorage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(offset=0, 80)), fileSize = 10L, blockHeight = 1, blockTimestamp = 1000L)
    diskBlockStorage.blockIndex.getLastBlockFile() shouldBe Some(FileNumber(FILE_NUMBER))
  }

  "updateFileInfo" should "pass case 2 : the block was written on the existing record file." in {
    val FILE_NUMBER = 1
    diskBlockStorage.blockIndex.getLastBlockFile() shouldBe None
    diskBlockStorage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(offset=100, 80)), fileSize = 10L, blockHeight = 1, blockTimestamp = 1000L)
    diskBlockStorage.blockIndex.getLastBlockFile() shouldBe None
  }

  "updateFileInfo" should "overwrite the file info if called twice" in {
    val FILE_NUMBER = 1
    diskBlockStorage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(0, 80)), fileSize = 10L, blockHeight = 1, blockTimestamp = 1000L)

    diskBlockStorage.blockIndex.getBlockFileInfo(FileNumber(FILE_NUMBER)) shouldBe
      Some( BlockFileInfo (
        blockCount = 1,
        fileSize = 10L,
        firstBlockHeight = 1,
        lastBlockHeight = 1,
        firstBlockTimestamp = 1000,
        lastBlockTimestamp = 1000
      ))

    // update once more with the next block.
    diskBlockStorage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(100, 80)), fileSize = 20L, blockHeight = 2, blockTimestamp = 2000L)

    diskBlockStorage.blockIndex.getBlockFileInfo(FileNumber(FILE_NUMBER)) shouldBe
      Some( BlockFileInfo (
        blockCount = 2,
        fileSize = 20L,
        firstBlockHeight = 1,
        lastBlockHeight = 2,
        firstBlockTimestamp = 1000,
        lastBlockTimestamp = 2000
      ))
  }

  "getBlockHeight" should "return -1 for the hash with all zero values" in {
    diskBlockStorage.getBlockHeight(ALL_ZERO_HASH) shouldBe Some(-1)
  }

  "getBlockHeight" should "return the height of the block" in {
    diskBlockStorage.putBlockHeader(block1.header)
    diskBlockStorage.putBlockHeader(block2.header)
    diskBlockStorage.getBlockHeight(blockHash1) shouldBe Some(0)
    diskBlockStorage.getBlockHeight(blockHash2) shouldBe Some(1)
  }

  // Test case for the issue Unable to decode a specific block if a new record file was created between writing block header and transactions.
  // https://github.com/ScaleChain/scalechain/issues/36
  "getBlock" should "read a block correctly on the file boundary" in {
    diskBlockStorage.putBlock(block1)
    var prevBlockHash = BlockHash( HashCalculator.blockHeaderHash(block1.header))
    while( diskBlockStorage.blockRecordStorage.files.size < 2) {
      val newBlock = block1.copy(
        header = block1.header.copy(
          hashPrevBlock = prevBlockHash
        )
      )
      diskBlockStorage.putBlock(newBlock)
      prevBlockHash = BlockHash( HashCalculator.blockHeaderHash(newBlock.header))

      diskBlockStorage.getBlock(prevBlockHash).map(_._2) shouldBe Some(newBlock)
    }
  }

}

