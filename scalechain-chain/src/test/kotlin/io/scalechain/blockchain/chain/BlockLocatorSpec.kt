package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.transaction.TransactionTestInterface
import org.junit.runner.RunWith

/**
  * Created by kangmo on 5/28/16.
  */
// Remove the ignore annotation after creating the "by block height" index
@RunWith(KTestJUnitRunner::class)
class BlockLocatorSpec : BlockchainTestTrait(), TransactionTestInterface, Matchers {

  override val testPath = File("./target/unittests-BlockLocatorSpec/")
  lateinit var locator : BlockLocator

  // For testing, override the MAX_HASH_COUNT to 5 so that we get only 5 hashes if the hashStop is all zero.
  val MAX_HASH_COUNT = 5

  override fun beforeEach() {
    super.beforeEach()

    locator = BlockLocator(chain)

    // put hashes into chain.
    chain.putBlock(
      db,
      env().GenesisBlockHash,
      env().GenesisBlock
    )

  }

  override fun afterEach() {
    super.afterEach()
  }

  init {

    "getLocatorHashes" should "return only genesis block hash if there is only the genesis block." {
      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
        env().GenesisBlockHash
      ))
    }

    "getLocatorHashes" should "return all hashes and genesis block if the best block height is 1" {
      putBlocks(1)

      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
        numberToHash(1),
        env().GenesisBlockHash
      ))
    }

    "getLocatorHashes" should "return all hashes and genesis block if the best block height is 2" {
      putBlocks(2)

      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
        numberToHash(2),
        numberToHash(1),
        env().GenesisBlockHash
      ))
    }

    "getLocatorHashes" should "return all hashes and genesis block if the best block height is 10" {
      putBlocks(10)

      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
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

        env().GenesisBlockHash
      ))
    }

    "getLocatorHashes" should "return the first 10 hashes and genesis block if the best block height is 11" {
      putBlocks(11)

      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
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

        env().GenesisBlockHash
      ))

    }

    "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 12" {
      putBlocks(12)

      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
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
        env().GenesisBlockHash
      ))
    }

    "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 13" {
      putBlocks(13)

      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
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
        env().GenesisBlockHash
      ))
    }

    "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 15" {
      putBlocks(15)

      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
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

        env().GenesisBlockHash
      ))
    }


    "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 16" {
      putBlocks(16)

      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
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
        env().GenesisBlockHash
      ))
    }


    "getLocatorHashes" should "return the first 10 hashes and hashes jumped exponentially and genesis block if the best block height is 17" {
      putBlocks(17)

      locator.getLocatorHashes() shouldBe BlockLocatorHashes(listOf(
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

        env().GenesisBlockHash
      ))
    }

    "getHashes" should "return the genesis block if the genesis block is the only hash it matches" {
      putBlocks(32)
      locator.getHashes(
        BlockLocatorHashes(listOf(
          env().GenesisBlockHash
        )
        ),
        env().GenesisBlockHash,
        MAX_HASH_COUNT
      ) shouldBe listOf(
        env().GenesisBlockHash
      )
    }

    "getHashes" should "return the genesis block if no block matches" {
      putBlocks(32)
      locator.getHashes(
        BlockLocatorHashes(listOf(
          SampleData.S2_BlockHash // No block matches.
        )
        ),
        env().GenesisBlockHash,
        MAX_HASH_COUNT
      ) shouldBe listOf(
        env().GenesisBlockHash
      )
    }

    "getHashes" should "return five hashes from the genesis block if no block matches and hashStop is all zero." {
      putBlocks(32)
      locator.getHashes(
        BlockLocatorHashes(listOf(
          SampleData.S2_BlockHash // No block matches. Ignored
        )
        ),
        Hash.ALL_ZERO, // hashStop is all zero : Get 5 hashes.
        MAX_HASH_COUNT
      ) shouldBe listOf(
        env().GenesisBlockHash,
        numberToHash(1),
        numberToHash(2),
        numberToHash(3),
        numberToHash(4)
      )
    }

    "getHashes" should "return matching hashes from the genesis block up to the hashStop if no block matches and hashStop is NOT all zero." {
      putBlocks(32)
      locator.getHashes(
        BlockLocatorHashes(listOf(
          SampleData.S2_BlockHash // No block matches.
        )
        ),
        Hash(numberToHash(3).value), // hashStop 3
        MAX_HASH_COUNT
      ) shouldBe listOf(
        env().GenesisBlockHash,
        numberToHash(1),
        numberToHash(2),
        numberToHash(3) // Stop at 3
      )
    }


    "getHashes" should "return the matching hash only if the stop hash is the matching hash" {
      putBlocks(32)

      locator.getHashes(
        BlockLocatorHashes(listOf(
          Hash(numberToHash(3).value) // block matches at 3
        )
        ),
        Hash(numberToHash(3).value), // hashStop 3
        MAX_HASH_COUNT
      ) shouldBe listOf(
        numberToHash(3) // Only hash 3 is returned.
      )
    }

    "getHashes" should "ignore hashes that are not matched" {
      putBlocks(32)

      locator.getHashes(
        BlockLocatorHashes(listOf(
          SampleData.S2_BlockHash, // No block matches. Ignored
          Hash(numberToHash(3).value) // block matches at 3
        )
        ),
        Hash(numberToHash(3).value), // hashStop 3
        MAX_HASH_COUNT
      ) shouldBe listOf(
        numberToHash(3) // Only hash 3 is returned.
      )
    }

    "getHashes" should "return the matching hash up to the hashStop if the stop hash is in the five hashes from the matching hash" {
      putBlocks(32)

      locator.getHashes(
        BlockLocatorHashes(listOf(
          Hash(numberToHash(3).value) // block matches at 3
        )
        ),
        Hash(numberToHash(6).value), // hashStop 6
        MAX_HASH_COUNT
      ) shouldBe listOf(
        numberToHash(3),
        numberToHash(4),
        numberToHash(5),
        numberToHash(6)
      )


      locator.getHashes(
        BlockLocatorHashes(listOf(
          Hash(numberToHash(3).value) // block matches at 3
        )
        ),
        Hash(numberToHash(7).value), // hashStop 7
        MAX_HASH_COUNT
      ) shouldBe listOf(
        numberToHash(3),
        numberToHash(4),
        numberToHash(5),
        numberToHash(6),
        numberToHash(7)
      )


      locator.getHashes(
        BlockLocatorHashes(listOf(
          Hash(numberToHash(3).value) // block matches at 3
        )
        ),
        Hash(numberToHash(8).value), // hashStop 8, but we can only send five hashes at once.
        MAX_HASH_COUNT
      ) shouldBe listOf(
        numberToHash(3),
        numberToHash(4),
        numberToHash(5),
        numberToHash(6),
        numberToHash(7)
      )
    }


    "getHashes" should "return five hashes from the matching hash if the stop hash is all zero" {
      putBlocks(32)

      locator.getHashes(
        BlockLocatorHashes(listOf(
          Hash(numberToHash(3).value) // block matches at 3
        )
        ),
        Hash.ALL_ZERO, // Get 5 hashes without any hashStop.
        MAX_HASH_COUNT
      ) shouldBe listOf(
        numberToHash(3), // 5 hashes from the hash 3 is returned.
        numberToHash(4),
        numberToHash(5),
        numberToHash(6),
        numberToHash(7)
      )
    }
  }
}
