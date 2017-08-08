package io.scalechain.wallet

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.chain.BlockSampleData
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import org.junit.runner.RunWith

/**
  * Test if Wallet returns expected data during block reorganization.
  */
@RunWith(KTestJUnitRunner::class)
class WalletAndChainSpec : WalletTestTrait(), Matchers {

  override val testPath = File("./build/unittests-WalletAndChainSpec-storage/")

  override fun beforeEach() {

    super.beforeEach()
  }

  override fun afterEach() {

    super.afterEach()
  }

  /**
    * List transactions in blocks and the transaction pool.
    *
    * @return The list of transaction hashes.
    */
  fun listTransactionHashes() : List<Hash> {
    return wallet.listTransactions(db, chain, "test account", count=100000, skip=0, includeWatchOnly = true ).map{
      walletTxDesc : WalletTransactionDescriptor ->
      walletTxDesc.txid!!
    }
  }

  /**
    * List transactions in the transaction pool only.
    *
    * @return The list of transaction hashes.
    */
  fun listPoolTransactionHashes() : List<Hash> {
    return chain.txPool.getOldestTransactions(db, 100).map { pair ->
      val txHash : Hash = pair.first
      //val transaction : Transaction = pair.second
      txHash
    }
  }

  init {

    "blockchain" should "reorganize blocks" {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      wallet.importOutputOwnership(db, chain, "test account", data.Addr1.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr2.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr3.address, rescanBlockchain = false)

      listOf(
        T.GEN01,
        T.GEN02, T.TX02,
        T.GEN03b, T.TX03, T.TX03b,
        T.GEN04a, T.TX04, T.TX04a,
        T.GEN04b, T.TX04, T.TX04b, T.TX04b2,
        T.GEN05a, T.TX05a
      ).forEach { t ->
        println("all tx hashes : ${t.name} = ${t.transaction.hash()}")
      }


      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01) // chain work = 4
      listTransactionHashes().toSet() shouldBe setOf( T.GEN01.transaction.hash() )

      listPoolTransactionHashes().toSet() shouldBe setOf<Hash>()

      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02) // chain work = 4 + 4
      listTransactionHashes().toSet() shouldBe listOf(
        T.GEN01,
        T.GEN02, T.TX02
      ).map{ it.transaction.hash() }.toSet()
      listPoolTransactionHashes().toSet() shouldBe setOf<Hash>()


      chain.putBlock(db, B.BLK03a.header.hash(), B.BLK03a) // chain work = 4 + 4 + 4
      listTransactionHashes().toSet() shouldBe listOf(
        T.GEN01,
        T.GEN02, T.TX02,
        T.GEN03a, T.TX03, T.TX03a
      ).map{ it.transaction.hash() }.toSet()

      listPoolTransactionHashes().toSet() shouldBe setOf<Hash>()


      // The chain work is equal to the best blockchain. nothing happens.
      chain.putBlock(db, B.BLK03b.header.hash(), B.BLK03b) // chain work = 4 + 4 + 4
      listTransactionHashes().toSet() shouldBe setOf(
        T.GEN01,
        T.GEN02, T.TX02,
        T.GEN03a, T.TX03, T.TX03a
      ).map{ it.transaction.hash() }.toSet()

      listPoolTransactionHashes().toSet() shouldBe setOf<Hash>()


      chain.putBlock(db, B.BLK04a.header.hash(), B.BLK04a) // chain work = 4 + 4 + 4 + 4
      listTransactionHashes().toSet() shouldBe setOf(
        T.GEN01,
        T.GEN02, T.TX02,
        T.GEN03a, T.TX03, T.TX03a,
        T.GEN04a, T.TX04, T.TX04a
      ).map{ it.transaction.hash() }.toSet()

      listPoolTransactionHashes().toSet() shouldBe setOf<Hash>()


      chain.putBlock(db, B.BLK04b.header.hash(), B.BLK04b) // chain work = 4 + 4 + 4 + 8, block reorg should happen.
      listTransactionHashes().toSet() shouldBe setOf(
        T.GEN01,
        T.GEN02, T.TX02,
        T.GEN03b, T.TX03, T.TX03b,
        T.GEN04b, T.TX04, T.TX04b, T.TX04b2,
        T.TX04a // T.TX04a does not conflict with T.TX04b
      ).map{ it.transaction.hash() }.toSet()

      listPoolTransactionHashes().toSet() shouldBe setOf(T.TX04a.transaction.hash())

      chain.putBlock(db, B.BLK05a.header.hash(), B.BLK05a) // chain work = 4 + 4 + 4 + 4 + 8, block reorg should happen again.
      listTransactionHashes().toSet() shouldBe setOf(
        T.GEN01,
        T.GEN02, T.TX02,
        T.GEN03a, T.TX03, T.TX03a,
        T.GEN04a, T.TX04, T.TX04a,
        T.GEN05a, T.TX05a,

        // T.TX04b can't go into the transaction pool when the B.BLK04a becomes the best block,
        // as it depends on the output T.GEN03b created on the branch b.
        T.TX04b2 // T.TX04b2 do not conflict with other transactions.
      ).map{ it.transaction.hash() }.toSet()

      // T.TX04b2 goes to the transaction pool, as it depends on the unpent output, (T.TX02,2)
      listPoolTransactionHashes().toSet() shouldBe setOf(T.TX04b2.transaction.hash())
    }
  }
}