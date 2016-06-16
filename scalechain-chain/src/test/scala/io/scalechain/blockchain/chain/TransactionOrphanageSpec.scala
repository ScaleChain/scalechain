package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

/**
  * Created by kangmo on 6/16/16.
  */
class TransactionOrphanageSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-TransactionOrphangeSpec/")

  import BlockSampleData._
  import BlockSampleData.Tx._
  import BlockSampleData.Block._

  var o : TransactionOrphanage = null
  override def beforeEach() {
    // initialize a test.

    super.beforeEach()

    o = chain.txOrphanage
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
  }

  "putOrphan" should "put an orphan" in {
    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.hasOrphan(TX02.transaction.hash) shouldBe true
  }


  "delOrphan" should "del orphans matching the given hashes" in {
    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.putOrphan(TX03.transaction.hash, TX03.transaction )
    o.putOrphan(TX04a.transaction.hash, TX04a.transaction )
    // first delete all orphans that depends on the transactions to delete from the orphanage.
    o.removeDependenciesOn(TX04a.transaction.hash)
    o.removeDependenciesOn(TX02.transaction.hash)

    o.delOrphan( TX02.transaction.hash )
    o.delOrphan( TX04a.transaction.hash )
    o.getOrphan(TX02.transaction.hash) shouldBe None
    o.getOrphan(TX03.transaction.hash) shouldBe Some(TX03.transaction)
    o.getOrphan(TX04a.transaction.hash) shouldBe None
  }

  "getOrphan" should "return None for a non-existent orphan" in {
    o.getOrphan(TX02.transaction.hash) shouldBe None
  }

  "getOrphan" should "return Some(orphan) for an orphan" in {
    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.putOrphan(TX03.transaction.hash, TX03.transaction )

    o.getOrphan(TX02.transaction.hash) shouldBe Some(TX02.transaction)
    o.getOrphan(TX03.transaction.hash) shouldBe Some(TX03.transaction)
  }

  "hasOrphan" should "return false for a non-existent orphan" in {
    o.hasOrphan(TX02.transaction.hash) shouldBe false
  }

  "hasOrphan" should "return true for an orphan" in {
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
  "getOrphansDependingOn" should "be able to put dependent orphans first" in {
    o.putOrphan(TX02.transaction.hash, TX02.transaction )
    o.putOrphan(TX03.transaction.hash, TX03.transaction )
    o.putOrphan(TX04.transaction.hash, TX04.transaction )

    o.getOrphansDependingOn(GEN01.transaction.hash).toSet shouldBe Set(TX02.transaction.hash)
    o.getOrphansDependingOn(TX02.transaction.hash).toSet shouldBe Set(TX03.transaction.hash, TX04.transaction.hash)
    o.getOrphansDependingOn(TX03.transaction.hash).toSet shouldBe Set(TX04.transaction.hash)
    o.getOrphansDependingOn(TX04.transaction.hash).toSet shouldBe Set()
  }


  "getOrphansDependingOn" should "be able to put depending orphans first" in {
    o.putOrphan(TX04.transaction.hash, TX04.transaction )
    o.putOrphan(TX03.transaction.hash, TX03.transaction )
    o.putOrphan(TX02.transaction.hash, TX02.transaction )

    o.getOrphansDependingOn(GEN01.transaction.hash).toSet shouldBe Set(TX02.transaction.hash)
    o.getOrphansDependingOn(TX02.transaction.hash).toSet shouldBe Set(TX03.transaction.hash, TX04.transaction.hash)
    o.getOrphansDependingOn(TX03.transaction.hash).toSet shouldBe Set(TX04.transaction.hash)
    o.getOrphansDependingOn(TX04.transaction.hash).toSet shouldBe Set()
  }

  "removeDependenciesOn" should "remove dependencies on a transaction" in {

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

  "getOrphansDependingOn" should "return depending orphans if even though the parent was not put yet" in {
    o.putOrphan(TX03.transaction.hash, TX03.transaction )
    o.putOrphan(TX04.transaction.hash, TX04.transaction )

    // Even though TX02, which is the parent of TX03 and TX04 is not put yet, getOrphansDependingOn should be able to return dependent transactions on the TX02.
    o.getOrphansDependingOn(TX02.transaction.hash).toSet shouldBe Set(TX03.transaction.hash, TX04.transaction.hash)
  }

}
