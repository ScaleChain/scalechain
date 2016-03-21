package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.record.BlockRecordStorage
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class DiskBlockStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  import TestData._

  Storage.initialize()

  var storage : DiskBlockStorage = null
  override def beforeEach() {

    val testPath = new File("./target/unittests-DiskBlockStorageSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    storage = new DiskBlockStorage(testPath)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()
  }

  "checkBestBlockHash" should "pass case 1 : the block height of the new block is greater than the highest one." in {
    storage.getBestBlockHash() shouldBe None
    storage.checkBestBlockHash(blockHash1, 1)
    storage.getBestBlockHash() shouldBe Some(blockHash1)
    storage.checkBestBlockHash(blockHash2, 2)
    storage.getBestBlockHash() shouldBe Some(blockHash2)
  }

  "checkBestBlockHash" should "pass case 2 : the block height of the new block is less than the highest one." in {
    storage.getBestBlockHash() shouldBe None
    storage.checkBestBlockHash(blockHash2, 2)
    storage.getBestBlockHash() shouldBe Some(blockHash2)
    storage.checkBestBlockHash(blockHash1, 1)
    storage.getBestBlockHash() shouldBe Some(blockHash2)
  }

  "updateFileInfo" should "pass case 1 : a new record file was created." in {
    val FILE_NUMBER = 1
    storage.blockIndex.getLastBlockFile() shouldBe None
    storage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(offset=0, 80)), fileSize = 10L, blockHeight = 1, blockTimestamp = 1000L)
    storage.blockIndex.getLastBlockFile() shouldBe Some(FileNumber(FILE_NUMBER))
  }

  "updateFileInfo" should "pass case 2 : the block was written on the existing record file." in {
    val FILE_NUMBER = 1
    storage.blockIndex.getLastBlockFile() shouldBe None
    storage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(offset=100, 80)), fileSize = 10L, blockHeight = 1, blockTimestamp = 1000L)
    storage.blockIndex.getLastBlockFile() shouldBe None
  }

  "updateFileInfo" should "overwrite the file info if called twice" in {
    val FILE_NUMBER = 1
    storage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(0, 80)), fileSize = 10L, blockHeight = 1, blockTimestamp = 1000L)

    storage.blockIndex.getBlockFileInfo(FileNumber(FILE_NUMBER)) shouldBe
      Some( BlockFileInfo (
        blockCount = 1,
        fileSize = 10L,
        firstBlockHeight = 1,
        lastBlockHeight = 1,
        firstBlockTimestamp = 1000,
        lastBlockTimestamp = 1000
      ))

    // update once more with the next block.
    storage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(100, 80)), fileSize = 20L, blockHeight = 2, blockTimestamp = 2000L)

    storage.blockIndex.getBlockFileInfo(FileNumber(FILE_NUMBER)) shouldBe
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
    storage.getBlockHeight(ALL_ZERO_HASH) shouldBe Some(-1)
  }

  "getBlockHeight" should "return the height of the block" in {
    storage.putBlockHeader(block1.header)
    storage.putBlockHeader(block2.header)
    storage.getBlockHeight(blockHash1) shouldBe Some(0)
    storage.getBlockHeight(blockHash2) shouldBe Some(1)
  }

  "putBlock(block)" should "store a block without hash" in {
    storage.putBlock(block1) shouldBe true
    storage.putBlock(block2) shouldBe true

    storage.getBlock(blockHash1) shouldBe Some(block1)
    storage.getBlock(blockHash2) shouldBe Some(block2)
    storage.hasBlock(blockHash1) shouldBe true
    storage.hasBlock(blockHash2) shouldBe true
  }

  "putBlock(hash,block)" should "pass case 1.1 : block info without a block locator was found." in {
    // Step 1 : put the genesis block header
    storage.putBlockHeader(block1.header)

    // Step 2 : put the genesis block
    storage.putBlock(block1) shouldBe false

    // Step 3 : should get the block
    storage.getBlock(blockHash1) shouldBe Some(block1)
    storage.hasBlock(blockHash1) shouldBe true

  }

  "putBlock(hash,block)" should "pass case 1.2 block info with a block locator was found." in {
    // Step 1 : put the genesis block
    storage.putBlock(block1) shouldBe true

    // Step 2 : put the same block
    storage.putBlock(block1) shouldBe true

    // Step 3 : should get the block
    storage.getBlock(blockHash1) shouldBe Some(block1)
    storage.hasBlock(blockHash1) shouldBe true
  }

  "putBlock(hash,block)" should "pass case 2.1 : no block info was found, previous block header exists. Only the header of the previous block exists." in {
    // Step 1 : put the genesis block header
    storage.putBlockHeader(block1.header)

    // Step 2 : put the second block
    storage.putBlock(block2) shouldBe true

    // Step 3 : should get the blocks
    storage.getBlock(blockHash1) shouldBe None // We put an header only.
    storage.getBlock(blockHash2) shouldBe Some(block2)
    storage.hasBlock(blockHash1) shouldBe false // We put an header only.
    storage.hasBlock(blockHash2) shouldBe true
  }

  "putBlock(hash,block)" should "pass case 2.1 : no block info was found, previous block header exists. Full block data exists for the previous block" in {
    // Step 1 : put the genesis block
    storage.putBlock(block1) shouldBe true

    // Step 2 : put the second block
    storage.putBlock(block2) shouldBe true

    // Step 3 : should get the blocks
    storage.getBlock(blockHash1) shouldBe Some(block1)
    storage.getBlock(blockHash2) shouldBe Some(block2)
    storage.hasBlock(blockHash1) shouldBe true
    storage.hasBlock(blockHash2) shouldBe true
  }


  "putBlock(hash,block)" should "pass case 2.2 : no block info was found, previous block header does not exists." in {
    // Step 1 : put the second block
    storage.putBlock(block2) shouldBe true

    // Step 2 : should get None for the second block hash
    storage.getBlock(blockHash2) shouldBe None
    storage.hasBlock(blockHash2) shouldBe false
  }

  "putBlockHeader(header)" should "store a block header without hash" in {
    storage.putBlockHeader(block1.header)
    storage.putBlockHeader(block2.header)
    storage.getBlockHeader(blockHash1) shouldBe Some(block1.header)
    storage.getBlockHeader(blockHash2) shouldBe Some(block2.header)
    // We don't have a block, but we have hash only.
    storage.hasBlock(blockHash1) shouldBe false
    storage.hasBlock(blockHash2) shouldBe false
  }

  "putBlockHeader(hash,header)" should "pass case 1.1 : the header does not exist yet." in {
    // The header does not exist yet.
    storage.putBlockHeader(blockHash1, block1.header)
    storage.getBlockHeader(blockHash1) shouldBe Some(block1.header)

    // We don't have a block, but we have hash only.
    storage.hasBlock(blockHash1) shouldBe false
  }

  "putBlockHeader(hash,header)" should "pass case 1.2 : the same block header already exists" in {
    storage.putBlockHeader(blockHash1, block1.header)
    // put the same header : nothing happens.
    storage.putBlockHeader(blockHash1, block1.header)
    storage.getBlockHeader(blockHash1) shouldBe Some(block1.header)

    // We don't have a block, but we have hash only.
    storage.hasBlock(blockHash1) shouldBe false
  }

  "putBlockHeader(hash,header)" should "pass case 2 : the previous block header was not found" in {
    // The previous block header, block1.header is not stored.
    storage.putBlockHeader(block2.header)
    // Because the previous block header was not found, the block2.header is not stored.
    storage.getBlockHeader(blockHash2) shouldBe None

    // We don't have a block, but we have hash only.
    storage.hasBlock(blockHash2) shouldBe false
  }


  "getTransaction" should "return None if the transaction is not found." in {
    for (transaction <- block1.transactions) {
      val txHash = TransactionHash( HashCalculator.transactionHash(transaction))
      storage.getTransaction(txHash) shouldBe None
    }
  }

  "getTransaction" should "return Some(transaction) if the transaction is found." in {
    storage.putBlock(block1) shouldBe true

    // Step 3 : After putting a block, the transaction not exist.
    for (transaction <- block1.transactions) {
      val txHash = Hash( HashCalculator.transactionHash(transaction) )
      storage.getTransaction(txHash) shouldBe Some(transaction)
    }
  }

  "getBlock" should "return None if the block is not found." in {
    storage.getBlock(blockHash1) shouldBe None
  }

  "getBlock" should "return Some(block) if the block is found." in {
    storage.putBlock(block1) shouldBe true
    storage.getBlock(blockHash1) shouldBe Some(block1)
  }

  "getBestBlockHash" should "return None if the best block hash was not put." in {
    storage.getBestBlockHash() shouldBe None
  }

  "getBestBlockHash" should "return Some(block hash) if the best block hash was put." in {
    storage.putBlockHeader(block1.header)
    storage.getBestBlockHash() shouldBe Some(blockHash1)
  }

  "getBestBlockHash" should "return the best block hash." in {
    storage.putBlockHeader(block1.header)
    storage.getBestBlockHash() shouldBe Some(blockHash1)
  }


  "hasBlock" should "return false if the block does not exist." in {
    storage.hasBlock(blockHash1) shouldBe false
    storage.hasBlock(blockHash2) shouldBe false
  }

  "hasBlock" should "return true if the block exists." in {
    // The test with hasBlock was done in the putBlock/putBlockHeader case.
  }

  "getBlockHeader" should "return None if the block header does not exist." in {
    storage.getBlockHeader(blockHash1) shouldBe None
  }

  "hasBlockHeader" should "return Some(block header) if the block header was put." in {
    storage.putBlockHeader(block1.header)
    storage.getBlockHeader(blockHash1) shouldBe Some(block1.header)
  }

  "hasBlockHeader" should "return Some(block header) if the block was put." in {
    storage.putBlock(block1) shouldBe true
    storage.getBlockHeader(blockHash1) shouldBe Some(block1.header)
  }

  // This method is a wrapper of the getBlock(Hash). Just do a sanity test.
  "getBlock(BlockHash)" should "get a block" in {
    storage.putBlock(block1) shouldBe true
    storage.putBlock(block2) shouldBe true

    storage.getBlock(BlockHash(blockHash1.value)) shouldBe Some(block1)
    storage.getBlock(BlockHash(blockHash2.value)) shouldBe Some(block2)
  }

  // This method is a wrapper of the getTransaction(Hash). Just do a sanity test.
  "getTransaction(TransactionHash)" should "get a transaction" in {
    // Step 1 : Before putting a block, the transaction does not exist.
    for (transaction <- block1.transactions) {
      val txHash = TransactionHash( HashCalculator.transactionHash(transaction))
      storage.getTransaction(txHash) shouldBe None
    }

    // Step 2 : Put a block.
    storage.putBlock(block1) shouldBe true

    // Step 3 : After putting a block, the transaction not exist.
    for (transaction <- block1.transactions) {
      val txHash = TransactionHash( HashCalculator.transactionHash(transaction))
      storage.getTransaction(txHash) shouldBe Some(transaction)
    }
  }

}

