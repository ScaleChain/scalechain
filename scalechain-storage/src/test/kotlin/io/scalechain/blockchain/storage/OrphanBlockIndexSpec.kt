package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.CodecTestUtil
import io.scalechain.blockchain.storage.index.{KeyValueDatabase, RocksDatabase, BlockDatabase}
import io.scalechain.util.HexUtil._
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 6/4/16.
  */
class OrphanBlockIndexSpec : FlatSpec with Matchers with BeforeAndAfterEach {
  this: Suite =>

  Storage.initialize()

  /**
    * Create a dummy hash.
    *
    * @param num should be a one digit integer such as 1 or 2
    * @return The dummy hash value which fills the hash with the given digit.
    */
  fun dummyHash(num : Int) {
    assert(num >= 0 && num <= 9)
    Hash(bytes(num.toString*64))
  }

  /** Create a dummy orphan block.
    *
    * @param num should be an integer value to create different dummy orphan blocks.
    * @return The created orphan block.
    */
  fun orphanBlock(num : Int) = OrphanBlockDescriptor(
    Block(
      header = BlockHeader(
        version = 4,
        hashPrevBlock = Hash(bytes("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b")),
        hashMerkleRoot = Hash(bytes("4f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e70")),
        timestamp = 1234567890L,
        target = num,
        nonce = num
      ),
      transactions = List()
    )
  )

  var index : OrphanBlockIndex = null

  val testPath = File("./target/unittests-OrphanBlockIndexSpec")
  implicit var db : KeyValueDatabase = null


  override fun beforeEach() {

    FileUtils.deleteDirectory( testPath )
    index = OrphanBlockIndex() {}
    db = RocksDatabase( testPath )

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    db.close()
    db = null

    FileUtils.deleteDirectory( testPath )
  }

  "putOrphanBlock/getOrphanBlock" should "successfully put/get orphan blocks" in {
    index.getOrphanBlock( dummyHash(1)) shouldBe None
    index.putOrphanBlock( dummyHash(1), orphanBlock(1) )
    index.getOrphanBlock( dummyHash(1) ) shouldBe Some(orphanBlock(1))

    index.getOrphanBlock( dummyHash(2)) shouldBe None
    index.putOrphanBlock( dummyHash(2), orphanBlock(2) )
    index.getOrphanBlock( dummyHash(2) ) shouldBe Some(orphanBlock(2))
  }

  "delOrphanBlock" should "delete the specified orphan block" in {
    index.putOrphanBlock( dummyHash(1), orphanBlock(1) )
    index.putOrphanBlock( dummyHash(2), orphanBlock(2) )
    index.putOrphanBlock( dummyHash(3), orphanBlock(3) )
    index.putOrphanBlock( dummyHash(4), orphanBlock(4) )
    index.putOrphanBlock( dummyHash(5), orphanBlock(5) )

    index.delOrphanBlock( dummyHash(2) )
    index.delOrphanBlock( dummyHash(4) )

    index.getOrphanBlock( dummyHash(1) ) shouldBe Some(orphanBlock(1))
    index.getOrphanBlock( dummyHash(2)) shouldBe None
    index.getOrphanBlock( dummyHash(3) ) shouldBe Some(orphanBlock(3))
    index.getOrphanBlock( dummyHash(4)) shouldBe None
    index.getOrphanBlock( dummyHash(5) ) shouldBe Some(orphanBlock(5))
  }

  "addOrphanBlockByParent/getOrphanBlocksByParent" should "successfully put/get orphan blocks" in {
    index.getOrphanBlocksByParent( dummyHash(0) ).toSet shouldBe Set()
    index.addOrphanBlockByParent( dummyHash(0), dummyHash(1) )
    index.getOrphanBlocksByParent( dummyHash(0) ).toSet shouldBe Set(dummyHash(1))
    index.addOrphanBlockByParent( dummyHash(0), dummyHash(2) )
    index.getOrphanBlocksByParent( dummyHash(0) ).toSet shouldBe Set(dummyHash(1), dummyHash(2))
    index.addOrphanBlockByParent( dummyHash(0), dummyHash(3) )
    index.getOrphanBlocksByParent( dummyHash(0) ).toSet shouldBe Set(dummyHash(1), dummyHash(2), dummyHash(3))
  }

  "delOrphanBlocksByParent" should "delete all orphan blocks that depend on the given parent" in {
    index.addOrphanBlockByParent( dummyHash(0), dummyHash(1) )
    index.addOrphanBlockByParent( dummyHash(0), dummyHash(2) )
    index.addOrphanBlockByParent( dummyHash(3), dummyHash(4) )
    index.addOrphanBlockByParent( dummyHash(3), dummyHash(5) )
    index.addOrphanBlockByParent( dummyHash(6), dummyHash(7) )
    index.addOrphanBlockByParent( dummyHash(6), dummyHash(8) )

    index.delOrphanBlocksByParent( dummyHash(3) )

    index.getOrphanBlocksByParent( dummyHash(0) ).toSet shouldBe Set(dummyHash(1), dummyHash(2))
    index.getOrphanBlocksByParent( dummyHash(3) ).toSet shouldBe Set()
    index.getOrphanBlocksByParent( dummyHash(6) ).toSet shouldBe Set(dummyHash(7), dummyHash(8))

    index.delOrphanBlocksByParent( dummyHash(0) )

    index.getOrphanBlocksByParent( dummyHash(0) ).toSet shouldBe Set()
    index.getOrphanBlocksByParent( dummyHash(3) ).toSet shouldBe Set()
    index.getOrphanBlocksByParent( dummyHash(6) ).toSet shouldBe Set(dummyHash(7), dummyHash(8))

    index.delOrphanBlocksByParent( dummyHash(6) )

    index.getOrphanBlocksByParent( dummyHash(0) ).toSet shouldBe Set()
    index.getOrphanBlocksByParent( dummyHash(3) ).toSet shouldBe Set()
    index.getOrphanBlocksByParent( dummyHash(6) ).toSet shouldBe Set()

  }
}
