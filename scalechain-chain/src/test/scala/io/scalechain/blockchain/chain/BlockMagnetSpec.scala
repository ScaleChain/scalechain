package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

/**
  * Created by kangmo on 6/16/16.
  */
class BlockMagnetSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-BlockMagnetSpec/")

  import BlockSampleData._
  import BlockSampleData.Tx._
  import BlockSampleData.Block._

  var bm : BlockMagnet = null

  override def beforeEach() {
    super.beforeEach()

    // put the genesis block
    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)

    bm = chain.blockMagnet
  }

  override def afterEach() {
    bm = null

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
