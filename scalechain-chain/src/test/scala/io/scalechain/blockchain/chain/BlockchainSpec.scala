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
}