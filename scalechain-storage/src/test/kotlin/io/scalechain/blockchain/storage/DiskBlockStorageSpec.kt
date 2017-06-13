package io.scalechain.blockchain.storage

import io.kotlintest.KTestJUnitRunner
import java.io.File

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.BlockHeaderCodec
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto.codec.primitive.Codecs
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.DatabaseFactory
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.TransactionDescriptorIndex
import io.scalechain.blockchain.storage.test.TestData
import io.scalechain.blockchain.storage.test.TestData.block1
import io.scalechain.util.ListExt
import org.junit.runner.RunWith
import kotlin.test.assertTrue

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
      while (diskBlockStorage.blockRecordStorage.files.size < 2) {
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
    "getTransaction" should "read a transaction in the transaction pool" {
      for( expectedTx in block1.transactions ) {
        if (! expectedTx.inputs[0].isCoinBaseInput()) {
          diskBlockStorage.putTransactionToPool(db,
            expectedTx.hash(),
            TransactionPoolEntry(
              expectedTx,
              listOf( null, InPoint(TestData.dummyHash(1), 1), null ),
              System.currentTimeMillis()
            )
          )
        }
      }
      for( expectedTx in block1.transactions ) {
        if (!expectedTx.inputs[0].isCoinBaseInput()) {
          val actualTx = diskBlockStorage.getTransaction(db, expectedTx.hash())
          actualTx shouldBe expectedTx
        }
      }
    }

    "getTransaction" should "read a transaction in a block" {
      diskBlockStorage.putBlock(db, block1)

      // Need to put the transaction descriptor to get the transaction from it..
      val block1Hash = block1.header.hash()
      val blockInfo = storage.getBlockInfo(db, block1Hash)!!

      val txDescIndex = object : TransactionDescriptorIndex {}

      // This is the offset of the block
      var currentOffset = blockInfo.blockLocatorOption!!.recordLocator.offset
      val blockHeaderSize = BlockHeaderCodec.encode(block1.header).size
      val txLengthFieldSize = Codecs.VariableInt.encode(block1.transactions.size.toLong()).size
      // Block header and transaction size is written first.
      currentOffset += blockHeaderSize + txLengthFieldSize

      for( tx in block1.transactions ) {
        val txHash = tx.hash()

        diskBlockStorage.hasTransaction(db, txHash) shouldBe false

        // And then each transaction is written.
        // currentOffset has the location where the transaction is written.
        val txSize = TransactionCodec.encode(tx).size
        val txLocator = blockInfo.blockLocatorOption!!.copy(
          recordLocator = RecordLocator(
            currentOffset,
            txSize
          )
        )
        // Increase the offset for the next transaction.
        currentOffset += txSize
        txDescIndex.putTransactionDescriptor(
          db,
          txHash,
          TransactionDescriptor(
            transactionLocator = txLocator,
            blockHeight = 1,
            outputsSpentBy = ListExt.fill<InPoint?>( tx.outputs.size, null)
          )
        )
      }


/*
      val txMagnet = TransactionMagnet(storage, txPoolIndex = storage, txTimeIndex = storage)
      val txPool = TransactionPool(storage, txMagnet)
      val blockMagnet = BlockMagnet(storage, txPool, txMagnet)
      blockMagnet.attachBlock(db, blockInfo, block1)
*/


      for( expectedTx in block1.transactions ) {
        val txHash = expectedTx.hash()
        diskBlockStorage.hasTransaction(db, txHash) shouldBe true

        val actualTx = diskBlockStorage.getTransaction(db, txHash)
        actualTx shouldBe expectedTx
      }
    }

    "get" should "return the object created by create method" {
      val testPath = File("./build/unittests-DiskBlockStorageSpec-for-create-test/")
      val storage = DiskBlockStorage.create(testPath, db);
      assertTrue( DiskBlockStorage.get() === storage )
    }
  }

  companion object {
    val TEST_RECORD_FILE_SIZE = 1024 * 1024
  }
}

