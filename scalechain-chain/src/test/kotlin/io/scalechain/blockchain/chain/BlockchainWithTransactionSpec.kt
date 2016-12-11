package io.scalechain.blockchain.chain

import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.transaction.ChainTestTrait


// Remove the ignore annotation after creating the "by block height" index
class BlockchainWithTransactionSpec : BlockchainTestTrait(), ChainTestTrait, Matchers {

  override val testPath = File("./target/unittests-BlockchainWithTransactionSpec/")

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    chain.putBlock(db, env().GenesisBlockHash, env().GenesisBlock )
  }

  override fun afterEach() {
    super.afterEach()
    // finalize a test.
  }

  init {

    "blockchain" should "be able to create an empty block" {
      val data = TransactionSampleData(db)
      val B = data.Block

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

      val block1 = data.mineBlock(db, chain)
      chain.putBlock(db, block1.header.hash(), block1)

      // Drop the generation transaction and check transactions in the block. No transaction should exist.
      block1.transactions.drop(1) shouldBe listOf<Transaction>()
    }

    "blockchain" should "be able to accept one transaction in a block" {
      val data = TransactionSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

      chain.putTransaction(db, T.TX04_01.transaction.hash(), T.TX04_01.transaction)

      val block1 = data.mineBlock(db, chain)
      chain.putBlock(db, block1.header.hash(), block1)

      // Drop the generation transaction and check all transactions are in the block.
      block1.transactions.drop(1) shouldBe listOf(
        T.TX04_01.transaction
      )
    }

    "blockchain" should "be able to accept two transactions in a block" {
      val data = TransactionSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

      chain.putTransaction(db, T.TX04_01.transaction.hash(), T.TX04_01.transaction)
      chain.putTransaction(db, T.TX04_02.transaction.hash(), T.TX04_02.transaction)

      val block1 = data.mineBlock(db, chain)
      chain.putBlock(db, block1.header.hash(), block1)

      // Drop the generation transaction and check all transactions are in the block.
      block1.transactions.drop(1) shouldBe listOf(
        T.TX04_01.transaction,
        T.TX04_02.transaction
      )
    }

    "blockchain" should "be able to accept three transactions in a block" {
      val data = TransactionSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

      chain.putTransaction(db, T.TX04_01.transaction.hash(), T.TX04_01.transaction)
      chain.putTransaction(db, T.TX04_02.transaction.hash(), T.TX04_02.transaction)
      chain.putTransaction(db, T.TX04_03.transaction.hash(), T.TX04_03.transaction)

      val block1 = data.mineBlock(db, chain)
      chain.putBlock(db, block1.header.hash(), block1)

      // Drop the generation transaction and check all transactions are in the block.
      block1.transactions.drop(1) shouldBe listOf(
        T.TX04_01.transaction,
        T.TX04_02.transaction,
        T.TX04_03.transaction
      )
    }

    "blockchain" should "be able to accept four transactions in a block" {
      val data = TransactionSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

      chain.putTransaction(db, T.TX04_01.transaction.hash(), T.TX04_01.transaction)
      chain.putTransaction(db, T.TX04_02.transaction.hash(), T.TX04_02.transaction)
      chain.putTransaction(db, T.TX04_03.transaction.hash(), T.TX04_03.transaction)
      chain.putTransaction(db, T.TX04_04.transaction.hash(), T.TX04_04.transaction)

      val block1 = data.mineBlock(db, chain)
      chain.putBlock(db, block1.header.hash(), block1)

      // Drop the generation transaction and check all transactions are in the block.
      block1.transactions.drop(1) shouldBe listOf(
        T.TX04_01.transaction,
        T.TX04_02.transaction,
        T.TX04_03.transaction,
        T.TX04_04.transaction
      )
    }


    "blockchain" should "be able to accept transactions in two blocks" {
      val data = TransactionSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

      chain.putTransaction(db, T.TX04_01.transaction.hash(), T.TX04_01.transaction)
      chain.putTransaction(db, T.TX04_02.transaction.hash(), T.TX04_02.transaction)
      chain.putTransaction(db, T.TX04_03.transaction.hash(), T.TX04_03.transaction)
      chain.putTransaction(db, T.TX04_04.transaction.hash(), T.TX04_04.transaction)

      val block1 = data.mineBlock(db, chain)
      chain.putBlock(db, block1.header.hash(), block1)

      // Drop the generation transaction and check all transactions are in the block.
      block1.transactions.drop(1) shouldBe listOf(
        T.TX04_01.transaction,
        T.TX04_02.transaction,
        T.TX04_03.transaction,
        T.TX04_04.transaction
      )

      chain.putTransaction(db, T.TX04_05_01.transaction.hash(), T.TX04_05_01.transaction)
      chain.putTransaction(db, T.TX04_05_02.transaction.hash(), T.TX04_05_02.transaction)
      chain.putTransaction(db, T.TX04_05_03.transaction.hash(), T.TX04_05_03.transaction)
      chain.putTransaction(db, T.TX04_05_04.transaction.hash(), T.TX04_05_04.transaction)
      chain.putTransaction(db, T.TX04_05_05.transaction.hash(), T.TX04_05_05.transaction)

      val block2 = data.mineBlock(db, chain)
      chain.putBlock(db, block2.header.hash(), block2)

      // Drop the generation transaction and check all transactions are in the block.
      block2.transactions.drop(1) shouldBe listOf(
        T.TX04_05_01.transaction,
        T.TX04_05_02.transaction,
        T.TX04_05_03.transaction,
        T.TX04_05_04.transaction,
        T.TX04_05_05.transaction
      )
    }


    "blockchain" should "be able to accept transactions in one block" {
      val data = TransactionSampleData(db)
      val B = data.Block
      val T = data.Tx

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

      chain.putTransaction(db, T.TX04_01.transaction.hash(), T.TX04_01.transaction)
      chain.putTransaction(db, T.TX04_02.transaction.hash(), T.TX04_02.transaction)
      chain.putTransaction(db, T.TX04_03.transaction.hash(), T.TX04_03.transaction)
      chain.putTransaction(db, T.TX04_04.transaction.hash(), T.TX04_04.transaction)

      chain.putTransaction(db, T.TX04_05_01.transaction.hash(), T.TX04_05_01.transaction)
      chain.putTransaction(db, T.TX04_05_02.transaction.hash(), T.TX04_05_02.transaction)
      chain.putTransaction(db, T.TX04_05_03.transaction.hash(), T.TX04_05_03.transaction)
      chain.putTransaction(db, T.TX04_05_04.transaction.hash(), T.TX04_05_04.transaction)
      chain.putTransaction(db, T.TX04_05_05.transaction.hash(), T.TX04_05_05.transaction)

      val block1 = data.mineBlock(db, chain)
      chain.putBlock(db, block1.header.hash(), block1)

      // Drop the generation transaction and check all transactions are in the block.
      block1.transactions.drop(1) shouldBe listOf(
        T.TX04_01.transaction,
        T.TX04_02.transaction,
        T.TX04_03.transaction,
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