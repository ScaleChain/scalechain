package io.scalechain.blockchain.storage

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.codec.BlockCodec
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto.codec.BlockHeaderCodec
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.record.BlockRecordStorage
import io.scalechain.blockchain.storage.record.RecordStorage
import io.scalechain.blockchain.storage.test.TestData
import io.scalechain.blockchain.storage.test.TestData.block1
import io.scalechain.blockchain.storage.test.TestData.block2
import org.apache.commons.io.FileUtils

/**
  * Created by kangmo on 11/2/15.
  */
class BlockWriterSpec : FlatSpec(), Matchers {

  // Use record storage with maxFileSize 1M, instead of using BlockRecordStorage, which uses 100M file size limit.
  lateinit var writer : BlockWriter
  lateinit var storage : BlockRecordStorage
  override fun beforeEach() {

    val testPath = File("./target/unittests-BlockWriterSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    storage = BlockRecordStorage(testPath, TEST_RECORD_FILE_SIZE)
    writer = BlockWriter(storage)

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    storage.close()
  }

  init {
    Storage.initialize()

    "appendBlock" should "write a block and produce valid locators for each transaction" {
      // Step 1 : Write a block
      val appendBlockResult: AppendBlockResult = writer.appendBlock(block1)

      // Step 2 : Read a block header and check if the block header matches.
      val blockHeader = storage.readRecord(BlockHeaderCodec, appendBlockResult.headerLocator)
      blockHeader shouldBe block1.header

      // Step 3 : Create a map from a transaction hash to transaction locator.
      val txLocatorByHash =
          (appendBlockResult.txLocators.map{ it.txHash } zip
              appendBlockResult.txLocators.map{ it.txLocator }
              ).toMap()

      // Step 4 : Read each transaction on each transaction locator.
      for (transaction in block1.transactions) {
      // Step 4.1 : Calculate transaction hash.
      val txHash = transaction.hash()

      // Step 4.2 : Get the transaction locator.
      val txLocatorOption = txLocatorByHash.get(txHash)

      // Step 4.3 : Read the transaction using the locator
      val readTransaction = storage.readRecord(TransactionCodec, txLocatorOption!!)

      // Step 4.4 : Make sure that the transaction matches.
      readTransaction shouldBe transaction

      // Step 4.5 : Make sure the transaction hash matches.
      readTransaction.hash() shouldBe txHash
    }
    }

    "appendBlock" should "be compatible with BlockCodec. (1 block)" {
      // Step 1 : Write using appendBlock
      val appendBlockResult: AppendBlockResult = writer.appendBlock(block1)

      // Step 2 : Read using BlockCodec
      val readBlock = storage.readRecord(BlockCodec, appendBlockResult.blockLocator)

      readBlock shouldBe block1
    }

    "appendBlock" should "be compatible with BlockCodec. (2 blocks)" {
      // Step 1 : Write using appendBlock
      val appendBlockResult1: AppendBlockResult = writer.appendBlock(block1)
      val appendBlockResult2: AppendBlockResult = writer.appendBlock(block2)

      // Step 2 : Read using BlockCodec
      val readBlock1 = storage.readRecord(BlockCodec, appendBlockResult1.blockLocator)
      val readBlock2 = storage.readRecord(BlockCodec, appendBlockResult2.blockLocator)

      readBlock1 shouldBe block1
      readBlock2 shouldBe block2
    }

    "getTxLocators" should "return the same list of transaction locators returned by appendBlock." {
      // Step 1 : Write using appendBlock
      val appendBlockResult1: AppendBlockResult = writer.appendBlock(block1)
      val appendBlockResult2: AppendBlockResult = writer.appendBlock(block2)

      // Step 2 : check if the list of transaction locators returned by getTxLocators matches the one in the apend block result.
      BlockWriter.getTxLocators(appendBlockResult1.blockLocator, block1) shouldBe appendBlockResult1.txLocators
      BlockWriter.getTxLocators(appendBlockResult2.blockLocator, block2) shouldBe appendBlockResult2.txLocators
    }
  }

  companion object {
    val TEST_RECORD_FILE_SIZE = 1024 * 1024
  }
}



