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

  val DUMMY_HASH = Hash(bytes("0"*64))

  "putBlockInfo/getBlockInfo" should "successfully put/get data" in {
    db.getBlockInfo(TestData.blockHash) shouldBe None

    val blockInfo = BlockInfo(
      height = 1,
      transactionCount = 0,
      status = 0,
      blockHeader = TestData.block.header,
      blockLocatorOption = None
    )

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

    val blockInfo = BlockInfo(
      height = 1,
      transactionCount = 0,
      status = 0,
      blockHeader = TestData.block.header,
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

    // hit an assertion : put a block info with a block locator, even though the block info has some locator.
    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        blockLocatorOption = Some(BLOCK_LOCATOR)
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
          hashPrevBlock = BlockHash(DUMMY_HASH.value)
        )
      ))
    }

    intercept[AssertionError] {
      db.putBlockInfo(TestData.blockHash, blockInfo.copy(
        blockHeader = blockInfo.blockHeader.copy(
          hashMerkleRoot = MerkleRootHash(DUMMY_HASH.value)
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

    db.putBestBlockHash(DUMMY_HASH)
    db.getBestBlockHash() shouldBe Some(DUMMY_HASH)

    db.putBestBlockHash(TestData.blockHash)
    db.getBestBlockHash() shouldBe Some(TestData.blockHash)
  }

}
