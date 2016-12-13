package io.scalechain.blockchain.net.handler

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.TransactionSampleData
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.transaction.ChainEnvironment
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TxMessageHandlerSpec : MessageHandlerTestTrait(), Matchers {

  override val testPath = File("./target/unittests-TransactionMessageHandlerSpec/")

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()

    val env = ChainEnvironment.get()
    assert(Blockchain.theBlockchain!=null)
    chain.putBlock(db, env.GenesisBlockHash, env.GenesisBlock )
  }

  override fun afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  init {

    // TODO : Block Mining : Rewrite test case
    "transaction message handler".config(ignored=true) should "be able to filter incomplete transaction while mining" {
      val data = TransactionSampleData(db)
      val T = data.Tx
      val B = data.Block

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

      TxMessageHandler.handle(context, T.TX04_01.transaction)
      TxMessageHandler.handle(context, T.TX04_02.transaction)
      TxMessageHandler.handle(context, T.TX04_03.transaction)

      // This T.TX04_05_01 ~ T.TX04_05_05 depends on T.TX04_04.
      // Note that T.TX04_04 is not put yet.

      TxMessageHandler.handle(context, T.TX04_05_01.transaction)
      TxMessageHandler.handle(context, T.TX04_05_02.transaction)
      TxMessageHandler.handle(context, T.TX04_05_03.transaction)
      TxMessageHandler.handle(context, T.TX04_05_04.transaction)
      TxMessageHandler.handle(context, T.TX04_05_05.transaction)

      val block1 = data.mineBlock(db, chain)
      chain.putBlock(db, block1.header.hash(), block1)

      // Drop the genesis transaction and check all transactions are in the block.
      block1.transactions.drop(1) shouldBe listOf(
        T.TX04_01.transaction,
        T.TX04_02.transaction,
        T.TX04_03.transaction
      )

      // Put the T.TX04_04. Now T.TX04_05_01 ~ T.TX04_05_05 should be mined.
      TxMessageHandler.handle(context, T.TX04_04.transaction)

      val block2 = data.mineBlock(db, chain)
      chain.putBlock(db, block2.header.hash(), block2)

      // Drop the generation transaction and check all transactions are in the block.
      block2.transactions.drop(1) shouldBe listOf(
        T.TX04_04.transaction,
        T.TX04_05_01.transaction,
        T.TX04_05_02.transaction,
        T.TX04_05_03.transaction,
        T.TX04_05_04.transaction,
        T.TX04_05_05.transaction
      )
    }
  }
}
