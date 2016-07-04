package io.scalechain.wallet

import java.io.File


import io.scalechain.blockchain.chain.BlockSampleData.Block._
import io.scalechain.blockchain.chain.BlockSampleData.Tx._
import io.scalechain.blockchain.chain.BlockSampleData._
import io.scalechain.blockchain.chain.{BlockSampleData, Blockchain}
import io.scalechain.blockchain.proto.{Hash, Transaction}
import io.scalechain.blockchain.storage.{DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction.{CoinAmount, ChainBlock, TransactionTestDataTrait}
import org.apache.commons.io.FileUtils
import org.apache.log4j.spi.LoggerFactory
import org.scalatest.{Suite, Matchers, BeforeAndAfterEach, FlatSpec}
import io.scalechain.blockchain.script.HashSupported._

import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * Created by kangmo on 7/4/16.
  */
@Ignore
class WalletPerformanceSpec extends FlatSpec with BeforeAndAfterEach with TransactionTestDataTrait with Matchers {

  this: Suite =>

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var wallet: Wallet = null
  var storage: DiskBlockStorage = null
  var chain: Blockchain = null

  val testPathForWallet = new File("./target/unittests-WalletSpec-wallet/")
  val testPathForStorage = new File("./target/unittests-WalletSpec-storage/")

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

  "perftest" should "measure performance on putting transactions only" in {
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

    var mergedCoin = getOutput(GEN01,0)

    chain.txPool.addTransactionToPool(GEN01.transaction.hash, GEN01.transaction)

    // Prepare test data.
    val transactions = new ListBuffer[(Hash, Transaction)]()
    while(testLoop > 0) {
      val accountName = s"${Random.nextInt}"
      val newAddress1 = BlockSampleData.generateAddress(accountName).address
      wallet.importOutputOwnership(chain, accountName, newAddress1, rescanBlockchain = false)
      val newAddress2 = BlockSampleData.generateAddress(accountName).address
      wallet.importOutputOwnership(chain, accountName, newAddress2, rescanBlockchain = false)
      val newAddress3 = BlockSampleData.generateAddress(accountName).address
      wallet.importOutputOwnership(chain, accountName, newAddress3, rescanBlockchain = false)

      val coin1Amount = Random.nextInt(10)
      val coin2Amount = Random.nextInt(18)
      val splitTx = normalTransaction(
        s"splitTx-${testLoop}",
        spendingOutputs = List( mergedCoin ),
        newOutputs = List(
          NewOutput(CoinAmount(coin1Amount), newAddress1),
          NewOutput(CoinAmount(coin2Amount), newAddress2),
          NewOutput(CoinAmount(50-coin1Amount-coin2Amount), newAddress3)
        )
      )

      transactions.append((splitTx.transaction.hash, splitTx.transaction))

      val mergeTx = normalTransaction(
        s"mergeTx-${testLoop}",
        spendingOutputs = List( getOutput(splitTx,0), getOutput(splitTx,1), getOutput(splitTx,2) ),
        newOutputs = List(
          NewOutput(CoinAmount(50), newAddress3)
        )
      )

      transactions.append((mergeTx.transaction.hash, mergeTx.transaction))

      mergedCoin = getOutput(mergeTx, 0)

      testLoop -= 1
    }


    println("Performance test started.")
    val startTimestamp = System.currentTimeMillis()

    transactions foreach { case (hash, tx) =>
      chain.txPool.addTransactionToPool(hash, tx)
    }

    val elapsedSecond = (System.currentTimeMillis()-startTimestamp) / 1000d
    println(s"Elasped second : ${elapsedSecond}")
    val totalTransactions = TEST_LOOP_COUNT
    println(s"Total transactions : ${totalTransactions}")
    println(s"Transactions per second : ${totalTransactions / elapsedSecond} /s ")
  }

}