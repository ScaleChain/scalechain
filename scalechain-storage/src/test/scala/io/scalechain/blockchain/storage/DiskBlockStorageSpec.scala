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

  "checkBestBlockHash" should "" in {
  }

  "putBlock(block)" should "" in {
  }


  "putBlock(hash,block)" should "" in {
  }


  "putBlockHeader(header)" should "" in {
  }


  "putBlockHeader(hash,header)" should "" in {
  }


  "getTransaction" should "" in {
  }


  "getBestBlockHash" should "" in {
  }

  "hasBlock" should "" in {
  }

  "getBlockHeader" should "" in {
  }

  "hasBlockHeader" should "" in {
  }

  "getBlock(BlockHash)" should "" in {
  }

  "getTransaction(TransactionHash)" should "" in {
  }

}

