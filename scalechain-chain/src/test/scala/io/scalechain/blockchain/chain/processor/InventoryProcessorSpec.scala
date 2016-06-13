package io.scalechain.blockchain.chain.processor

import java.io.File

import io.scalechain.blockchain.chain.{ChainTestDataTrait, BlockchainTestTrait}
import io.scalechain.blockchain.proto.Hash
import org.scalatest._

class InventoryProcessorSpec extends BlockchainTestTrait with ChainTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-InventoryProcessorTestTrait/")

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
  }

  "alreadyHas" should "" in {
  }
/*
  "alreadyHas" should "" in {
  }
*/

}