package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import java.io.File

import io.scalechain.blockchain.transaction.TransactionTestInterface
import io.kotlintest.matchers.Matchers
import org.junit.runner.RunWith

/**
  * Created by kangmo on 6/16/16.
  */
@RunWith(KTestJUnitRunner::class)
class BlockMagnetSpec : BlockchainTestTrait(), TransactionTestInterface, Matchers {

  override val testPath = File("./target/unittests-BlockMagnetSpec/")

  lateinit var bm : BlockMagnet

  override fun beforeEach() {
    super.beforeEach()

    // put the genesis block
    chain.putBlock(db, env().GenesisBlockHash, env().GenesisBlock)

    bm = chain.blockMagnet
  }

  override fun afterEach() {

    super.afterEach()

    // finalize a test.
  }

  init {

    "setEventListener" should "" {
/*
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx
*/
    }

    "detachBlock" should "" {
    }

    "detachBlocksAfter" should "" {
    }

    "attachBlock" should "" {
    }

    "collectBlockInfos" should "" {
    }


    "attachBlocksAfter" should "" {
    }

    "reorganize" should "" {
    }

    "findCommonBlock" should "" {
    }
  }
}
