package io.scalechain.blockchain.chain

import java.io.File
import java.lang.ref.WeakReference

import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.blockchain.storage.DiskBlockStorage
import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils

trait BlockchainTestTrait : FlatSpec with BeforeAndAfterEach {

  this: Suite =>

  val testPath : File

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var storage : DiskBlockStorage = null
  var chain : Blockchain = null

  implicit var db : KeyValueDatabase = null

  override fun beforeEach() {
    // initialize a test.

    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    val rocksDB = RocksDatabase(testPath)
    db = rocksDB
    storage = DiskBlockStorage(testPath, TEST_RECORD_FILE_SIZE)
    chain = Blockchain(storage)(rocksDB)
    BlockProcessor.theBlockProcessor = null
    BlockProcessor.create(chain)

    Blockchain.theBlockchain = chain

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // finalize a test.
    db.close()
    storage.close()

    storage = null
    chain   = null
    db      = null
    Blockchain.theBlockchain = null

    FileUtils.deleteDirectory(testPath)
  }


  val SampleData = ChainSampleData(None)

  fun createBlock(height : Int ) {
    assert(height > 0)
    SampleData.S1_Block.copy(
      header = SampleData.S1_Block.header.copy(
        hashPrevBlock = Hash( chain.getBlockHash(height-1).value),
        nonce = height
      )
    )
  }

  fun numberToHash(blockHeight : Int) {
    createBlock(blockHeight).header.hash
/*
    Hash( HashCalculator.blockHeaderHash( createBlock(blockHeight).header ).value ) {
      // Put height of the block on the hash for debugging purpose.
      val height = blockHeight
      override fun equals(o : Any) {
        super.equals(this.asInstanceOf<Hash>, o.asInstanceOf<Hash>)
      }
    }
*/
  }


  fun putBlocks(blockCount : Int) {
    for (blockHeight <- 1 to blockCount) {
      val blockHash = numberToHash(blockHeight)
      //println(s"putblocks : ${blockCount}, ${blockHash}, ${createBlock(blockHeight)} ")
      // put a block using genesis block, as we don't check if the block hash matches in the putBlock method.
      chain.putBlock(blockHash, createBlock(blockHeight))
    }
  }
  /*
  fun numberToHash(num : Int) : Hash {
    val hexNum = BigInteger.valueOf(num).toString(16)
    val hexHash = ("000000000000000000000000000000" + hexNum ).takeRight(64)
    Hash(HexUtil.bytes(hexHash))
  }

  fun hashToNumber(hash : Hash) : Int {
    val hexValues = HexUtil.hex(hash.value)
    BigInteger(hexValues, 16).intValue()
  }
  */
}