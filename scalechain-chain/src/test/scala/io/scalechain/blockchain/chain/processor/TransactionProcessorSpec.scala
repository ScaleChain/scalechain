package io.scalechain.blockchain.chain.processor

import java.io.File

import io.scalechain.blockchain.chain.{ChainTestDataTrait, BlockchainTestTrait}
import io.scalechain.blockchain.proto.Hash
import org.scalatest._

class TransactionProcessorSpec extends BlockchainTestTrait with ChainTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-TransactionProcessorTestTrait/")

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()


  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
  }

  "exists" should "" in {
  }

  "getTransaction" should "" in {
  }
  "addTransactionToPool" should "" in {
  }

  "acceptChildren" should "" in {
  }

  "delOrphans" should "" in {
  }

  "putOrphan" should "" in {
  }
}