package io.scalechain.wallet

import java.io.File


import io.scalechain.blockchain.chain.BlockSampleData
import io.scalechain.blockchain.chain.mining.BlockMining
import io.scalechain.blockchain.chain.{NewOutput, TransactionWithName, BlockSampleData, Blockchain}
import io.scalechain.blockchain.proto.codec.{TransactionCodec, HashCodec}
import io.scalechain.blockchain.proto.{CoinbaseData, TransactionPoolEntry, Hash, Transaction}
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.{TransactionPoolIndex, DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction._
import io.scalechain.test.PerformanceTestTrait
import io.scalechain.util.{StopWatch, StringUtil, GlobalStopWatch}
import org.apache.commons.io.FileUtils
import org.apache.log4j.spi.LoggerFactory
import org.scalatest._
import io.scalechain.blockchain.script.HashSupported._
import scodec.bits.BitVector

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * Created by kangmo on 7/4/16.
  */

class WalletPerformanceSpec : FlatSpec with PerformanceTestTrait with WalletTestTrait with BeforeAndAfterEach with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = File("./target/unittests-WalletPerformanceSpec-storage/")

  implicit var keyValueDB : KeyValueDatabase = null
  override fun beforeEach() {

    super.beforeEach()

    keyValueDB = db
  }

  override fun afterEach() {

    super.afterEach()

    keyValueDB = null
  }

  "perftest" should "measure performance on register transaction" ignore {
    val data = BlockSampleData()
    import data._
    import data.Block._
    import data.Tx._


    import ch.qos.logback.classic.Logger
    val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf<Logger>
    root.setLevel(ch.qos.logback.classic.Level.WARN);


    val transactions = List (
      GEN01,
      GEN02, TX02,
      GEN03a, TX03, TX03a,
      GEN04a, TX04, TX04a,
      GEN05a, TX05a)

    val reverseTransactions = transactions.reverse

    wallet.importOutputOwnership(chain, "test account", Addr1.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr2.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr3.address, rescanBlockchain = false)


//    val TEST_LOOP_COUNT = 100000
    implicit val TEST_LOOP_COUNT = 1000
    var testLoop = TEST_LOOP_COUNT

    println("Performance test started.")
    measure("register transaction") {
      while(testLoop > 0) {
        // register all
        var txIndex : Int = -1
        transactions.map(_.transaction) foreach { tx : Transaction =>
          val txHash = tx.hash
          txIndex += 1

          // registerTransaction is called in addTransactionToPool
          //wallet.registerTransaction(tx, Some(ChainBlock(10, BLK05a)), Some(txIndex))
          chain.txPool.addTransactionToPool(txHash, tx)
        }

        // unregister all
        reverseTransactions.map(_.transaction) foreach { tx : Transaction =>
          val txHash = tx.hash
          chain.txPool.removeTransactionFromPool(txHash)
          wallet.unregisterTransaction(txHash, tx)
        }
        testLoop -= 1
      }
    }
  }

  fun prepareTestTransactions(txCount : Long, data : BlockSampleData = BlockSampleData(), genTxOption : Option<Transaction> = None) : ListBuffer<(Hash, Transaction)> {
    import data._
    import data.Block._
    import data.Tx._

    val generationAddress = wallet.newAddress("generation")
    val generationTx =
      if (genTxOption.isDefined) {
        TransactionWithName("generation transaction", genTxOption.get )
      } else {
        generationTransaction( "GenTx.BLK01", CoinAmount(50), generationAddress )
      }

    // Prepare test data.
    val transactions = ListBuffer<(Hash, Transaction)>()

    var mergedCoin = getOutput(generationTx,0)

    var testLoopCount = txCount

    transactions.append((generationTx.transaction.hash, generationTx.transaction))

    while(testLoopCount > 0) {
      val accountName = s"${Random.nextInt}"
      val newAddress1 = wallet.newAddress(accountName)
      val newAddress2 = wallet.newAddress(accountName)
      val newAddress3 = wallet.newAddress(accountName)

      val coin1Amount = Random.nextInt(10)
      val coin2Amount = Random.nextInt(18)
      val splitTxOrg = normalTransaction(
        s"splitTx-${testLoopCount}",
        spendingOutputs = List( mergedCoin ),
        newOutputs = List(
          NewOutput(CoinAmount(coin1Amount), newAddress1),
          NewOutput(CoinAmount(coin2Amount), newAddress2),
          NewOutput(CoinAmount(50-coin1Amount-coin2Amount), newAddress3)
        )
      )
//      val splitTx = splitTxOrg.copy(transaction = splitTxOrg.transaction.copy(version=coin1Amount))

      val splitTx = splitTxOrg

      transactions.append((splitTx.transaction.hash, splitTx.transaction))

      val mergeTx = normalTransaction(
        s"mergeTx-${testLoopCount}",
        spendingOutputs = List( getOutput(splitTx,0), getOutput(splitTx,1), getOutput(splitTx,2) ),
        newOutputs = List(
          NewOutput(CoinAmount(50), newAddress3)
        )
      )

      transactions.append((mergeTx.transaction.hash, mergeTx.transaction))

      mergedCoin = getOutput(mergeTx, 0)

      testLoopCount -= 2

      if (testLoopCount == (testLoopCount >> 10 << 10)) {
        println (s"${testLoopCount} transactions remaining")
      }
    }

    transactions
  }

  "encoding/decoding key/value" should "measure performance" ignore {
    val data = BlockSampleData()
    import data._
    import data.Block._
    import data.Tx._

    import ch.qos.logback.classic.Logger
    val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf<Logger>
    root.setLevel(ch.qos.logback.classic.Level.WARN);

    wallet.importOutputOwnership(chain, "test account", Addr1.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr2.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr3.address, rescanBlockchain = false)

    println("Preparing Performance test data.")

    implicit val TEST_LOOP_COUNT = 1000
    val transactions = prepareTestTransactions(TEST_LOOP_COUNT)

    val hashes = ListBuffer<Array<Byte>>()
    measureWithSize("encode hash") {
      var totalSize = 0

      transactions foreach { case (txHash, tx) =>
        val rawHash : ByteArray = HashCodec.serialize(txHash)
        val prefixedRawHash = Array('A'.toByte) ++ rawHash
        hashes.append(prefixedRawHash)
        totalSize += rawHash.length
      }

      totalSize
    }


    var prefixSum = 0
    measureWithSize("decode hash") {
      var totalSize = 0

      hashes foreach { prefixedRawHash =>
        val rawPrefix = prefixedRawHash.take(1)
        val rawHash = prefixedRawHash.drop(1)
        prefixSum += HashCodec.parse(rawHash).value(0).toInt
        totalSize += prefixedRawHash.length
      }

      totalSize
    }


    val rawTransactions = ListBuffer<Array<Byte>>()
    measureWithSize("encode transaction") {
      var totalSize = 0

      transactions foreach { case (txHash, tx) =>
        //val rawTx: ByteArray = TransactionCodec.serialize(tx)
        val rawTx = TransactionCodec.serialize(tx)
        rawTransactions.append(rawTx)
        totalSize += rawTx.length
      }
      totalSize
    }

    var sum : Long = 0
    measureWithSize("decode transaction") {
      var totalSize = 0
      rawTransactions foreach { rawTx =>
        sum += TransactionCodec.parse(rawTx).outputs(0).value
        totalSize += rawTx.length
      }
      totalSize
    }

    println(s"Prefix sum ${prefixSum}, Version sum ${sum}")


  }

  "single thread perf test" should "measure performance for mining blocks" in {
    val data = BlockSampleData()
    import data._
    import data.Block._
    import data.Tx._

    import ch.qos.logback.classic.Logger
    val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf<Logger>
    root.setLevel(ch.qos.logback.classic.Level.WARN);

    wallet.importOutputOwnership(chain, "test account", Addr1.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr2.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr3.address, rescanBlockchain = false)

    chain.putBlock(BLK01.header.hash, BLK01)
    chain.putBlock(BLK02.header.hash, BLK02)
    chain.putBlock(BLK03a.header.hash, BLK03a)

    //    val TEST_LOOP_COUNT = 2
    implicit val TEST_LOOP_COUNT = 1000
    var testLoop = TEST_LOOP_COUNT

    println("Preparing Performance test data.")

    val transactions = prepareTestTransactions(TEST_LOOP_COUNT, data, Some(BLK02.transactions(0)))


    measure("single thread Add Transaction") {
      val poolIndex = TransactionPoolIndex {}
      // Drop the first transaction, which is the generation transaction already included in the first block.
      transactions.drop(1) foreach { case (txHash, tx) =>
        chain.withTransaction { transactingDatabase =>
          /*
                  GlobalStopWatch.measure("put-to-pool") {
                    poolIndex.putTransactionToPool(
                      txHash,
                      TransactionPoolEntry(tx, List.fill(tx.outputs.length)(None))
                    )(transactingDatabase)
                  }
                  GlobalStopWatch.measure("put-to-wallet") {
                    wallet.onNewTransaction(txHash, tx, None, None)
                  }
          */
          chain.txPool.addTransactionToPool(txHash, tx)(transactingDatabase)
        }
      }
    }

    val w = StopWatch()

    //val MaxBlockSize = 1024 * 1024 * 2
    val MaxBlockSize = 128 * 1024
    val minedBlockOption = measure("mine block") {
      w.start("COINBASE_MESSAGE")
      val COINBASE_MESSAGE = CoinbaseData(s"height:${chain.getBestBlockHeight()}, ScaleChain by Kwanho, Chanwoo, Kangmo.".getBytes)
      w.stop("COINBASE_MESSAGE")
      // Step 2 : Create the block template
      w.start("getBestBlockHash")
      val bestBlockHash = chain.getBestBlockHash()
      w.stop("getBestBlockHash")
      if (bestBlockHash.isDefined) {

        w.start("blockTemplate")
        val blockTemplate {
          val blockMining = BlockMining(chain.txDescIndex, chain.txPool, chain)(chain.db)
          Some(blockMining.getBlockTemplate(COINBASE_MESSAGE, minerAddress, MaxBlockSize))
        }
        w.stop("blockTemplate")

        if (blockTemplate.isDefined) {
          w.start("getBlockHeader")
          // Step 3 : Get block header
          val blockHeader = blockTemplate.get.getBlockHeader(Hash(bestBlockHash.get.value))
          w.stop("getBlockHeader")


          // Step 3 : Loop until we find a block header hash less than the threshold.
          //            do {
          // TODO : BUGBUG : Need to use chain.getDifficulty instead of using a fixed difficulty

          // TODO : BUGBUG : Remove scalechain.mining.header_hash_threshold configuration after the temporary project finishes

          // Check the best block hash once more.
          w.start("hash compare")
          if (bestBlockHash.get.value == chain.getBestBlockHash().get.value) {
            w.stop("hash compare")
            // Step 5 : When a block is found, create the block and put it on the blockchain.
            // Also propate the block to the peer to peer network.
            w.start("create block")
            val block = blockTemplate.get.createBlock(blockHeader, blockHeader.nonce)
            w.stop("create block")

            w.start("calc block hash")
            val blockHeaderHash = block.header.hash
            w.stop("calc block hash")
            Some(block)
          } else {
            None
          }
        } else {
          println("Not enough signed transactions with the previous block hash.")
          None
        }
      } else {
        println("The best block hash is not defined yet.")
        None
      }

    }(1)

    println(s"result : ${w.toString}")
    println(s"transactions in the block : ${minedBlockOption.get.transactions.length}")
  }

  "single thread perf test" should "measure performance by adding transactions to the pool" ignore {
    val data = BlockSampleData()
    import data._
    import data.Block._
    import data.Tx._

    import ch.qos.logback.classic.Logger
    val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf<Logger>
    root.setLevel(ch.qos.logback.classic.Level.WARN);

    wallet.importOutputOwnership(chain, "test account", Addr1.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr2.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr3.address, rescanBlockchain = false)

    //    val TEST_LOOP_COUNT = 2
    implicit val TEST_LOOP_COUNT = 1000
    var testLoop = TEST_LOOP_COUNT

    println("Preparing Performance test data.")

    val transactions = prepareTestTransactions(TEST_LOOP_COUNT)

    measure("single thread Add Transaction") {
      val poolIndex = TransactionPoolIndex {}
      transactions foreach { case (txHash, tx) =>
        chain.withTransaction { transactingDatabase =>
          /*
                  GlobalStopWatch.measure("put-to-pool") {
                    poolIndex.putTransactionToPool(
                      txHash,
                      TransactionPoolEntry(tx, List.fill(tx.outputs.length)(None))
                    )(transactingDatabase)
                  }
                  GlobalStopWatch.measure("put-to-wallet") {
                    wallet.onNewTransaction(txHash, tx, None, None)
                  }
          */
          chain.txPool.addTransactionToPool(txHash, tx)(transactingDatabase)
        }
      }
    }
  }

  "multi thread perf test" should "measure performance by adding transactions to the pool" ignore {
    val data = BlockSampleData()
    import data._
    import data.Block._
    import data.Tx._

    import ch.qos.logback.classic.Logger
    val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf<Logger>
    root.setLevel(ch.qos.logback.classic.Level.WARN);

    wallet.importOutputOwnership(chain, "test account", Addr1.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr2.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr3.address, rescanBlockchain = false)

    //    val TEST_LOOP_COUNT = 100000

    println("Preparing Performance test data.")

    val TEST_LOOP_COUNT = 1000
    var testLoop = TEST_LOOP_COUNT

    var transactionsMap = scala.collection.mutable.Map<Int, ListBuffer<(Hash, Transaction)>>()

    val threadCount = 4
    0 until threadCount foreach { i =>
      val transactions = prepareTestTransactions(TEST_LOOP_COUNT)
      transactionsMap(i) = transactions
    }


    val threads =
      (0 until threadCount).map { i =>
        Thread() {
          override fun run(): Unit {
            val transactions = transactionsMap(i)
            var addedCount = 0
            transactions foreach { case (hash, tx) =>
              chain.txPool.addTransactionToPool(hash, tx)
              addedCount += 1
            }
            println(s"Added ${addedCount} transactions.")
          }
        }
      }

    implicit val totalTransactions = TEST_LOOP_COUNT * threadCount

    measure("multithread add transaction") {
      threads foreach { _.start } // Start all threads
      threads foreach { _.join }  //
    }
  }
}