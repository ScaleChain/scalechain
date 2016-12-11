package io.scalechain.blockchain.chain

import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait

/**
  * Created by kangmo on 6/16/16.
  */
class TransactionMagnetSpec : BlockchainTestTrait(), TransactionTestDataTrait, Matchers {
  override val testPath = File("./target/unittests-TransactionMagnetSpec/")

  lateinit var tm : TransactionMagnet

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    // put the genesis block
    chain.putBlock(db, env().GenesisBlockHash, env().GenesisBlock)

    tm = chain.txMagnet
  }

  override fun afterEach() {
    super.afterEach()

  }

  init {

    "markOutputSpent" should "" {
/*
      val data = TransactionSampleData(db)
      val B = data.Block
      val T = data.Tx
*/
    }

    "markOutputUnspent" should "" {
    }

    "markAllOutputsUnspent" should "" {
    }

    "detachTransactionInput" should "" {
    }

    "detachTransactionInputs" should "" {
    }

    "detachTransaction" should "" {
    }

    "attachTransactionInput" should "" {
    }

    "attachTransactionInputs" should "" {
    }

    "attachTransaction" should "" {
    }
  }
}
