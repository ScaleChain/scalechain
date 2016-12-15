package io.scalechain.blockchain.storage

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.blockchain.storage.test.TestData.dummyHash
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

/**
  * Created by kangmo on 6/4/16.
  */
@RunWith(KTestJUnitRunner::class)
class OrphanBlockIndexSpec : FlatSpec(), Matchers {

  /** Create a dummy orphan block.
    *
    * @param num should be an integer value to create different dummy orphan blocks.
    * @return The created orphan block.
    */
  fun orphanBlock(num : Long) = OrphanBlockDescriptor(
    Block(
      header = BlockHeader(
        version = 4,
        hashPrevBlock = Hash(bytes("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b")),
        hashMerkleRoot = Hash(bytes("4f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e70")),
        timestamp = 1234567890L,
        target = num,
        nonce = num
      ),
      transactions = listOf()
    )
  )


  val testPath = File("./target/unittests-OrphanBlockIndexSpec")

  lateinit var index : OrphanBlockIndex
  lateinit var db : KeyValueDatabase


  override fun beforeEach() {

    testPath.deleteRecursively()
    testPath.mkdir()

    index = object : OrphanBlockIndex {}
    db = RocksDatabase( testPath )

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()

    testPath.deleteRecursively()
  }

  init {
    Storage.initialize()

    "putOrphanBlock/getOrphanBlock" should "successfully put/get orphan blocks" {
      index.getOrphanBlock(db, dummyHash(1)) shouldBe null
      index.putOrphanBlock(db, dummyHash(1), orphanBlock(1) )
      index.getOrphanBlock(db, dummyHash(1) ) shouldBe orphanBlock(1)

      index.getOrphanBlock(db, dummyHash(2)) shouldBe null
      index.putOrphanBlock(db, dummyHash(2), orphanBlock(2) )
      index.getOrphanBlock(db, dummyHash(2) ) shouldBe orphanBlock(2)
    }

    "delOrphanBlock" should "delete the specified orphan block" {
      index.putOrphanBlock(db, dummyHash(1), orphanBlock(1) )
      index.putOrphanBlock(db, dummyHash(2), orphanBlock(2) )
      index.putOrphanBlock(db, dummyHash(3), orphanBlock(3) )
      index.putOrphanBlock(db, dummyHash(4), orphanBlock(4) )
      index.putOrphanBlock(db, dummyHash(5), orphanBlock(5) )

      index.delOrphanBlock(db, dummyHash(2) )
      index.delOrphanBlock(db, dummyHash(4) )

      index.getOrphanBlock(db, dummyHash(1) ) shouldBe orphanBlock(1)
      index.getOrphanBlock(db, dummyHash(2)) shouldBe null
      index.getOrphanBlock(db, dummyHash(3) ) shouldBe orphanBlock(3)
      index.getOrphanBlock(db, dummyHash(4)) shouldBe null
      index.getOrphanBlock(db, dummyHash(5) ) shouldBe orphanBlock(5)
    }

    "addOrphanBlockByParent/getOrphanBlocksByParent" should "successfully put/get orphan blocks" {
      index.getOrphanBlocksByParent(db, dummyHash(0) ).toSet().isEmpty() shouldBe true
      index.addOrphanBlockByParent(db, dummyHash(0), dummyHash(1) )
      index.getOrphanBlocksByParent(db, dummyHash(0) ).toSet() shouldBe setOf(dummyHash(1))
      index.addOrphanBlockByParent(db, dummyHash(0), dummyHash(2) )
      index.getOrphanBlocksByParent(db, dummyHash(0) ).toSet() shouldBe setOf(dummyHash(1), dummyHash(2))
      index.addOrphanBlockByParent(db, dummyHash(0), dummyHash(3) )
      index.getOrphanBlocksByParent(db, dummyHash(0) ).toSet() shouldBe setOf(dummyHash(1), dummyHash(2), dummyHash(3))
    }

    "delOrphanBlocksByParent" should "delete all orphan blocks that depend on the given parent" {
      index.addOrphanBlockByParent(db, dummyHash(0), dummyHash(1) )
      index.addOrphanBlockByParent(db, dummyHash(0), dummyHash(2) )
      index.addOrphanBlockByParent(db, dummyHash(3), dummyHash(4) )
      index.addOrphanBlockByParent(db, dummyHash(3), dummyHash(5) )
      index.addOrphanBlockByParent(db, dummyHash(6), dummyHash(7) )
      index.addOrphanBlockByParent(db, dummyHash(6), dummyHash(8) )

      index.delOrphanBlocksByParent(db, dummyHash(3) )

      index.getOrphanBlocksByParent(db, dummyHash(0) ).toSet() shouldBe setOf(dummyHash(1), dummyHash(2))
      index.getOrphanBlocksByParent(db, dummyHash(3) ).toSet().isEmpty() shouldBe true
      index.getOrphanBlocksByParent(db, dummyHash(6) ).toSet() shouldBe setOf(dummyHash(7), dummyHash(8))

      index.delOrphanBlocksByParent(db, dummyHash(0) )

      index.getOrphanBlocksByParent(db, dummyHash(0) ).toSet().isEmpty() shouldBe true
      index.getOrphanBlocksByParent(db, dummyHash(3) ).toSet().isEmpty() shouldBe true
      index.getOrphanBlocksByParent(db, dummyHash(6) ).toSet() shouldBe setOf(dummyHash(7), dummyHash(8))

      index.delOrphanBlocksByParent(db, dummyHash(6) )

      index.getOrphanBlocksByParent(db, dummyHash(0) ).isEmpty() shouldBe true
      index.getOrphanBlocksByParent(db, dummyHash(3) ).isEmpty() shouldBe true
      index.getOrphanBlocksByParent(db, dummyHash(6) ).isEmpty() shouldBe true

    }
  }
}
