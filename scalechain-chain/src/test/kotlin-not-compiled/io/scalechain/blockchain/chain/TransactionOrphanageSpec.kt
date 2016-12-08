package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest.*
import HashSupported.*

/**
  * Created by kangmo on 6/16/16.
  */
class TransactionOrphanageSpec : BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = File("./target/unittests-TransactionOrphangeSpec/")

  implicit var keyValueDB : KeyValueDatabase = null

  var o : TransactionOrphanage = null
  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    keyValueDB = db
    // put the genesis block
    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)

    o = chain.txOrphanage
  }

  override fun afterEach() {
    super.afterEach()

    keyValueDB = null
    o = null
    // finalize a test.
  }

  "putOrphan" should "put an orphan" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.hasOrphan(TX02.transaction.hash) shouldBe true
  }


  "delOrphan" should "del orphans matching the given hashes" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.putOrphan(TX03.transaction.hash, TX03.transaction )
    o.putOrphan(TX04a.transaction.hash, TX04a.transaction )
    // first delete all orphans that depends on the transactions to delete from the orphanage.
    o.removeDependenciesOn(TX04a.transaction.hash)
    o.removeDependenciesOn(TX02.transaction.hash)

    o.delOrphan( TX02.transaction.hash )
    o.delOrphan( TX04a.transaction.hash )
    o.getOrphan(TX02.transaction.hash) shouldBe null
    o.getOrphan(TX03.transaction.hash) shouldBe TX03.transaction)
    o.getOrphan(TX04a.transaction.hash) shouldBe null
  }

  "getOrphan" should "return None for a non-existent orphan" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.getOrphan(TX02.transaction.hash) shouldBe null
  }

  "getOrphan" should "return Some(orphan) for an orphan" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.putOrphan(TX03.transaction.hash, TX03.transaction )

    o.getOrphan(TX02.transaction.hash) shouldBe TX02.transaction)
    o.getOrphan(TX03.transaction.hash) shouldBe TX03.transaction)
  }

  "hasOrphan" should "return false for a non-existent orphan" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.hasOrphan(TX02.transaction.hash) shouldBe false
  }

  "hasOrphan" should "return true for an orphan" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.hasOrphan(TX02.transaction.hash) shouldBe true
  }

  /**
    * Transaction Dependency :
    *
    *  GEN01  → TX02 → TX03
    *          ↘      ↘
    *            ↘ → → → → TX04
    */
  "getOrphansDependingOn" should "be able to put dependent orphans first" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.putOrphan(TX03.transaction.hash, TX03.transaction )
    o.putOrphan(TX04.transaction.hash, TX04.transaction )

    o.getOrphansDependingOn(GEN01.transaction.hash).toSet shouldBe Set(TX02.transaction.hash)
    o.getOrphansDependingOn(TX02.transaction.hash).toSet shouldBe Set(TX03.transaction.hash, TX04.transaction.hash)
    o.getOrphansDependingOn(TX03.transaction.hash).toSet shouldBe Set(TX04.transaction.hash)
    o.getOrphansDependingOn(TX04.transaction.hash).toSet shouldBe Set()
  }


  "getOrphansDependingOn" should "be able to put depending orphans first" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.putOrphan(TX04.transaction.hash, TX04.transaction )
    o.putOrphan(TX03.transaction.hash, TX03.transaction )
    o.putOrphan(TX02.transaction.hash, TX02.transaction )

    o.getOrphansDependingOn(GEN01.transaction.hash).toSet shouldBe Set(TX02.transaction.hash)
    o.getOrphansDependingOn(TX02.transaction.hash).toSet shouldBe Set(TX03.transaction.hash, TX04.transaction.hash)
    o.getOrphansDependingOn(TX03.transaction.hash).toSet shouldBe Set(TX04.transaction.hash)
    o.getOrphansDependingOn(TX04.transaction.hash).toSet shouldBe Set()
  }

  "removeDependenciesOn" should "remove dependencies on a transaction" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.putOrphan(TX03.transaction.hash, TX03.transaction )
    o.putOrphan(TX04.transaction.hash, TX04.transaction )

    /**
      * Transaction Dependency before calling removeDependenciesOn(TX02)
      *
      *  TX02 → TX03
      *    ↘       ↘
      *      ↘ → → → → TX04
      */

    o.removeDependenciesOn(TX02.transaction.hash)

    /**
      * Transaction Dependency after calling removeDependenciesOn(TX02)
      *
      *  TX03
      *      ↘
      *       TX04
      */

    o.getOrphansDependingOn(TX02.transaction.hash).toSet shouldBe Set()
    o.getOrphansDependingOn(TX03.transaction.hash).toSet shouldBe Set(TX04.transaction.hash)
    o.getOrphansDependingOn(TX04.transaction.hash).toSet shouldBe Set()
  }

  "getOrphansDependingOn" should "return depending orphans if even though the parent was not put yet" {
    val data = BlockSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*

    o.putOrphan(TX03.transaction.hash, TX03.transaction )
    o.putOrphan(TX04.transaction.hash, TX04.transaction )

    // Even though TX02, which is the parent of TX03 and TX04 is not put yet, getOrphansDependingOn should be able to return dependent transactions on the TX02.
    o.getOrphansDependingOn(TX02.transaction.hash).toSet shouldBe Set(TX03.transaction.hash, TX04.transaction.hash)
  }

}
