package io.scalechain.blockchain.storage

import io.kotlintest.KTestJUnitRunner
import java.io.File

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.DatabaseFactory
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.test.TestData.block1
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class DiskBlockStorageSpec : BlockStorageTestTrait()  {

  override lateinit var db : KeyValueDatabase

  lateinit var diskBlockStorage : DiskBlockStorage
  override lateinit var storage : BlockStorage
  val testPath = File("./build/unittests-DiskBlockStorageSpec/")
  override fun beforeEach() {

    testPath.deleteRecursively()
    testPath.mkdir()

    db = DatabaseFactory.create( testPath )
    diskBlockStorage = DiskBlockStorage(db, testPath, TEST_RECORD_FILE_SIZE)

    storage = diskBlockStorage

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
    storage.close()

    testPath.deleteRecursively()
  }

  init {
    Storage.initialize()
    runTests()

    "updateFileInfo" should "pass case 1 : a new record file was created." {
      val FILE_NUMBER = 1
      diskBlockStorage.getLastBlockFile(db) shouldBe null
      diskBlockStorage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(offset=0, size=80)), fileSize = 10L, blockHeight = 1, blockTimestamp = 1000L)
      diskBlockStorage.getLastBlockFile(db) shouldBe FileNumber(FILE_NUMBER)
    }

    "updateFileInfo" should "pass case 2 : the block was written on the existing record file." {
      val FILE_NUMBER = 1
      diskBlockStorage.getLastBlockFile(db) shouldBe null
      diskBlockStorage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(offset=100, size=80)), fileSize = 10L, blockHeight = 1, blockTimestamp = 1000L)
      diskBlockStorage.getLastBlockFile(db) shouldBe null
    }

    "updateFileInfo" should "overwrite the file info if called twice" {
      val FILE_NUMBER = 1
      diskBlockStorage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(0, size=80)), fileSize = 10L, blockHeight = 1, blockTimestamp = 1000L)

      diskBlockStorage.getBlockFileInfo(db, FileNumber(FILE_NUMBER)) shouldBe
        BlockFileInfo (
          blockCount = 1,
          fileSize = 10L,
          firstBlockHeight = 1,
          lastBlockHeight = 1,
          firstBlockTimestamp = 1000,
          lastBlockTimestamp = 1000
        )

      // update once more with the next block.
      diskBlockStorage.updateFileInfo(FileRecordLocator(FILE_NUMBER, RecordLocator(100, 80)), fileSize = 20L, blockHeight = 2, blockTimestamp = 2000L)

      diskBlockStorage.getBlockFileInfo(db, FileNumber(FILE_NUMBER)) shouldBe
        BlockFileInfo (
          blockCount = 2,
          fileSize = 20L,
          firstBlockHeight = 1,
          lastBlockHeight = 2,
          firstBlockTimestamp = 1000,
          lastBlockTimestamp = 2000
        )
    }

    // Test case for the issue Unable to decode a specific block if a new record file was created between writing block header and transactions.
    // https://github.com/ScaleChain/scalechain/issues/36
    "getBlock" should "read a block correctly on the file boundary" {
      diskBlockStorage.putBlock(db, block1)
      var prevBlockHash = block1.header.hash()
      while( diskBlockStorage.blockRecordStorage.files.size < 2) {
        val newBlock = block1.copy(
          header = block1.header.copy(
            hashPrevBlock = prevBlockHash
          )
        )
        diskBlockStorage.putBlock(db, newBlock)
        prevBlockHash = newBlock.header.hash()

        diskBlockStorage.getBlock(db, prevBlockHash)?.second shouldBe newBlock
      }
    }
  }

  companion object {
    val TEST_RECORD_FILE_SIZE = 1024 * 1024
  }
}

