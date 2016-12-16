package io.scalechain.blockchain.storage.index

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.CodecTestUtil
import org.junit.runner.RunWith

import io.scalechain.blockchain.storage.test.TestData
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import io.scalechain.util.ListExt

/**
  * Created by kangmo on 3/24/16.
  */
abstract class BlockDatabaseTestTrait : FlatSpec(), Matchers, CodecTestUtil {
  abstract var blockDb : BlockDatabase
  abstract var db : KeyValueDatabase

  val BLOCK_LOCATOR = FileRecordLocator(
    fileIndex = 1,
    recordLocator = RecordLocator( 10, 15)
  )

  val BLOCK_LOCATOR2 = FileRecordLocator(
    fileIndex = 2,
    recordLocator = RecordLocator( 11, 16)
  )

  val TestBlockInfo = BlockInfo(
    height = 1,
    chainWork = 100000L,
    nextBlockHash = null,
    transactionCount = 0,
    status = 0,
    blockHeader = TestData.block.header,
    blockLocatorOption = null
  )
  val DUMMY_HASH1 = Hash(Bytes.from(String(ListExt.fill(64,'1').toCharArray())))
  val DUMMY_HASH2 = Hash(Bytes.from(String(ListExt.fill(64,'2').toCharArray())))

  fun runTests() {
    "putBlockInfo/getBlockInfo" should "successfully put/get data" {
      blockDb.getBlockInfo(db, TestData.blockHash) shouldBe null

      val blockInfo = TestBlockInfo

      blockDb.putBlockInfo(db, TestData.blockHash, blockInfo)
      blockDb.getBlockInfo(db, TestData.blockHash) shouldBe blockInfo
      blockDb.getBlockHeight(db, TestData.blockHash) shouldBe 1L

      val newBlockInfo = blockInfo.copy(
          transactionCount = 10,
          blockLocatorOption = BLOCK_LOCATOR
      )

      blockDb.putBlockInfo(db, TestData.blockHash, newBlockInfo)
      blockDb.getBlockInfo(db, TestData.blockHash) shouldBe newBlockInfo
    }

    "putBlockInfo" should "hit an assertion if the new block info is incorrect." {
      blockDb.getBlockInfo(db, TestData.blockHash) shouldBe null

      val blockInfo = TestBlockInfo.copy(
          blockLocatorOption = BLOCK_LOCATOR
      )

      blockDb.putBlockInfo(db, TestData.blockHash, blockInfo)

      // hit an assertion : put a block info with different height
      shouldThrow<AssertionError> {
        blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
            height = blockInfo.height + 1
        ))
      }

      shouldThrow<AssertionError> {
        blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
            height = blockInfo.height - 1
        ))
      }

      // Should not hit an assertion if the block locator does not change.
      blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
          blockLocatorOption = BLOCK_LOCATOR
      ))

      // hit an assertion : put a block info with a different block locator
      shouldThrow<AssertionError> {
        blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
            blockLocatorOption = BLOCK_LOCATOR2
        ))
      }

      // hit an assertion : change any field on the block header
      shouldThrow<AssertionError> {
        blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
            blockHeader = blockInfo.blockHeader.copy(
                version = blockInfo.blockHeader.version + 1
            )
        ))
      }

      shouldThrow<AssertionError> {
        blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
            blockHeader = blockInfo.blockHeader.copy(
                hashPrevBlock = Hash(DUMMY_HASH1.value)
            )
        ))
      }

      shouldThrow<AssertionError> {
        blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
            blockHeader = blockInfo.blockHeader.copy(
                hashMerkleRoot = Hash(DUMMY_HASH1.value)
            )
        ))
      }


      shouldThrow<AssertionError> {
        blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
            blockHeader = blockInfo.blockHeader.copy(
                timestamp = blockInfo.blockHeader.timestamp + 1
            )
        ))
      }


      shouldThrow<AssertionError> {
        blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
            blockHeader = blockInfo.blockHeader.copy(
                target = blockInfo.blockHeader.target + 1
            )
        ))
      }


      shouldThrow<AssertionError> {
        blockDb.putBlockInfo(db, TestData.blockHash, blockInfo.copy(
            blockHeader = blockInfo.blockHeader.copy(
                nonce = blockInfo.blockHeader.nonce + 1
            )
        ))
      }
    }

    "putBestBlockHash/getBestBlockHash" should "successfully put/get data" {
      blockDb.getBestBlockHash(db) shouldBe null

      blockDb.putBestBlockHash(db, DUMMY_HASH1)
      blockDb.getBestBlockHash(db) shouldBe DUMMY_HASH1

      blockDb.putBestBlockHash(db, TestData.blockHash)
      blockDb.getBestBlockHash(db) shouldBe TestData.blockHash
    }

    "putBlockHashByHeight/getBlockHashByHeight" should "successfully put/get data" {
      blockDb.getBlockHashByHeight(db, 0) shouldBe null
      blockDb.getBlockHashByHeight(db,1) shouldBe null

      blockDb.putBlockHashByHeight(db,0, DUMMY_HASH1)
      blockDb.putBlockHashByHeight(db,1, DUMMY_HASH2)

      blockDb.getBlockHashByHeight(db,0) shouldBe DUMMY_HASH1
      blockDb.getBlockHashByHeight(db,1) shouldBe DUMMY_HASH2
    }

    "delBlockHashByHeight" should "successfully delete the block hash" {
      blockDb.putBlockHashByHeight(db, 0, DUMMY_HASH1)
      blockDb.putBlockHashByHeight(db, 1, DUMMY_HASH2)

      blockDb.getBlockHashByHeight(db, 0) shouldBe DUMMY_HASH1
      blockDb.getBlockHashByHeight(db, 1) shouldBe DUMMY_HASH2

      blockDb.delBlockHashByHeight(db, 1)
      blockDb.getBlockHashByHeight(db, 0) shouldBe DUMMY_HASH1
      blockDb.getBlockHashByHeight(db, 1) shouldBe null

      blockDb.delBlockHashByHeight(db, 0)
      blockDb.getBlockHashByHeight(db, 0) shouldBe null
      blockDb.getBlockHashByHeight(db, 1) shouldBe null
    }

    "updateNextBlockHash" should "successfully update the next block hash" {
      val blockInfo = TestBlockInfo

      blockDb.putBlockInfo(db, TestData.blockHash, blockInfo)
      blockDb.getBlockInfo(db, TestData.blockHash)!!.nextBlockHash shouldBe null
      blockDb.updateNextBlockHash(db, TestData.blockHash, DUMMY_HASH1)

      blockDb.getBlockInfo(db, TestData.blockHash)!!.nextBlockHash shouldBe DUMMY_HASH1
    }

    "updateNextBlockHash" should "hit an assertion if the block hash does not exist" {
      shouldThrow<AssertionError> {
        blockDb.updateNextBlockHash(db, DUMMY_HASH1, null)
      }
    }
  }
}
