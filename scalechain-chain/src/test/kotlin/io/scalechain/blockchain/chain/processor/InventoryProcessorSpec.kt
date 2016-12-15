package io.scalechain.blockchain.chain.processor

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.chain.BlockSampleData
import io.scalechain.blockchain.chain.TransactionSampleData
import io.scalechain.blockchain.chain.BlockchainTestTrait
import io.scalechain.blockchain.proto.InvVector
import io.scalechain.blockchain.proto.InvType
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestInterface
import org.junit.runner.RunWith

import scala.util.Random

@RunWith(KTestJUnitRunner::class)
class InventoryProcessorSpec : BlockchainTestTrait(), TransactionTestInterface, Matchers {

  override val testPath = File("./target/unittests-InventoryProcessorSpec/")

  lateinit var t : TransactionProcessor
  lateinit var b : BlockProcessor
  lateinit var i : InventoryProcessor

  override fun beforeEach() {
    super.beforeEach()

    t = TransactionProcessor(chain)
    b = BlockProcessor(db, chain)
    i = InventoryProcessor(db, chain)
    // Put the genesis block for testing.
    b.acceptBlock(env().GenesisBlockHash, env().GenesisBlock)

  }

  override fun afterEach() {
    super.afterEach()

    // finalize a test.
  }
  
  init {

    "alreadyHas" should "return true for a block on the blockchain." {
      val data = BlockSampleData(db)
      val B = data.Block

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      b.acceptBlock(B.BLK03a.header.hash(), B.BLK03a)

      i.alreadyHas(InvVector(InvType.MSG_BLOCK, B.BLK01.header.hash())) shouldBe true
      i.alreadyHas(InvVector(InvType.MSG_BLOCK, B.BLK02.header.hash())) shouldBe true
      i.alreadyHas(InvVector(InvType.MSG_BLOCK, B.BLK03a.header.hash())) shouldBe true
    }

    "alreadyHas" should "return true for an orphan block." {
      val data = BlockSampleData(db)
      val B = data.Block

      i.alreadyHas(InvVector(InvType.MSG_BLOCK, B.BLK03a.header.hash())) shouldBe false
      b.putOrphan(B.BLK03a)
      i.alreadyHas(InvVector(InvType.MSG_BLOCK, B.BLK03a.header.hash())) shouldBe true
    }

    /*
    "alreadyHas".config(ignored=true) should "return true for a transaction in a non-orphan block." {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      b.acceptBlock(B.BLK03a.header.hash(), B.BLK03a)

      i.alreadyHas( InvVector( InvType.MSG_TX, T.TX03.transaction.hash() ) ) shouldBe true
    }

    "alreadyHas".config(ignored=true) should "return false for a transaction in an orphan block." {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      b.putOrphan(B.BLK03a)

      i.alreadyHas( InvVector( InvType.MSG_TX, T.TX03.transaction.hash() ) ) shouldBe false
    }

    "alreadyHas".config(ignored=true) should "return true for a transaction in the transaction pool." {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      b.acceptBlock(B.BLK01.header.hash(), B.BLK01)
      b.acceptBlock(B.BLK02.header.hash(), B.BLK02)
      t.addTransactionToPool(T.TX03.transaction.hash(), T.TX03.transaction)

      i.alreadyHas( InvVector( InvType.MSG_TX, T.TX03.transaction.hash() ) ) shouldBe true
    }

    "alreadyHas".config(ignored=true) should "return true for an orphan transaction." {
      val data = BlockSampleData(db)
      val B = data.Block
      val T = data.Tx

      t.putOrphan(db, T.TX03.transaction.hash(), T.TX03.transaction)

      i.alreadyHas( InvVector( InvType.MSG_TX, T.TX03.transaction.hash() ) ) shouldBe true
    }
  */
  }
}