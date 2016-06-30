package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.chain.TransactionSampleData.Tx._
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

@Ignore
class BlockMiningSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-BlockMiningSpec/")

  import TransactionSampleData._
  import TransactionSampleData.Tx._
  import TransactionSampleData.Block._

  var bm : BlockMining = null

  override def beforeEach() {
    super.beforeEach()

    // put the genesis block
    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)
    chain.putBlock(BLK01.header.hash, BLK01)
    chain.putBlock(BLK02.header.hash, BLK02)
    chain.putBlock(BLK03.header.hash, BLK03)

    bm = new BlockMining(chain.txPool, chain)
  }

  override def afterEach() {
    bm = null

    super.afterEach()

    // finalize a test.
  }

  "getBlockTemplate" should "" in {
  }

  "selectTransactions" should "select complete transactions with higher fees" in {
    val inputTransactions = List(
      TX04_05_05,
      TX04_05_04,
      TX04_05_03,
      TX04_05_02,
      TX04_05_01,
      TX04_04,
      TX04_03,
      TX04_02,
      TX04_01
    )

    val expectedTransactions = List(
      // TX04_01 ~ TX04_04 => These are dependent transactions, should be sorted by dependency.
      TX04_01, // fee 1
      TX04_02, // fee 2
      TX04_03, // fee 4
      TX04_04, // fee 12
      // TX04_05_0X => These are independent transactions, should be sorted by fee in descending order.
      TX04_05_01, // fee 8
      TX04_05_02, // fee 6
      TX04_05_03, // fee 4
      TX04_05_04, // fee 2
      TX04_05_05  // fee 0
    )

    bm.selectTransactions(GEN04.transaction, inputTransactions.map(_.transaction), 1024 * 1024) shouldBe expectedTransactions.map(_.transaction)
  }


  "selectTransactions" should "exclude incomplete transactions. case 1" in {
    val inputTransactions = List(
      TX04_05_05,
      TX04_05_04,
      TX04_05_03,
      TX04_05_02,
      TX04_05_01,
      TX04_04,
      //TX04_03,
      TX04_02,
      TX04_01
    )

    val expectedTransactions = List(
      // TX04_01 ~ TX04_04 => These are dependent transactions, should be sorted by dependency.
      TX04_01, // fee 1
      TX04_02 // fee 2
    )

    bm.selectTransactions(GEN04.transaction, inputTransactions.map(_.transaction), 1024 * 1024) shouldBe expectedTransactions.map(_.transaction)
  }


  "selectTransactions" should "exclude incomplete transactions. case 2" in {
    val inputTransactions = List(
      TX04_05_05,
      TX04_05_04,
      TX04_05_03,
      TX04_05_02,
      TX04_05_01,
//      TX04_04,
      TX04_03,
      TX04_02,
      TX04_01
    )

    val expectedTransactions = List(
      // TX04_01 ~ TX04_04 => These are dependent transactions, should be sorted by dependency.
      TX04_01, // fee 1
      TX04_02, // fee 2
      TX04_03 // fee 4
    )

    bm.selectTransactions(GEN04.transaction, inputTransactions.map(_.transaction), 1024 * 1024) shouldBe expectedTransactions.map(_.transaction)
  }
}
