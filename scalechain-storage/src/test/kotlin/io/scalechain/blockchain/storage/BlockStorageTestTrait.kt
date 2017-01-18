package io.scalechain.blockchain.storage

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.test.TestData
import io.scalechain.blockchain.storage.test.TestData.block1
import io.scalechain.blockchain.storage.test.TestData.block2
import io.scalechain.blockchain.storage.test.TestData.blockHash2
import io.scalechain.blockchain.storage.test.TestData.blockHash1

/**
  * Created by kangmo on 3/23/16.
  */
abstract class BlockStorageTestTrait : FlatSpec(), Matchers {
  abstract var storage : BlockStorage
  abstract var db : KeyValueDatabase

  fun runTests() {
    "getBlockHeight" should "return -1 for the hash with all zero values" {
      storage.getBlockHeight(db, Hash.ALL_ZERO) shouldBe null
    }

    "getBlockHeight" should "return the height of the block" {
      storage.putBlockHeader(db, block1.header)
      storage.putBlockHeader(db, block2.header)
      storage.getBlockHeight(db, blockHash1) shouldBe 0L
      storage.getBlockHeight(db, blockHash2) shouldBe 1L
    }


    "putBlock(block)" should "store a block without hash" {
      storage.putBlock(db, block1)
      storage.putBlock(db, block2)

      storage.getBlock(db, blockHash1)?.second shouldBe block1
      storage.getBlock(db, blockHash2)?.second shouldBe block2
      storage.hasBlock(db, blockHash1) shouldBe true
      storage.hasBlock(db, blockHash2) shouldBe true
    }

    "putBlock(hash,block)" should "pass case 1.1 : block info without a block locator was found." {
      // Step 1 : put the genesis block header
      storage.putBlockHeader(db, block1.header)

      // Step 2 : put the genesis block
      storage.putBlock(db, block1)

      // Step 3 : should get the block
      storage.getBlock(db, blockHash1)?.second shouldBe block1
      storage.hasBlock(db, blockHash1) shouldBe true

    }

    "putBlock(hash,block)" should "pass case 1.2 block info with a block locator was found." {
      // Step 1 : put the genesis block
      storage.putBlock(db, block1)

      // Step 2 : put the same block
      storage.putBlock(db, block1)

      // Step 3 : should get the block
      storage.getBlock(db, blockHash1)?.second shouldBe block1
      storage.hasBlock(db, blockHash1) shouldBe true
    }

    "putBlock(hash,block)" should "pass case 2.1 : no block info was found, previous block header exists. Only the header of the previous block exists." {
      // Step 1 : put the genesis block header
      storage.putBlockHeader(db, block1.header)

      // Step 2 : put the second block
      storage.putBlock(db, block2)

      // Step 3 : should get the blocks
      storage.getBlock(db, blockHash1) shouldBe null // We put an header only.
      storage.getBlock(db, blockHash2)?.second shouldBe block2
      storage.hasBlock(db, blockHash1) shouldBe false // We put an header only.
      storage.hasBlock(db, blockHash2) shouldBe true
    }

    "putBlock(hash,block)" should "pass case 2.1 : no block info was found, previous block header exists. Full block data exists for the previous block" {
      // Step 1 : put the genesis block
      storage.putBlock(db, block1)

      // Step 2 : put the second block
      storage.putBlock(db, block2)

      // Step 3 : should get the blocks
      storage.getBlock(db, blockHash1)?.second shouldBe block1
      storage.getBlock(db, blockHash2)?.second shouldBe block2
      storage.hasBlock(db, blockHash1) shouldBe true
      storage.hasBlock(db, blockHash2) shouldBe true
    }


    "putBlock(hash,block)" should "pass case 2.2 : no block info was found, previous block header does not exists." {
      // Step 1 : put the second block
      storage.putBlock(db, block2)

      // Step 2 : should get None for the second block hash
      storage.getBlock(db, blockHash2) shouldBe null
      storage.hasBlock(db, blockHash2) shouldBe false
    }

    "putBlockHeader(header)" should "store a block header without hash" {
      storage.putBlockHeader(db, block1.header)
      storage.putBlockHeader(db, block2.header)
      storage.getBlockHeader(db, blockHash1) shouldBe block1.header
      storage.getBlockHeader(db, blockHash2) shouldBe block2.header
      // We don't have a block, but we have hash only
      storage.hasBlock(db, blockHash1) shouldBe false
      storage.hasBlock(db, blockHash2) shouldBe false
    }

    "putBlockHeader(hash,header)" should "pass case 1.1 : the header does not exist yet." {
      // The header does not exist yet.
      storage.putBlockHeader(db, blockHash1, block1.header)
      storage.getBlockHeader(db, blockHash1) shouldBe block1.header

      // We don't have a block, but we have hash only.
      storage.hasBlock(db, blockHash1) shouldBe false
    }

    "putBlockHeader(hash,header)" should "pass case 1.2 : the same block header already exists" {
      storage.putBlockHeader(db, blockHash1, block1.header)
      // put the same header : nothing happens.
      storage.putBlockHeader(db, blockHash1, block1.header)
      storage.getBlockHeader(db, blockHash1) shouldBe block1.header

      // We don't have a block, but we have hash only.
      storage.hasBlock(db, blockHash1) shouldBe false
    }

    "putBlockHeader(hash,header)" should "pass case 2 : the previous block header was not found" {
      // The previous block header, block1.header is not stored.
      storage.putBlockHeader(db, block2.header)
      // Because the previous block header was not found, the block2.header is not stored.
      storage.getBlockHeader(db, blockHash2) shouldBe null

      // We don't have a block, but we have hash only.
      storage.hasBlock(db, blockHash2) shouldBe false
    }


    "getBlock" should "return None if the block is not found." {
      storage.getBlock(db, blockHash1) shouldBe null
    }

    "getBlock" should "return Some(block) if the block is found." {
      storage.putBlock(db, block1)
      storage.getBlock(db, blockHash1)?.second shouldBe block1
    }

    "getBlock" should "return Some(block) if the block locator was updated." {
      // Put the block header first. block locator is not set.
      storage.putBlockHeader(db, block1.header)
      storage.putBlockHeader(db, block2.header)

      // Put the block into record storage and update the block locator.
      storage.putBlock(db, block1)
      storage.putBlock(db, block2)

      // Get the block and make sure the block data matches.
      storage.getBlock(db, blockHash1)?.second shouldBe block1
      storage.getBlock(db, blockHash2)?.second shouldBe block2
    }


    "getBestBlockHash" should "return None if the best block hash was not put." {
      storage.getBestBlockHash(db) shouldBe null
    }

    // The best block hash is not maintained by the storage layer any more. It is maintained by the chain layer.
    /*
    "getBestBlockHash" should "return None if only the block header was put." ignore {
      storage.putBlockHeader(block1.header)
      storage.getBestBlockHash() shouldBe null
    }

    "getBestBlockHash" should "return the best block hash." ignore {
      storage.getBestBlockHash() shouldBe null
      storage.putBlockHeader(block1.header)
      storage.getBestBlockHash() shouldBe null
      storage.putBlockHeader(block2.header)
      storage.getBestBlockHash() shouldBe null
      storage.putBlock(block1)
      storage.getBestBlockHash() shouldBe blockHash1)
      storage.putBlock(block2)
      storage.getBestBlockHash() shouldBe blockHash2)
    }
    */

    "hasBlock" should "return false if the block does not exist." {
      storage.hasBlock(db, blockHash1) shouldBe false
      storage.hasBlock(db, blockHash2) shouldBe false
    }

    "hasBlock" should "return true if the block exists." {
      // The test with hasBlock was done in the putBlock/putBlockHeader case.
    }

    "getBlockHeader" should "return None if the block header does not exist." {
      storage.getBlockHeader(db, blockHash1) shouldBe null
    }

    "getBlockHeader" should "return Some(block header) if the block header was put." {
      storage.putBlockHeader(db, block1.header)
      storage.getBlockHeader(db, blockHash1) shouldBe block1.header
    }

    "getBlockHeader" should "return Some(block header) if the block was put." {
      storage.putBlock(db, block1)
      storage.getBlockHeader(db, blockHash1) shouldBe block1.header
    }

    "hasBlockHeader" should "return false if the block header does not exist" {
      storage.hasBlockHeader(db, blockHash1) shouldBe false
    }

    "hasBlockHeader" should "return true if the block header exists" {
      storage.putBlockHeader(db, block1.header)
      storage.hasBlockHeader(db, blockHash1) shouldBe true
    }


    // This method is a wrapper of the getBlock(Hash). Just do a sanity test.
    "getBlock(Hash)" should "get a block" {
      storage.putBlock(db, block1)
      storage.putBlock(db, block2)

      storage.getBlock(db, Hash(blockHash1.value))?.second shouldBe block1
      storage.getBlock(db, Hash(blockHash2.value))?.second shouldBe block2
    }

    val blockCount = 8
    "getBlock" should "read many blocks correctly" {
      storage.putBlock(db, block1)
      var blocksStored = 0
      while( blocksStored < blockCount) {
        val newBlock = block1.copy(
            header = block1.header.copy(
                hashPrevBlock = block1.header.hash()
            )
        )
        storage.putBlock(db, newBlock)

        storage.getBlock(db, newBlock.header.hash())?.second shouldBe newBlock

        blocksStored += 1
      }
    }

    "putBlockHashByHeight/getBlockHashByHeight" should "successfully put/get data" {
      storage.getBlockHashByHeight(db, 0) shouldBe null
      storage.getBlockHashByHeight(db, 1) shouldBe null

      storage.putBlockHashByHeight(db, 0, blockHash1)
      storage.putBlockHashByHeight(db, 1, blockHash2)

      storage.getBlockHashByHeight(db, 0) shouldBe blockHash1
      storage.getBlockHashByHeight(db, 1) shouldBe blockHash2
    }

    "updateNextBlockHash" should "successfully update the next block hash" {
      storage.putBlock(db, block1)

      storage.getBlockInfo(db, blockHash1)?.nextBlockHash shouldBe null
      storage.updateNextBlockHash(db, blockHash1, blockHash2)

      storage.getBlockInfo(db, blockHash1)?.nextBlockHash shouldBe blockHash2
      storage.getNextBlockHash(db, blockHash1) shouldBe blockHash2
    }

    "updateNextBlockHash" should "hit an assertion if the block hash does not exist" {
      shouldThrow<AssertionError> {
        storage.updateNextBlockHash(db, blockHash1, null)
      }
    }
  }
}
