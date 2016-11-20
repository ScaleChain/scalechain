package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.storage.{DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction.{TransactionTestDataTrait, ChainTestTrait, ChainEnvironment}
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 5/28/16.
  */
// Remove the ignore annotation after creating the "by block height" index
class BlockLocatorSpec extends BlockchainTestTrait with TransactionTestDataTrait with Matchers {
  this: Suite =>

  val testPath = new File("./target/unittests-BlockLocatorSpec/")
  var locator : BlockLocator = null

  // For testing, override the MAX_HASH_COUNT to 5 so that we get only 5 hashes if the hashStop is all zero.
  val MAX_HASH_COUNT = 5

  override def beforeEach() {
    super.beforeEach()

    locator = new BlockLocator(chain)

    // put hashes into chain.
    chain.putBlock(
      env.GenesisBlockHash,
      env.GenesisBlock
    )

  }

  override def afterEach() {
    locator = null

    super.afterEach()
  }

  "getLocatorHashes" should "return only genesis block hash if there is only the genesis block." in {
    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List (
      env.GenesisBlockHash
    ))
  }

  "getLocatorHashes" should "return all hashes and genesis block if the best block height is 1" in {
    putBlocks(1)

    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List (
      numberToHash(1),
      env.GenesisBlockHash
    ))
  }

  "getLocatorHashes" should "return all hashes and genesis block if the best block height is 2" in {
    putBlocks(2)

    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List (
      numberToHash(2),
      numberToHash(1),
      env.GenesisBlockHash
    ))
  }

  "getLocatorHashes" should "return all hashes and genesis block if the best block height is 10" in {
    putBlocks(10)

    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List (
      numberToHash(10), // Add 10 hashes
      numberToHash(9),
      numberToHash(8),
      numberToHash(7),
      numberToHash(6),

      numberToHash(5),
      numberToHash(4),
      numberToHash(3),
      numberToHash(2),
      numberToHash(1),

      env.GenesisBlockHash
    ))
  }

  "getLocatorHashes" should "return the first 10 hashes and genesis block if the best block height is 11" in {
    putBlocks(11)

    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List(
      numberToHash(11), // Add the first 10 hashes
      numberToHash(10),
      numberToHash(9),
      numberToHash(8),
      numberToHash(7),

      numberToHash(6),
      numberToHash(5),
      numberToHash(4),
      numberToHash(3),
      numberToHash(2), // Jump two steps

      env.GenesisBlockHash
    ))

  }

  "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 12" in {
    putBlocks(12)

    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List(
      numberToHash(12), // Add the first 10 hashes
      numberToHash(11),
      numberToHash(10),
      numberToHash(9),
      numberToHash(8),

      numberToHash(7),
      numberToHash(6),
      numberToHash(5),
      numberToHash(4),
      numberToHash(3), // Jump 2 steps : 3 -> 1

      numberToHash(1),
      env.GenesisBlockHash
    ))
  }

  "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 13" in {
    putBlocks(13)

    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List(
      numberToHash(13), // Add the first 10 hashes
      numberToHash(12),
      numberToHash(11),
      numberToHash(10),
      numberToHash(9),

      numberToHash(8),
      numberToHash(7),
      numberToHash(6),
      numberToHash(5),
      numberToHash(4), // Jump 2 steps : 4 -> 2

      numberToHash(2),
      env.GenesisBlockHash
    ))
  }

  "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 15" in {
    putBlocks(15)

    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List(
      numberToHash(15), // Add the first 10 hashes
      numberToHash(14),
      numberToHash(13),
      numberToHash(12),
      numberToHash(11),

      numberToHash(10),
      numberToHash(9),
      numberToHash(8),
      numberToHash(7),
      numberToHash(6), // Jump 2 steps : 6-> 4

      numberToHash(4), // Jump 4 steps : 4-> genesis

      env.GenesisBlockHash
    ))
  }


  "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 16" in {
    putBlocks(16)

    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List(
      numberToHash(16), // Add the first 10 hashes
      numberToHash(15),
      numberToHash(14),
      numberToHash(13),
      numberToHash(12),

      numberToHash(11),
      numberToHash(10),
      numberToHash(9),
      numberToHash(8),
      numberToHash(7), // Jump 2 steps : 7-> 5

      numberToHash(5), // Jump 4 steps : 5-> 1

      numberToHash(1),
      env.GenesisBlockHash
    ))
  }


  "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 17" in {
    putBlocks(17)

    locator.getLocatorHashes() shouldBe BlockLocatorHashes( List(
      numberToHash(17), // Add the first 10 hashes
      numberToHash(16),
      numberToHash(15),
      numberToHash(14),
      numberToHash(13),

      numberToHash(12),
      numberToHash(11),
      numberToHash(10),
      numberToHash(9),
      numberToHash(8), // Jump 2 steps : 8-> 6

      numberToHash(6), // Jump 4 steps : 6-> 2

      numberToHash(2),

      env.GenesisBlockHash
    ))
  }

  "getHashes" should "return the genesis block if the genesis block is the only hash it matches" in {
    putBlocks(32)
    locator.getHashes(
      BlockLocatorHashes( List(
          env.GenesisBlockHash
        )
      ),
      env.GenesisBlockHash,
      MAX_HASH_COUNT
    ) shouldBe List(
        env.GenesisBlockHash
    )
  }

  "getHashes" should "return the genesis block if no block matches" in {
    putBlocks(32)
    locator.getHashes(
      BlockLocatorHashes( List(
        SampleData.S2_BlockHash // No block matches.
      )
      ),
      env.GenesisBlockHash,
      MAX_HASH_COUNT
    ) shouldBe List(
      env.GenesisBlockHash
    )
  }

  "getHashes" should "return five hashes from the genesis block if no block matches and hashStop is all zero." in {
    putBlocks(32)
    locator.getHashes(
      BlockLocatorHashes( List(
        SampleData.S2_BlockHash // No block matches. Ignored
      )
      ),
      ALL_ZERO_HASH, // hashStop is all zero : Get 5 hashes.
      MAX_HASH_COUNT
    ) shouldBe List(
      env.GenesisBlockHash,
      numberToHash(1),
      numberToHash(2),
      numberToHash(3),
      numberToHash(4)
    )
  }

  "getHashes" should "return matching hashes from the genesis block up to the hashStop if no block matches and hashStop is NOT all zero." in {
    putBlocks(32)
    locator.getHashes(
      BlockLocatorHashes( List(
        SampleData.S2_BlockHash // No block matches.
      )
      ),
      Hash( numberToHash(3).value), // hashStop 3
      MAX_HASH_COUNT
    ) shouldBe List(
      env.GenesisBlockHash,
      numberToHash(1),
      numberToHash(2),
      numberToHash(3) // Stop at 3
    )
  }


  "getHashes" should "return the matching hash only if the stop hash is the matching hash" in {
    putBlocks(32)

    locator.getHashes(
      BlockLocatorHashes( List(
        Hash( numberToHash(3).value ) // block matches at 3
      )
      ),
      Hash( numberToHash(3).value), // hashStop 3
      MAX_HASH_COUNT
    ) shouldBe List(
      numberToHash(3) // Only hash 3 is returned.
    )
  }

  "getHashes" should "ignore hashes that are not matched" in {
    putBlocks(32)

    locator.getHashes(
      BlockLocatorHashes( List(
        SampleData.S2_BlockHash, // No block matches. Ignored
        Hash( numberToHash(3).value ) // block matches at 3
      )
      ),
      Hash( numberToHash(3).value), // hashStop 3
      MAX_HASH_COUNT
    ) shouldBe List(
      numberToHash(3) // Only hash 3 is returned.
    )
  }

  "getHashes" should "return the matching hash up to the hashStop if the stop hash is in the five hashes from the matching hash" in {
    putBlocks(32)

    locator.getHashes(
      BlockLocatorHashes( List(
        Hash( numberToHash(3).value ) // block matches at 3
      )
      ),
      Hash( numberToHash(6).value), // hashStop 6
      MAX_HASH_COUNT
    ) shouldBe List(
      numberToHash(3),
      numberToHash(4),
      numberToHash(5),
      numberToHash(6)
    )


    locator.getHashes(
      BlockLocatorHashes( List(
        Hash( numberToHash(3).value ) // block matches at 3
      )
      ),
      Hash( numberToHash(7).value), // hashStop 7
      MAX_HASH_COUNT
    ) shouldBe List(
      numberToHash(3),
      numberToHash(4),
      numberToHash(5),
      numberToHash(6),
      numberToHash(7)
    )


    locator.getHashes(
      BlockLocatorHashes( List(
        Hash( numberToHash(3).value ) // block matches at 3
      )
      ),
      Hash( numberToHash(8).value), // hashStop 8, but we can only send five hashes at once.
      MAX_HASH_COUNT
    ) shouldBe List(
      numberToHash(3),
      numberToHash(4),
      numberToHash(5),
      numberToHash(6),
      numberToHash(7)
    )
  }


  "getHashes" should "return five hashes from the matching hash if the stop hash is all zero" in {
    putBlocks(32)

    locator.getHashes(
      BlockLocatorHashes( List(
        Hash( numberToHash(3).value ) // block matches at 3
      )
      ),
      ALL_ZERO_HASH, // Get 5 hashes without any hashStop.
      MAX_HASH_COUNT
    ) shouldBe List(
      numberToHash(3), // 5 hashes from the hash 3 is returned.
      numberToHash(4),
      numberToHash(5),
      numberToHash(6),
      numberToHash(7)
    )
  }
}
