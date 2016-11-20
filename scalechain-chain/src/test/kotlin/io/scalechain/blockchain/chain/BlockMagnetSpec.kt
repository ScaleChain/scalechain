package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

/**
  * Created by kangmo on 6/16/16.
  */
class BlockMagnetSpec extends BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = new File("./target/unittests-BlockMagnetSpec/")

  implicit var keyValueDB : KeyValueDatabase = null

  var bm : BlockMagnet = null

  override def beforeEach() {
    super.beforeEach()

    keyValueDB = db
    // put the genesis block
    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)

    bm = chain.blockMagnet
  }

  override def afterEach() {
    bm = null
    keyValueDB = null

    super.afterEach()

    // finalize a test.
  }

  "setEventListener" should "" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._

  }

  "detachBlock" should "" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._
  }

  "detachBlocksAfter" should "" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._
  }

  "attachBlock" should "" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._
  }

  "collectBlockInfos" should "" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._
  }


  "attachBlocksAfter" should "" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._
  }

  "reorganize" should "" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._
  }

  "findCommonBlock" should "" in {
    val data = new BlockSampleData()
    import data._
    import data.Tx._
    import data.Block._
  }
}
