package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.proto.{BlockHash, Hash}
import io.scalechain.blockchain.storage.{BlockStorage, DiskBlockStorage, Storage}
import io.scalechain.crypto.HashFunctions
import org.apache.commons.io.FileUtils
import org.scalatest._


class BlockchainSpec extends BlockchainTestTrait with ChainTestDataTrait with ShouldMatchers {

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
    chain.putBlock(BlockHash( env.GenesisBlockHash.value) , env.GenesisBlock)
    val Some((blockInfo, block)) = chain.getBlock(env.GenesisBlockHash)
    blockInfo.height shouldBe 0
    blockInfo.blockHeader shouldBe env.GenesisBlock.header
    blockInfo.transactionCount shouldBe env.GenesisBlock.transactions.length

    chain.getBestBlockHash() shouldBe Some(env.GenesisBlockHash)
    chain.getBlockHash(0) shouldBe env.GenesisBlockHash
  }
}