package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.scalechain.blockchain.proto.Hash
import java.io.File
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.transaction.TransactionTestInterface
import org.junit.runner.RunWith

/**
  * Created by kangmo on 6/16/16.
  */
@RunWith(KTestJUnitRunner::class)
class TransactionOrphanageSpec : BlockchainTestTrait(), TransactionTestInterface, Matchers {

  override val testPath = File("./build/unittests-TransactionOrphangeSpec/")

  lateinit var o : TransactionOrphanage
  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    // put the genesis block
    chain.putBlock(db, env().GenesisBlockHash, env().GenesisBlock)

    o = chain.txOrphanage
  }

  override fun afterEach() {
    super.afterEach()

    // finalize a test.
  }

  init {

    "putOrphan" should "put an orphan" {
      val data = BlockSampleData(db)
      val T = data.Tx

      o.putOrphan(db, T.TX02.transaction.hash(), T.TX02.transaction )
      o.hasOrphan(db, T.TX02.transaction.hash()) shouldBe true
    }


    "delOrphan" should "del orphans matching the given hashes" {
      val data = BlockSampleData(db)
      val T = data.Tx

      o.putOrphan(db, T.TX02.transaction.hash(), T.TX02.transaction )
      o.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction )
      o.putOrphan(db, T.TX04a.transaction.hash(), T.TX04a.transaction )
      // first delete all orphans that depends on the transactions to delete from the orphanage.
      o.removeDependenciesOn(db, T.TX04a.transaction.hash())
      o.removeDependenciesOn(db, T.TX02.transaction.hash())

      o.delOrphan(db, T.TX02.transaction.hash() )
      o.delOrphan(db, T.TX04a.transaction.hash() )
      o.getOrphan(db, T.TX02.transaction.hash()) shouldBe null
      o.getOrphan(db, T.TX03.transaction.hash()) shouldBe T.TX03.transaction
      o.getOrphan(db, T.TX04a.transaction.hash()) shouldBe null
    }

    "getOrphan" should "return None for a non-existent orphan" {
      val data = BlockSampleData(db)
      val T = data.Tx


      o.getOrphan(db, T.TX02.transaction.hash()) shouldBe null
    }

    "getOrphan" should "return Some(orphan) for an orphan" {
      val data = BlockSampleData(db)
      val T = data.Tx


      o.putOrphan(db, T.TX02.transaction.hash(), T.TX02.transaction )
      o.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction )

      o.getOrphan(db, T.TX02.transaction.hash()) shouldBe T.TX02.transaction
      o.getOrphan(db, T.TX03.transaction.hash()) shouldBe T.TX03.transaction
    }

    "hasOrphan" should "return false for a non-existent orphan" {
      val data = BlockSampleData(db)
      val T = data.Tx


      o.hasOrphan(db, T.TX02.transaction.hash()) shouldBe false
    }

    "hasOrphan" should "return true for an orphan" {
      val data = BlockSampleData(db)
      val T = data.Tx


      o.putOrphan(db, T.TX02.transaction.hash(), T.TX02.transaction )
      o.hasOrphan(db, T.TX02.transaction.hash()) shouldBe true
    }

    /**
      * Transaction Dependency :
      *
      *  T.GEN01  → T.TX02 → T.TX03
      *          ↘      ↘
      *            ↘ → → → → T.TX04
      */
    "getOrphansDependingOn" should "be able to put dependent orphans first" {
      val data = BlockSampleData(db)
      val T = data.Tx


      o.putOrphan(db, T.TX02.transaction.hash(), T.TX02.transaction )
      o.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction )
      o.putOrphan(db, T.TX04.transaction.hash(), T.TX04.transaction )

      o.getOrphansDependingOn(db, T.GEN01.transaction.hash()).toSet() shouldBe setOf(T.TX02.transaction.hash())
      o.getOrphansDependingOn(db, T.TX02.transaction.hash()).toSet() shouldBe setOf(T.TX03.transaction.hash(), T.TX04.transaction.hash())
      o.getOrphansDependingOn(db, T.TX03.transaction.hash()).toSet() shouldBe setOf(T.TX04.transaction.hash())
      o.getOrphansDependingOn(db, T.TX04.transaction.hash()).toSet() shouldBe setOf<Hash>()
    }


    "getOrphansDependingOn" should "be able to put depending orphans first" {
      val data = BlockSampleData(db)
      val T = data.Tx


      o.putOrphan(db, T.TX04.transaction.hash(), T.TX04.transaction )
      o.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction )
      o.putOrphan(db, T.TX02.transaction.hash(), T.TX02.transaction )

      o.getOrphansDependingOn(db, T.GEN01.transaction.hash()).toSet() shouldBe setOf(T.TX02.transaction.hash())
      o.getOrphansDependingOn(db, T.TX02.transaction.hash()).toSet() shouldBe setOf(T.TX03.transaction.hash(), T.TX04.transaction.hash())
      o.getOrphansDependingOn(db, T.TX03.transaction.hash()).toSet() shouldBe setOf(T.TX04.transaction.hash())
      o.getOrphansDependingOn(db, T.TX04.transaction.hash()).toSet() shouldBe setOf<Hash>()
    }

    "removeDependenciesOn" should "remove dependencies on a transaction" {
      val data = BlockSampleData(db)
      val T = data.Tx


      o.putOrphan(db, T.TX02.transaction.hash(), T.TX02.transaction )
      o.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction )
      o.putOrphan(db, T.TX04.transaction.hash(), T.TX04.transaction )

      /**
        * Transaction Dependency before calling removeDependenciesOn(T.TX02)
        *
        *  T.TX02 → T.TX03
        *    ↘       ↘
        *      ↘ → → → → T.TX04
        */

      o.removeDependenciesOn(db, T.TX02.transaction.hash())

      /**
        * Transaction Dependency after calling removeDependenciesOn(T.TX02)
        *
        *  T.TX03
        *      ↘
        *       T.TX04
        */

      o.getOrphansDependingOn(db, T.TX02.transaction.hash()).toSet() shouldBe setOf<Hash>()
      o.getOrphansDependingOn(db, T.TX03.transaction.hash()).toSet() shouldBe setOf(T.TX04.transaction.hash())
      o.getOrphansDependingOn(db, T.TX04.transaction.hash()).toSet() shouldBe setOf<Hash>()
    }

    "getOrphansDependingOn" should "return depending orphans if even though the parent was not put yet" {
      val data = BlockSampleData(db)
      val T = data.Tx


      o.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction )
      o.putOrphan(db, T.TX04.transaction.hash(), T.TX04.transaction )

      // Even though T.TX02, which is the parent of T.TX03 and T.TX04 is not put yet, getOrphansDependingOn should be able to return dependent transactions on the T.TX02.
      o.getOrphansDependingOn(db, T.TX02.transaction.hash()).toSet() shouldBe setOf(T.TX03.transaction.hash(), T.TX04.transaction.hash())
    }
  }
}
