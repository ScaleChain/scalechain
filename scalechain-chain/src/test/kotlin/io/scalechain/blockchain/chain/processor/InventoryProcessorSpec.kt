package io.scalechain.blockchain.chain.processor

import java.io.File

import io.scalechain.blockchain.chain.{BlockSampleData, TransactionSampleData, BlockchainTestTrait}
import io.scalechain.blockchain.proto.{InvVector, InvType, Hash}
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

import scala.util.Random

class InventoryProcessorSpec : BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = File(s"./target/unittests-InventoryProcessorSpec/")

  var t : TransactionProcessor = null
  var b : BlockProcessor = null
  var i : InventoryProcessor = null
  implicit var keyValueDB : KeyValueDatabase = null

  override fun beforeEach() {
    super.beforeEach()

    keyValueDB = db
    t = TransactionProcessor(chain)
    b = BlockProcessor(chain)
    i = InventoryProcessor(chain)
    // Put the genesis block for testing.
    b.acceptBlock(env.GenesisBlockHash, env.GenesisBlock)

  }

  override fun afterEach() {
    keyValueDB = null
    t = null
    b = null
    i = null

    super.afterEach()

    // finalize a test.
  }

  "alreadyHas" should "return true for a block on the blockchain." in {
    val data = BlockSampleData()
    import data._
    import data.Block._
    import data.Tx._

    b.acceptBlock(BLK01.header.hash, BLK01)
    b.acceptBlock(BLK02.header.hash, BLK02)
    b.acceptBlock(BLK03a.header.hash, BLK03a)

    i.alreadyHas( InvVector( InvType.MSG_BLOCK, BLK01.header.hash ) ) shouldBe true
    i.alreadyHas( InvVector( InvType.MSG_BLOCK, BLK02.header.hash ) ) shouldBe true
    i.alreadyHas( InvVector( InvType.MSG_BLOCK, BLK03a.header.hash ) ) shouldBe true
  }

  "alreadyHas" should "return true for an orphan block." in {
    val data = BlockSampleData()
    import data._
    import data.Block._
    import data.Tx._

    i.alreadyHas( InvVector( InvType.MSG_BLOCK, BLK03a.header.hash ) ) shouldBe false
    b.putOrphan(BLK03a)
    i.alreadyHas( InvVector( InvType.MSG_BLOCK, BLK03a.header.hash ) ) shouldBe true
  }

  /*

    "alreadyHas" should "return true for a transaction in a non-orphan block." in {
      val data = BlockSampleData()
      import data._
      import data.Block._
      import data.Tx._

      b.acceptBlock(BLK01.header.hash, BLK01)
      b.acceptBlock(BLK02.header.hash, BLK02)
      b.acceptBlock(BLK03a.header.hash, BLK03a)

      i.alreadyHas( InvVector( InvType.MSG_TX, TX03.transaction.hash ) ) shouldBe true
    }

    "alreadyHas" should "return false for a transaction in an orphan block." in {
      val data = BlockSampleData()
      import data._
      import data.Block._
      import data.Tx._

      b.putOrphan(BLK03a)

      i.alreadyHas( InvVector( InvType.MSG_TX, TX03.transaction.hash ) ) shouldBe false
    }

    "alreadyHas" should "return true for a transaction in the transaction pool." in {
      val data = BlockSampleData()
      import data._
      import data.Block._
      import data.Tx._

      b.acceptBlock(BLK01.header.hash, BLK01)
      b.acceptBlock(BLK02.header.hash, BLK02)
      t.addTransactionToPool(TX03.transaction.hash, TX03.transaction)

      i.alreadyHas( InvVector( InvType.MSG_TX, TX03.transaction.hash ) ) shouldBe true
    }

    "alreadyHas" should "return true for an orphan transaction." in {
      val data = BlockSampleData()
      import data._
      import data.Block._
      import data.Tx._

      t.putOrphan(TX03.transaction.hash, TX03.transaction)

      i.alreadyHas( InvVector( InvType.MSG_TX, TX03.transaction.hash ) ) shouldBe true
    }*/
}