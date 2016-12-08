package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.proto.{ Hash}
import io.scalechain.blockchain.storage.{BlockStorage, DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import io.scalechain.crypto.HashFunctions
import org.apache.commons.io.FileUtils
import org.scalatest.*

// Remove the ignore annotation after creating the "by block height" index
class BlockchainSpec : BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = File("./target/unittests-BlockchainSpec/")

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // finalize a test.
  }

  "putBlock" should "put the genesis block" {
    chain.putBlock(Hash( env.GenesisBlockHash.value) , env.GenesisBlock)
    val Some((blockInfo, block)) = chain.getBlock(env.GenesisBlockHash)
    blockInfo.height shouldBe 0
    blockInfo.blockHeader shouldBe env.GenesisBlock.header
    blockInfo.transactionCount shouldBe env.GenesisBlock.transactions.length

    chain.getBestBlockHash() shouldBe env.GenesisBlockHash)
    chain.getBlockHash(0) shouldBe env.GenesisBlockHash
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