package io.scalechain.blockchain.chain

import io.kotlintest.specs.FlatSpec
import java.io.File
import java.lang.ref.WeakReference

import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.blockchain.storage.DiskBlockStorage
import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import java.util.concurrent.atomic.AtomicInteger


abstract class BlockchainTestTrait : FlatSpec() {

  abstract val testPath : File


  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  lateinit var storage : DiskBlockStorage
  lateinit var chain : Blockchain

  lateinit var db : KeyValueDatabase
  lateinit var SampleData : ChainSampleData

  init {
    Storage.initialize()
  }

  override fun beforeEach() {
    super.beforeEach()
    println("beforeEach init++++++++++++")

    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    val rocksDB = RocksDatabase(testPath)
    db = rocksDB

    SampleData = ChainSampleData(db, null)

    storage = DiskBlockStorage(db, testPath, TEST_RECORD_FILE_SIZE)
    chain = Blockchain(rocksDB, storage)
    BlockProcessor.theBlockProcessor = null
    BlockProcessor.create(chain)

    Blockchain.theBlockchain = chain

    println("beforeEach done++++++++++++")
  }

  override fun afterEach() {
    println("afterEach init------------")
    super.afterEach()

    // finalize a test.
    storage.close()
    db.close()

    Blockchain.theBlockchain = null

    FileUtils.deleteDirectory(testPath)

    println("afterEach done------------")
  }

  fun createBlock(height : Long ) : Block {
    assert(height > 0)
    return SampleData.S1_Block.copy(
      header = SampleData.S1_Block.header.copy(
        hashPrevBlock = Hash( chain.getBlockHash(db, height-1).value),
        nonce = height
      )
    )
  }

  fun numberToHash(blockHeight : Int) : Hash {
    return createBlock(blockHeight.toLong()).header.hash()
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
    for (blockHeight in 1 .. blockCount) {
      val blockHash = numberToHash(blockHeight)
      //println(s"putblocks : ${blockCount}, ${blockHash}, ${createBlock(blockHeight)} ")
      // put a block using genesis block, as we don't check if the block hash matches in the putBlock method.
      chain.putBlock(db, blockHash, createBlock(blockHeight.toLong()))
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
