package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.test.TestData
import TestData._
import org.scalatest._

/**
  * Created by kangmo on 3/23/16.
  */
trait BlockStorageTestTrait : FlatSpec with Matchers {
  var storage : BlockStorage
  implicit var db : KeyValueDatabase

  "getBlockHeight" should "return -1 for the hash with all zero values" in {
    storage.getBlockHeight(ALL_ZERO_HASH) shouldBe None
  }

  "getBlockHeight" should "return the height of the block" in {
    storage.putBlockHeader(block1.header)
    storage.putBlockHeader(block2.header)
    storage.getBlockHeight(blockHash1) shouldBe Some(0L)
    storage.getBlockHeight(blockHash2) shouldBe Some(1L)
  }


  "putBlock(block)" should "store a block without hash" in {
    storage.putBlock(block1)
    storage.putBlock(block2)

    storage.getBlock(blockHash1).map(_._2) shouldBe Some(block1)
    storage.getBlock(blockHash2).map(_._2) shouldBe Some(block2)
    storage.hasBlock(blockHash1) shouldBe true
    storage.hasBlock(blockHash2) shouldBe true
  }

  "putBlock(hash,block)" should "pass case 1.1 : block info without a block locator was found." in {
    // Step 1 : put the genesis block header
    storage.putBlockHeader(block1.header)

    // Step 2 : put the genesis block
    storage.putBlock(block1)

    // Step 3 : should get the block
    storage.getBlock(blockHash1).map(_._2) shouldBe Some(block1)
    storage.hasBlock(blockHash1) shouldBe true

  }

  "putBlock(hash,block)" should "pass case 1.2 block info with a block locator was found." in {
    // Step 1 : put the genesis block
    storage.putBlock(block1)

    // Step 2 : put the same block
    storage.putBlock(block1)

    // Step 3 : should get the block
    storage.getBlock(blockHash1).map(_._2) shouldBe Some(block1)
    storage.hasBlock(blockHash1) shouldBe true
  }

  "putBlock(hash,block)" should "pass case 2.1 : no block info was found, previous block header exists. Only the header of the previous block exists." in {
    // Step 1 : put the genesis block header
    storage.putBlockHeader(block1.header)

    // Step 2 : put the second block
    storage.putBlock(block2)

    // Step 3 : should get the blocks
    storage.getBlock(blockHash1) shouldBe None // We put an header only.
    storage.getBlock(blockHash2).map(_._2) shouldBe Some(block2)
    storage.hasBlock(blockHash1) shouldBe false // We put an header only.
    storage.hasBlock(blockHash2) shouldBe true
  }

  "putBlock(hash,block)" should "pass case 2.1 : no block info was found, previous block header exists. Full block data exists for the previous block" in {
    // Step 1 : put the genesis block
    storage.putBlock(block1)

    // Step 2 : put the second block
    storage.putBlock(block2)

    // Step 3 : should get the blocks
    storage.getBlock(blockHash1).map(_._2) shouldBe Some(block1)
    storage.getBlock(blockHash2).map(_._2) shouldBe Some(block2)
    storage.hasBlock(blockHash1) shouldBe true
    storage.hasBlock(blockHash2) shouldBe true
  }


  "putBlock(hash,block)" should "pass case 2.2 : no block info was found, previous block header does not exists." in {
    // Step 1 : put the second block
    storage.putBlock(block2)

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


  "getBlock" should "return None if the block is not found." in {
    storage.getBlock(blockHash1) shouldBe None
  }

  "getBlock" should "return Some(block) if the block is found." in {
    storage.putBlock(block1)
    storage.getBlock(blockHash1).map(_._2) shouldBe Some(block1)
  }

  "getBlock" should "return Some(block) if the block locator was updated." in {
    // Put the block header first. block locator is not set.
    storage.putBlockHeader(block1.header)
    storage.putBlockHeader(block2.header)

    // Put the block into record storage and update the block locator.
    storage.putBlock(block1)
    storage.putBlock(block2)

    // Get the block and make sure the block data matches.
    storage.getBlock(blockHash1).map(_._2) shouldBe Some(block1)
    storage.getBlock(blockHash2).map(_._2) shouldBe Some(block2)
  }


  "getBestBlockHash" should "return None if the best block hash was not put." in {
    storage.getBestBlockHash() shouldBe None
  }

  // The best block hash is not maintained by the storage layer any more. It is maintained by the chain layer.
  /*
  "getBestBlockHash" should "return None if only the block header was put." ignore {
    storage.putBlockHeader(block1.header)
    storage.getBestBlockHash() shouldBe None
  }

  "getBestBlockHash" should "return the best block hash." ignore {
    storage.getBestBlockHash() shouldBe None
    storage.putBlockHeader(block1.header)
    storage.getBestBlockHash() shouldBe None
    storage.putBlockHeader(block2.header)
    storage.getBestBlockHash() shouldBe None
    storage.putBlock(block1)
    storage.getBestBlockHash() shouldBe Some(blockHash1)
    storage.putBlock(block2)
    storage.getBestBlockHash() shouldBe Some(blockHash2)
  }
  */

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
    storage.putBlock(block1)
    storage.getBlockHeader(blockHash1) shouldBe Some(block1.header)
  }

  // This method is a wrapper of the getBlock(Hash). Just do a sanity test.
  "getBlock(Hash)" should "get a block" in {
    storage.putBlock(block1)
    storage.putBlock(block2)

    storage.getBlock(Hash(blockHash1.value)).map(_._2) shouldBe Some(block1)
    storage.getBlock(Hash(blockHash2.value)).map(_._2) shouldBe Some(block2)
  }

  val blockCount = 8
  "getBlock" should "read many blocks correctly" in {
    storage.putBlock(block1)
    var blocksStored = 0
    while( blocksStored < blockCount) {
      val newBlock = block1.copy(
        header = block1.header.copy(
          hashPrevBlock = block1.header.hash
        )
      )
      storage.putBlock(newBlock)

      storage.getBlock(newBlock.header.hash).map(_._2) shouldBe Some(newBlock)

      blocksStored += 1
    }
  }

  "putBlockHashByHeight/getBlockHashByHeight" should "successfully put/get data" in {
    storage.getBlockHashByHeight(0) shouldBe None
    storage.getBlockHashByHeight(1) shouldBe None

    storage.putBlockHashByHeight(0, blockHash1)
    storage.putBlockHashByHeight(1, blockHash2)

    storage.getBlockHashByHeight(0) shouldBe Some(blockHash1)
    storage.getBlockHashByHeight(1) shouldBe Some(blockHash2)
  }

  "updateNextBlockHash" should "successfully update the next block hash" in {
    storage.putBlock(block1)

    storage.getBlockInfo(blockHash1).get.nextBlockHash shouldBe None
    storage.updateNextBlockHash(blockHash1, Some(blockHash2))

    storage.getBlockInfo(blockHash1).get.nextBlockHash shouldBe Some(blockHash2)
  }

  "updateNextBlockHash" should "hit an assertion if the block hash does not exist" in {
    intercept<AssertionError> {
      storage.updateNextBlockHash(blockHash1, None)
    }
  }

  "putBlock" should "return transaction locators" in {

  }

}
