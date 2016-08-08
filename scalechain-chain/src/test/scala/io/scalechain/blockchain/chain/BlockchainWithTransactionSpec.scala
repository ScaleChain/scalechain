package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.proto.CoinbaseData
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction._
import org.scalatest._
import HashSupported._


// Remove the ignore annotation after creating the "by block height" index
class BlockchainWithTransactionSpec extends BlockchainTestTrait with ChainTestTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-BlockchainWithTransactionSpec/")

  implicit var keyValueDB : KeyValueDatabase = null

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()

    keyValueDB = db

    chain.putBlock( env.GenesisBlockHash, env.GenesisBlock )
  }

  override def afterEach() {
    super.afterEach()

    keyValueDB = null
    // finalize a test.
  }

  "blockchain" should "be able to create an empty block" in {
    val data = new TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
    chain.putBlock( BLK01.header.hash, BLK01 )
    chain.putBlock( BLK02.header.hash, BLK02 )
    chain.putBlock( BLK03.header.hash, BLK03 )

    val block1 = mineBlock(chain)
    chain.putBlock(block1.header.hash, block1)

    // Drop the generation transaction and check transactions in the block. No transaction should exist.
    block1.transactions.drop(1) shouldBe List()
  }

  "blockchain" should "be able to accept one transaction in a block" in {
    val data = new TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._

    chain.putBlock( BLK01.header.hash, BLK01 )
    chain.putBlock( BLK02.header.hash, BLK02 )
    chain.putBlock( BLK03.header.hash, BLK03 )

    chain.putTransaction(TX04_01.transaction.hash, TX04_01.transaction)

    val block1 = mineBlock(chain)
    chain.putBlock(block1.header.hash, block1)

    // Drop the generation transaction and check all transactions are in the block.
    block1.transactions.drop(1) shouldBe List(
      TX04_01.transaction
    )
  }

  "blockchain" should "be able to accept two transactions in a block" in {
    val data = new TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._

    chain.putBlock( BLK01.header.hash, BLK01 )
    chain.putBlock( BLK02.header.hash, BLK02 )
    chain.putBlock( BLK03.header.hash, BLK03 )

    chain.putTransaction(TX04_01.transaction.hash, TX04_01.transaction)
    chain.putTransaction(TX04_02.transaction.hash, TX04_02.transaction)

    val block1 = mineBlock(chain)
    chain.putBlock(block1.header.hash, block1)

    // Drop the generation transaction and check all transactions are in the block.
    block1.transactions.drop(1) shouldBe List(
      TX04_01.transaction,
      TX04_02.transaction
    )
  }

  "blockchain" should "be able to accept three transactions in a block" in {
    val data = new TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._

    chain.putBlock( BLK01.header.hash, BLK01 )
    chain.putBlock( BLK02.header.hash, BLK02 )
    chain.putBlock( BLK03.header.hash, BLK03 )

    chain.putTransaction(TX04_01.transaction.hash, TX04_01.transaction)
    chain.putTransaction(TX04_02.transaction.hash, TX04_02.transaction)
    chain.putTransaction(TX04_03.transaction.hash, TX04_03.transaction)

    val block1 = mineBlock(chain)
    chain.putBlock(block1.header.hash, block1)

    // Drop the generation transaction and check all transactions are in the block.
    block1.transactions.drop(1) shouldBe List(
      TX04_01.transaction,
      TX04_02.transaction,
      TX04_03.transaction
    )
  }

  "blockchain" should "be able to accept four transactions in a block" in {
    val data = new TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._

    chain.putBlock( BLK01.header.hash, BLK01 )
    chain.putBlock( BLK02.header.hash, BLK02 )
    chain.putBlock( BLK03.header.hash, BLK03 )

    chain.putTransaction(TX04_01.transaction.hash, TX04_01.transaction)
    chain.putTransaction(TX04_02.transaction.hash, TX04_02.transaction)
    chain.putTransaction(TX04_03.transaction.hash, TX04_03.transaction)
    chain.putTransaction(TX04_04.transaction.hash, TX04_04.transaction)

    val block1 = mineBlock(chain)
    chain.putBlock(block1.header.hash, block1)

    // Drop the generation transaction and check all transactions are in the block.
    block1.transactions.drop(1) shouldBe List(
      TX04_01.transaction,
      TX04_02.transaction,
      TX04_03.transaction,
      TX04_04.transaction
    )
  }


  "blockchain" should "be able to accept transactions in two blocks" in {
    val data = new TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._

    chain.putBlock( BLK01.header.hash, BLK01 )
    chain.putBlock( BLK02.header.hash, BLK02 )
    chain.putBlock( BLK03.header.hash, BLK03 )

    chain.putTransaction(TX04_01.transaction.hash, TX04_01.transaction)
    chain.putTransaction(TX04_02.transaction.hash, TX04_02.transaction)
    chain.putTransaction(TX04_03.transaction.hash, TX04_03.transaction)
    chain.putTransaction(TX04_04.transaction.hash, TX04_04.transaction)

    val block1 = mineBlock(chain)
    chain.putBlock(block1.header.hash, block1)

    // Drop the generation transaction and check all transactions are in the block.
    block1.transactions.drop(1) shouldBe List(
      TX04_01.transaction,
      TX04_02.transaction,
      TX04_03.transaction,
      TX04_04.transaction
    )

    chain.putTransaction(TX04_05_01.transaction.hash, TX04_05_01.transaction)
    chain.putTransaction(TX04_05_02.transaction.hash, TX04_05_02.transaction)
    chain.putTransaction(TX04_05_03.transaction.hash, TX04_05_03.transaction)
    chain.putTransaction(TX04_05_04.transaction.hash, TX04_05_04.transaction)
    chain.putTransaction(TX04_05_05.transaction.hash, TX04_05_05.transaction)

    val block2 = mineBlock(chain)
    chain.putBlock(block2.header.hash, block2)

    // Drop the generation transaction and check all transactions are in the block.
    block2.transactions.drop(1) shouldBe List(
      TX04_05_01.transaction,
      TX04_05_02.transaction,
      TX04_05_03.transaction,
      TX04_05_04.transaction,
      TX04_05_05.transaction
    )
  }


  "blockchain" should "be able to accept transactions in one block" in {
    val data = new TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._

    chain.putBlock( BLK01.header.hash, BLK01 )
    chain.putBlock( BLK02.header.hash, BLK02 )
    chain.putBlock( BLK03.header.hash, BLK03 )

    chain.putTransaction(TX04_01.transaction.hash, TX04_01.transaction)
    chain.putTransaction(TX04_02.transaction.hash, TX04_02.transaction)
    chain.putTransaction(TX04_03.transaction.hash, TX04_03.transaction)
    chain.putTransaction(TX04_04.transaction.hash, TX04_04.transaction)

    chain.putTransaction(TX04_05_01.transaction.hash, TX04_05_01.transaction)
    chain.putTransaction(TX04_05_02.transaction.hash, TX04_05_02.transaction)
    chain.putTransaction(TX04_05_03.transaction.hash, TX04_05_03.transaction)
    chain.putTransaction(TX04_05_04.transaction.hash, TX04_05_04.transaction)
    chain.putTransaction(TX04_05_05.transaction.hash, TX04_05_05.transaction)

    val block1 = mineBlock(chain)
    chain.putBlock(block1.header.hash, block1)

    // Drop the generation transaction and check all transactions are in the block.
    block1.transactions.drop(1) shouldBe List(
      TX04_01.transaction,
      TX04_02.transaction,
      TX04_03.transaction,
      TX04_04.transaction,
      TX04_05_01.transaction,
      TX04_05_02.transaction,
      TX04_05_03.transaction,
      TX04_05_04.transaction,
      TX04_05_05.transaction
    )
  }
}