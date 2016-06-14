package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.proto.{ Hash}
import io.scalechain.blockchain.storage.{BlockStorage, DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import io.scalechain.crypto.HashFunctions
import org.apache.commons.io.FileUtils
import org.scalatest._

// Remove the ignore annotation after creating the "by block height" index
class BlockchainSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-BlockchainSpec/")

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
  }

  "putBlock" should "put the genesis block" in {
    chain.putBlock(Hash( env.GenesisBlockHash.value) , env.GenesisBlock)
    val Some((blockInfo, block)) = chain.getBlock(env.GenesisBlockHash)
    blockInfo.height shouldBe 0
    blockInfo.blockHeader shouldBe env.GenesisBlock.header
    blockInfo.transactionCount shouldBe env.GenesisBlock.transactions.length

    chain.getBestBlockHash() shouldBe Some(env.GenesisBlockHash)
    chain.getBlockHash(0) shouldBe env.GenesisBlockHash
  }

  "setEventListener" should "" in {
  }

  "setBestBlock" should "" in {
  }

  "putBlock" should "" in {
  }

  "putBlockHeader" should "" in {
  }

  "putTransaction" should "" in {
  }

  "getIterator" should "" in {
  }

  "getBestBlockHeight" should "" in {
  }

  "getBestBlockHash" should "" in {
  }

  "getBlockHash" should "" in {
  }

  "getBlockInfo" should "" in {
  }

  "hasBlock" should "" in {
  }

  "getBlock" should "" in {
  }

  "getBlockHeader" should "" in {
  }

  "getTransaction" should "" in {
  }

  "hasTransaction" should "" in {
  }

  "getTransactionOutput" should "" in {
  }
}