package io.scalechain.blockchain.net.handler

import java.io.File

import io.netty.channel.embedded.EmbeddedChannel
import io.scalechain.blockchain.chain.{Blockchain, BlockchainTestTrait, BlockBuildingTestTrait, TransactionSampleData}
import io.scalechain.blockchain.chain.TransactionSampleData.Block._
import io.scalechain.blockchain.chain.TransactionSampleData.Tx._
import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.transaction.ChainEnvironment
import org.scalatest._
import HashSupported._

class TxMessageHandlerSpec extends MessageHandlerTestTrait with ShouldMatchers {
  this: Suite =>

  val testPath = new File("./target/unittests-TransactionMessageHandlerSpec/")

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()

    val env = ChainEnvironment.get
    assert(Blockchain.theBlockchain!=null)
    chain.putBlock( env.GenesisBlockHash, env.GenesisBlock )
  }

  override def afterEach() {
    super.afterEach()

    // tear-down code
    //
  }


  "transaction message handler" should "be able to filter incomplete transaction while mining" in {
    import TransactionSampleData._
    import TransactionSampleData.Block._
    import TransactionSampleData.Tx._
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

    // Drop the genesis transaction and check all transactions are in the block.
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