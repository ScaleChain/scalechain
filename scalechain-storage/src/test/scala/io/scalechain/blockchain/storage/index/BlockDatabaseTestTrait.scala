package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{CodecTestUtil}
import io.scalechain.blockchain.storage.test.TestData
import io.scalechain.util.HexUtil._
import org.scalatest._

/**
  * Created by kangmo on 3/24/16.
  */
trait BlockDatabaseTestTrait extends FlatSpec with ShouldMatchers with CodecTestUtil {
  var db : BlockDatabase

  val BLOCK_LOCATOR = FileRecordLocator(
    fileIndex = 1,
    RecordLocator( 10, 15)
  )

  val BLOCK_LOCATOR2 = FileRecordLocator(
    fileIndex = 2,
    RecordLocator( 11, 16)
  )

  val TestBlockInfo = BlockInfo(
    height = 1,
    chainWork = 100000L,
    nextBlockHash = None,
    transactionCount = 0,
    status = 0,
    blockHeader = TestData.block.header,
    blockLocatorOption = None
  )
  val DUMMY_HASH1 = Hash(bytes("1"*64))
  val DUMMY_HASH2 = Hash(bytes("2"*64))

  "putBlockInfo/getBlockInfo" should "successfully put/get data" in {
    db.getBlockInfo(TestData.blockHash) shouldBe None

    val blockInfo = TestBlockInfo

    db.putBlockInfo(TestData.blockHash, blockInfo)
    db.getBlockInfo(TestData.blockHash) shouldBe Some(blockInfo)
    db.getBlockHeight(TestData.blockHash) shouldBe Some(1)

    val newBlockInfo = blockInfo.copy(
      transactionCount = 10,
      blockLocatorOption = Some (
        BLOCK_LOCATOR
      )
    )

    db.putBlockInfo(TestData.blockHash, newBlockInfo)
    db.getBlockInfo(TestData.blockHash) shouldBe Some(newBlockInfo)
  }

  "putBlockInfo" should "hit an assertion if the new block info is incorrect." in {
    db.getBlockInfo(TestData.blockHash) shouldBe None

    val blockInfo = TestBlockInfo.copy(
      blockLocatorOption = Some(BLOCK_LOCATOR)
    )

    db.putBlockInfo(TestData.blockHash, blockInfo)

    // hit an assertion : put a block info with different height
    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        height = blockInfo.height + 1
      ))
    }

    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        height = blockInfo.height - 1
      ))
    }

    // Should not hit an assertion if the block locator does not change.
    db.putBlockInfo(TestData.blockHash, blockInfo.copy(
      blockLocatorOption = Some(BLOCK_LOCATOR)
    ))

    // hit an assertion : put a block info with a different block locator
    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        blockLocatorOption = Some(BLOCK_LOCATOR2)
      ))
    }

    // hit an assertion : change any field on the block header
    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        blockHeader = blockInfo.blockHeader.copy(
          version = blockInfo.blockHeader.version + 1
        )
      ))
    }

    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        blockHeader = blockInfo.blockHeader.copy(
          hashPrevBlock = Hash(DUMMY_HASH1.value)
        )
      ))
    }

    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        blockHeader = blockInfo.blockHeader.copy(
          hashMerkleRoot = Hash(DUMMY_HASH1.value)
        )
      ))
    }


    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        blockHeader = blockInfo.blockHeader.copy(
          timestamp = blockInfo.blockHeader.timestamp + 1
        )
      ))
    }


    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        blockHeader = blockInfo.blockHeader.copy(
          target = blockInfo.blockHeader.target + 1
        )
      ))
    }


    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        blockHeader = blockInfo.blockHeader.copy(
          nonce = blockInfo.blockHeader.nonce + 1
        )
      ))
    }
  }

  "putBestBlockHash/getBestBlockHash" should "successfully put/get data" in {
    db.getBestBlockHash() shouldBe None

    db.putBestBlockHash(DUMMY_HASH1)
    db.getBestBlockHash() shouldBe Some(DUMMY_HASH1)

    db.putBestBlockHash(TestData.blockHash)
    db.getBestBlockHash() shouldBe Some(TestData.blockHash)
  }

  "putBlockHashByHeight/getBlockHashByHeight" should "successfully put/get data" in {
    db.getBlockHashByHeight(0) shouldBe None
    db.getBlockHashByHeight(1) shouldBe None

    db.putBlockHashByHeight(0, DUMMY_HASH1)
    db.putBlockHashByHeight(1, DUMMY_HASH2)

    db.getBlockHashByHeight(0) shouldBe Some(DUMMY_HASH1)
    db.getBlockHashByHeight(1) shouldBe Some(DUMMY_HASH2)
  }

  "updateNextBlockHash" should "successfully update the next block hash" in {
    val blockInfo = TestBlockInfo

    db.putBlockInfo(TestData.blockHash, blockInfo)
    db.getBlockInfo(TestData.blockHash).get.nextBlockHash shouldBe None
    db.updateNextBlockHash(TestData.blockHash, Some(DUMMY_HASH1))

    db.getBlockInfo(TestData.blockHash).get.nextBlockHash shouldBe Some(DUMMY_HASH1)
  }

  "updateNextBlockHash" should "hit an assertion if the block hash does not exist" in {
    intercept[AssertionError] {
      db.updateNextBlockHash(DUMMY_HASH1, None)
    }
  }
}
