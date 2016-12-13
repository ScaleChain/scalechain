package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.junit.runner.RunWith


// Remove the ignore annotation after creating the "by block height" index
@RunWith(KTestJUnitRunner::class)
class BlockchainSpec : BlockchainTestTrait(), TransactionTestDataTrait, Matchers {

  override val testPath = File("./target/unittests-BlockchainSpec/")

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // finalize a test.
  }

  init {
    "putBlock" should "put the genesis block" {
      chain.putBlock(db, Hash( env().GenesisBlockHash.value) , env().GenesisBlock)
      val (blockInfo, block) = chain.getBlock(db, env().GenesisBlockHash)!!
      blockInfo.height shouldBe 0
      blockInfo.blockHeader shouldBe env().GenesisBlock.header
      blockInfo.transactionCount shouldBe env().GenesisBlock.transactions.size

      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
      chain.getBlockHash(db, 0) shouldBe env().GenesisBlockHash
    }

    "setEventListener" should "" {
    }

    "setBestBlock" should "" {
    }

    "putBlock" should "" {
    }

    "putBlockHeader" should "" {
    }

    "putTransaction" should "" {
    }

    "getIterator" should "" {
    }

    "getBestBlockHeight" should "" {
    }

    "getBestBlockHash" should "" {
    }

    "getBlockHash" should "" {
    }

    "getBlockInfo" should "" {
    }

    "hasBlock" should "" {
    }

    "getBlock" should "" {
    }

    "getBlockHeader" should "" {
    }

    "getTransaction" should "" {
    }

    "hasTransaction" should "" {
    }

    "getTransactionOutput" should "" {
    }
  }
}
