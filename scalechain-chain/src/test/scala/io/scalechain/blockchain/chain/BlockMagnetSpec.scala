package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._

/**
  * Created by kangmo on 6/16/16.
  */
class BlockMagnetSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-BlockMagnetSpec/")

  import BlockSampleData._
  import BlockSampleData.Tx._
  import BlockSampleData.Block._

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
  }

  "setEventListener" should "" in {
  }

  "detachBlock" should "" in {
  }

  "detachBlocksAfter" should "" in {
  }

  "attachBlock" should "" in {
  }

  "collectBlockInfos" should "" in {
  }


  "attachBlocksAfter" should "" in {
  }

  "reorganize" should "" in {
  }

  "findCommonBlock" should "" in {
  }
}
