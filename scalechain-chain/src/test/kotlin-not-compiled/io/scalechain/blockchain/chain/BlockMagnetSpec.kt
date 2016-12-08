package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest.*
import HashSupported.*

/**
  * Created by kangmo on 6/16/16.
  */
class BlockMagnetSpec : BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = File("./target/unittests-BlockMagnetSpec/")

  implicit var keyValueDB : KeyValueDatabase = null

  var bm : BlockMagnet = null

  override fun beforeEach() {
    super.beforeEach()

    keyValueDB = db
    // put the genesis block
    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)

    bm = chain.blockMagnet
  }

  override fun afterEach() {
    bm = null
    keyValueDB = null

    super.afterEach()

    // finalize a test.
  }

  "setEventListener" should "" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*

  }

  "detachBlock" should "" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*
  }

  "detachBlocksAfter" should "" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*
  }

  "attachBlock" should "" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*
  }

  "collectBlockInfos" should "" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*
  }


  "attachBlocksAfter" should "" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*
  }

  "reorganize" should "" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*
  }

  "findCommonBlock" should "" {
    val data = BlockSampleData()
    import data.*
    import data.Tx.*
    import data.Block.*
  }
}
