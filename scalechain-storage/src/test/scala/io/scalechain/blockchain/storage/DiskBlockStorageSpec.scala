package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.record.BlockRecordStorage
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class DiskBlockStorageSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

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
  }

  "checkBestBlockHash" should "pass case 2 : the block height of the new block is less than the highest one." in {
  }

  "updateFileInfo" should "pass case 1 : a new record file was created." in {
  }

  "updateFileInfo" should "pass case 2 : the block was written on the existing record file." in {
  }

  "putBlock(block)" should "store a block without hash" in {
  }

  "putBlock(hash,block)" should "pass case 1.1 : block info without a block locator was found." in {
  }

  "putBlock(hash,block)" should "pass case 1.2 block info with a block locator was found." in {
  }

  "putBlock(hash,block)" should "pass case 2.1 : no block info was found, previous block header exists." in {
  }

  "putBlock(hash,block)" should "pass case 2.2 : no block info was found, previous block header does not exists." in {
  }

  "putBlockHeader(header)" should "store a block header without hash" in {
  }

  "putBlockHeader(hash,header)" should "pass case 1.1 : the same block header was not found" in {
  }

  "putBlockHeader(hash,header)" should "pass case 1.2 : the same block header already exists" in {
  }

  "putBlockHeader(hash,header)" should "pass case 2 : the previous block header was not found" in {
  }


  "getTransaction" should "return None if the transaction is not found." in {
  }

  "getTransaction" should "return Some(transaction) if the transaction is found." in {
  }

  "getBlock" should "return None if the block is not found." in {

  }

  "getBlock" should "return Some(block) if the block is found." in {

  }

  "getBestBlockHash" should "return None if the best block hash was not put." in {
  }

  "getBestBlockHash" should "return Some(block hash) if the best block hash was put." in {
  }


  "hasBlock" should "return true if the block exists." in {
  }

  "hasBlock" should "return false if the block does not exist." in {
  }

  "getBlockHeader" should "return None if the block header does not exist." in {
  }

  "hasBlockHeader" should "return Some(block header) if the block header exists." in {
  }

  // This method is a wrapper of the getBlock(Hash). Just do a sanity test.
  "getBlock(BlockHash)" should "get a block" in {
  }

  // This method is a wrapper of the getTransaction(Hash). Just do a sanity test.
  "getTransaction(TransactionHash)" should "get a transaction" in {
  }

}

