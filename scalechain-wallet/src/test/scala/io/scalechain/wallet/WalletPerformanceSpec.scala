package io.scalechain.wallet

import java.io.File


import io.scalechain.blockchain.chain.BlockSampleData.Block._
import io.scalechain.blockchain.chain.BlockSampleData.Tx._
import io.scalechain.blockchain.chain.BlockSampleData._
import io.scalechain.blockchain.chain.{NewOutput, TransactionWithName, BlockSampleData, Blockchain}
import io.scalechain.blockchain.proto.{Hash, Transaction}
import io.scalechain.blockchain.storage.{DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction.TransactionSigner.SignedTransaction
import io.scalechain.blockchain.transaction._
import org.apache.commons.io.FileUtils
import org.apache.log4j.spi.LoggerFactory
import org.scalatest._
import io.scalechain.blockchain.script.HashSupported._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * Created by kangmo on 7/4/16.
  */

class WalletPerformanceSpec extends FlatSpec with BeforeAndAfterEach with TransactionTestDataTrait with Matchers {

  this: Suite =>

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var wallet: Wallet = null
  var storage: DiskBlockStorage = null
  var chain: Blockchain = null

  val testPathForWallet = new File("./target/unittests-WalletPerformanceSpec-wallet/")
  val testPathForStorage = new File("./target/unittests-WalletPerformanceSpec-storage/")

  override def beforeEach() {
    FileUtils.deleteDirectory(testPathForWallet)
    FileUtils.deleteDirectory(testPathForStorage)
    testPathForWallet.mkdir()
    testPathForStorage.mkdir()

    storage = new DiskBlockStorage(testPathForStorage, TEST_RECORD_FILE_SIZE)
    DiskBlockStorage.theBlockStorage = storage

    chain = new Blockchain(storage)
    Blockchain.theBlockchain = chain

    wallet = Wallet.create(testPathForWallet)
    chain.setEventListener(wallet)

    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()
    wallet.close()

    storage = null
    chain = null
    wallet = null

    FileUtils.deleteDirectory(testPathForWallet)
    FileUtils.deleteDirectory(testPathForStorage)
  }


  "perftest" should "measure performance on register transaction" ignore {
    import BlockSampleData._
    import BlockSampleData.Block._
    import BlockSampleData.Tx._


    import ch.qos.logback.classic.Logger
    val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
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
    val TEST_LOOP_COUNT = 5000
    var testLoop = TEST_LOOP_COUNT

    println("Performance test started.")
    val startTimestamp = System.currentTimeMillis()
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
        wallet.unregisterTransaction(tx)
      }
      testLoop -= 1
    }
    val elapsedSecond = (System.currentTimeMillis()-startTimestamp) / 1000d
    println(s"Elasped second : ${elapsedSecond}")
    val totalTransactions = (TEST_LOOP_COUNT * transactions.length)
    println(s"Total transactions : ${totalTransactions}")
    println(s"Transactions per second : ${totalTransactions / elapsedSecond} /s ")
  }

  def prepareTestTransactions(txCount : Long) : ListBuffer[(Hash, Transaction)] = {
    val generationAddress = wallet.newAddress("generation")
    val generationTx = generationTransaction( "GenTx.BLK01", CoinAmount(50), generationAddress )

    // Prepare test data.
    val transactions = new ListBuffer[(Hash, Transaction)]()

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
      val splitTx = normalTransaction(
        s"splitTx-${testLoopCount}",
        spendingOutputs = List( mergedCoin ),
        newOutputs = List(
          NewOutput(CoinAmount(coin1Amount), newAddress1),
          NewOutput(CoinAmount(coin2Amount), newAddress2),
          NewOutput(CoinAmount(50-coin1Amount-coin2Amount), newAddress3)
        )
      )

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
    }

    transactions
  }

  "single thread perf test" should "measure performance by adding transactions to the pool" in {
    import BlockSampleData._
    import BlockSampleData.Block._
    import BlockSampleData.Tx._


    import ch.qos.logback.classic.Logger
    val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
    root.setLevel(ch.qos.logback.classic.Level.WARN);

    wallet.importOutputOwnership(chain, "test account", Addr1.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr2.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr3.address, rescanBlockchain = false)

    //    val TEST_LOOP_COUNT = 100000

    println("Preparing Performance test data.")

    val TEST_LOOP_COUNT = 2
//    val TEST_LOOP_COUNT = 30000
    var testLoop = TEST_LOOP_COUNT

    val transactions = prepareTestTransactions(TEST_LOOP_COUNT)

    {
      println("Performance test started. Add Transaction.")
      val startTimestamp = System.currentTimeMillis()

      transactions foreach { case (hash, tx) =>
        chain.txPool.addTransactionToPool(hash, tx)
      }

      val elapsedSecond = (System.currentTimeMillis()-startTimestamp) / 1000d
      println(s"Elapsed second : ${elapsedSecond}")
      val totalTransactions = TEST_LOOP_COUNT
      println(s"Total transactions : ${totalTransactions}")
      println(s"Transactions per second : ${totalTransactions / elapsedSecond} /s ")
    }
/*
    val signedTransactions =
      {
        println("Performance test started. Sign Transaction.")
        val startTimestamp = System.currentTimeMillis()

        var index = -1
        val signedTransactions =
          // Drop the generation transaction, unable to sign the generation transaction
          transactions.drop(1).map { case (hash, tx) =>
            index += 1
            println(s"singing ${index}")
            Wallet.get.signTransaction(
              tx,
              Blockchain.get,
              List(),
              None,
              SigHash.ALL)
          }

        val elapsedSecond = (System.currentTimeMillis()-startTimestamp) / 1000d
        println(s"Elapsed second : ${elapsedSecond}")
        val totalTransactions = TEST_LOOP_COUNT
        println(s"Total transactions : ${totalTransactions}")
        println(s"Transactions per second : ${totalTransactions / elapsedSecond} /s ")

        signedTransactions
      }


    {
      println("Performance test started. Verify Transaction.")
      val startTimestamp = System.currentTimeMillis()

      signedTransactions foreach { signedTx =>

        assert(signedTx.complete)
        new TransactionVerifier(signedTx.transaction).verify(chain)
      }

      val elapsedSecond = (System.currentTimeMillis()-startTimestamp) / 1000d
      println(s"Elapsed second : ${elapsedSecond}")
      val totalTransactions = TEST_LOOP_COUNT
      println(s"Total transactions : ${totalTransactions}")
      println(s"Transactions per second : ${totalTransactions / elapsedSecond} /s ")
    }

*/
  }


  "multi thread perf test" should "measure performance by adding transactions to the pool" ignore {
    import BlockSampleData._
    import BlockSampleData.Block._
    import BlockSampleData.Tx._


    import ch.qos.logback.classic.Logger
    val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
    root.setLevel(ch.qos.logback.classic.Level.WARN);

    wallet.importOutputOwnership(chain, "test account", Addr1.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr2.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr3.address, rescanBlockchain = false)

    //    val TEST_LOOP_COUNT = 100000

    println("Preparing Performance test data.")

    val TEST_LOOP_COUNT = 30000
    var testLoop = TEST_LOOP_COUNT

    val generationTxs = List(GEN01, GEN02, GEN03a, GEN04a, GEN05a)
    var transactionsMap = scala.collection.mutable.Map[Int, ListBuffer[(Hash, Transaction)]]()

    /*
    val prepareThreads = generationTxs.map{ genTx =>
      chain.txPool.addTransactionToPool(genTx.transaction.hash, genTx.transaction)
      new Thread() {
        override def run(): Unit = {
          val transactions = prepareTestTransactions(TEST_LOOP_COUNT)
          transactionsMap(genTx) = transactions
        }
      }
    }

    prepareThreads foreach { _.start } // Start all preparation threads
    prepareThreads foreach { _.join }  // join all preparation threads
*/

    val threadCount = 4
    0 until threadCount foreach { i =>
      val transactions = prepareTestTransactions(TEST_LOOP_COUNT)
      transactionsMap(i) = transactions
    }


    val threads =
      (0 until threadCount).map { i =>
        new Thread() {
          override def run(): Unit = {
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


    println("Performance test started.")
    val startTimestamp = System.currentTimeMillis()

    threads foreach { _.start } // Start all threads
    threads foreach { _.join }  //

    val elapsedSecond = (System.currentTimeMillis()-startTimestamp) / 1000d
    println(s"Elapsed second : ${elapsedSecond}")
    val totalTransactions = TEST_LOOP_COUNT * threadCount
    println(s"Total transactions : ${totalTransactions}")
    println(s"Transactions per second : ${totalTransactions / elapsedSecond} /s ")
  }


}