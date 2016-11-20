package io.scalechain.blockchain.net.handler

import java.io.File

import io.netty.channel.embedded.EmbeddedChannel
import io.scalechain.blockchain.chain.{Blockchain, TransactionSampleData}
import io.scalechain.blockchain.chain.TransactionSampleData

import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.ChainEnvironment
import org.scalatest._
import HashSupported._

class TxMessageHandlerSpec : MessageHandlerTestTrait with Matchers {
  this: Suite =>

  val testPath = File("./target/unittests-TransactionMessageHandlerSpec/")

  implicit var keyValueDB : KeyValueDatabase = null

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()

    keyValueDB = db
    val env = ChainEnvironment.get
    assert(Blockchain.theBlockchain!=null)
    chain.putBlock( env.GenesisBlockHash, env.GenesisBlock )
  }

  override fun afterEach() {
    super.afterEach()

    keyValueDB = null
    // tear-down code
    //
  }

  // TODO : Block Mining : Rewrite test case
  "transaction message handler" should "be able to filter incomplete transaction while mining" ignore {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._

    chain.putBlock( BLK01.header.hash, BLK01 )
    chain.putBlock( BLK02.header.hash, BLK02 )
    chain.putBlock( BLK03.header.hash, BLK03 )

    TxMessageHandler.handle(context, TX04_01.transaction)
    TxMessageHandler.handle(context, TX04_02.transaction)
    TxMessageHandler.handle(context, TX04_03.transaction)

    // This TX04_05_01 ~ TX04_05_05 depends on TX04_04.
    // Note that TX04_04 is not put yet.

    TxMessageHandler.handle(context, TX04_05_01.transaction)
    TxMessageHandler.handle(context, TX04_05_02.transaction)
    TxMessageHandler.handle(context, TX04_05_03.transaction)
    TxMessageHandler.handle(context, TX04_05_04.transaction)
    TxMessageHandler.handle(context, TX04_05_05.transaction)

    val block1 = mineBlock(chain)
    chain.putBlock(block1.header.hash, block1)

    // Drop the genesis transaction and check all transactions are in the block.
    block1.transactions.drop(1) shouldBe List(
      TX04_01.transaction,
      TX04_02.transaction,
      TX04_03.transaction
    )

    // Put the TX04_04. Now TX04_05_01 ~ TX04_05_05 should be mined.
    TxMessageHandler.handle( context, TX04_04.transaction)

    val block2 = mineBlock(chain)
    chain.putBlock(block2.header.hash, block2)

    // Drop the generation transaction and check all transactions are in the block.
    block2.transactions.drop(1) shouldBe List(
      TX04_04.transaction,
      TX04_05_01.transaction,
      TX04_05_02.transaction,
      TX04_05_03.transaction,
      TX04_05_04.transaction,
      TX04_05_05.transaction
    )
  }
}
