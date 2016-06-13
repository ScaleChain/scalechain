package io.scalechain.blockchain.chain.processor

import java.io.File

import io.scalechain.blockchain.chain.{ChainTestDataTrait, BlockchainTestTrait}
import io.scalechain.blockchain.proto.Hash
import org.scalatest._

class BlockProcessorSpec extends BlockchainTestTrait with ChainTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-BlockProcessorTestTrait/")

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
  }

  "getBlock" should "" in {
  }

  "getBlockHeader" should "" in {
  }

  "exists" should "" in {
  }

  "hasOrphan" should "" in {
  }

  "hasNonOrphan" should "" in {
  }

  "putOrphan" should "" in {
  }

  "getOrphanRoot" should "" in {
  }

  "validateBlock" should "" in {
  }

  "delOrphan" should "" in {
  }

  "acceptChildren" should "" in {
  }

  "acceptBlockHeader" should "" in {
  }

}