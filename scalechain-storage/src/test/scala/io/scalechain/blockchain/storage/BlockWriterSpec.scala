package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec, BlockHeaderCodec}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.record.{BlockRecordStorage, RecordStorage}
import io.scalechain.blockchain.storage.test.TestData
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockWriterSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  import TestData._

  Storage.initialize()
  val TEST_RECORD_FILE_SIZE = 1024 * 1024
  // Use record storage with maxFileSize 1M, instead of using BlockRecordStorage, which uses 100M file size limit.
  var writer : BlockWriter = null
  var storage : BlockRecordStorage = null
  override def beforeEach() {

    val testPath = new File("./target/unittests-BlockWriterSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    storage = new BlockRecordStorage(testPath, TEST_RECORD_FILE_SIZE)
    writer = new BlockWriter(storage)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()
  }

  "appendBlock" should "write a block and produce valid locators for each transaction" in {
    // Step 1 : Write a block
    val appendBlockResult : AppendBlockResult = writer.appendBlock(block1)

    // Step 2 : Read a block header and check if the block header matches.
    val blockHeader = storage.readRecord( appendBlockResult.headerLocator)(BlockHeaderCodec)
    blockHeader shouldBe block1.header

    // Step 3 : Create a map from a transaction hash to transaction locator.
    val txLocatorByHash =
        (appendBlockResult.txLocators.map( _.txHash ) zip
         appendBlockResult.txLocators.map(_.txLocator)
        ).toMap

    // Step 4 : Read each transaction on each transaction locator.
    for (transaction <- block1.transactions) {
      // Step 4.1 : Calculate transaction hash.
      val txHash = Hash( HashCalculator.transactionHash(transaction) )
      // Step 4.2 : Get the transaction locator.
      val txLocatorOptin = txLocatorByHash.get(txHash)

      // Step 4.3 : Read the transaction using the locator
      val readTransaction = storage.readRecord(txLocatorOptin.get)(TransactionCodec)

      // Step 4.4 : Make sure that the transaction matches.
      readTransaction shouldBe transaction

      // Step 4.5 : Make sure the transaction hash matches.
      Hash( HashCalculator.transactionHash(readTransaction)) shouldBe txHash
    }
  }

  "appendBlock" should "be compatible with BlockCodec. (1 block)" in {
    // Step 1 : Write using appendBlock
    val appendBlockResult : AppendBlockResult = writer.appendBlock(block1)

    // Step 2 : Read using BlockCodec
    val readBlock = storage.readRecord(appendBlockResult.blockLocator)(BlockCodec)

    readBlock shouldBe block1
  }

  "appendBlock" should "be compatible with BlockCodec. (2 blocks)" in {
    // Step 1 : Write using appendBlock
    val appendBlockResult1 : AppendBlockResult = writer.appendBlock(block1)
    val appendBlockResult2 : AppendBlockResult = writer.appendBlock(block2)

    // Step 2 : Read using BlockCodec
    val readBlock1 = storage.readRecord(appendBlockResult1.blockLocator)(BlockCodec)
    val readBlock2 = storage.readRecord(appendBlockResult2.blockLocator)(BlockCodec)

    readBlock1 shouldBe block1
    readBlock2 shouldBe block2
  }

}

