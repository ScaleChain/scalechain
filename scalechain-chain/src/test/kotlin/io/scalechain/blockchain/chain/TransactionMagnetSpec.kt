package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.scalechain.blockchain.script.hash
import java.io.File

import io.scalechain.blockchain.transaction.TransactionTestInterface
import org.junit.runner.RunWith

/**
  * Created by kangmo on 6/16/16.
  */
@RunWith(KTestJUnitRunner::class)
class TransactionMagnetSpec : BlockchainTestTrait(), TransactionTestInterface, Matchers {
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

    "attachTransaction" should "attach transactions in order" {
      val data = TransactionSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, data.env().GenesisBlockHash, data.env().GenesisBlock)
      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

      listOf(T.TX04_01, T.TX04_02, T.TX04_03, T.TX04_04).forEach { tx ->
        //println("hash : ${tx.transaction.hash()}")
        tm.attachTransaction(db, tx.transaction.hash(), tx.transaction, false)
      }
    }
  }
}
