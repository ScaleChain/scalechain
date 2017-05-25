package io.scalechain.wallet

import io.kotlintest.matchers.Matchers
import java.io.File
import io.scalechain.blockchain.chain.BlockSampleData
import io.scalechain.blockchain.chain.mining.BlockMining
import io.scalechain.blockchain.chain.NewOutput
import io.scalechain.blockchain.chain.TransactionWithName
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto.codec.HashCodec
import io.scalechain.blockchain.storage.index.TransactionPoolIndex
import io.scalechain.blockchain.transaction.*
import io.scalechain.test.PerformanceTestTrait
import io.scalechain.util.StopWatch
import io.scalechain.blockchain.script.hash
import ch.qos.logback.classic.Logger
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.*
import io.scalechain.util.ByteBufExt
import org.junit.runner.RunWith
import java.util.*

/**
  * Created by kangmo on 7/4/16.
  */
@RunWith(KTestJUnitRunner::class)
class WalletPerformanceSpec : WalletTestTrait(), PerformanceTestTrait, TransactionTestInterface, Matchers {
  val TEST_LOOP_COUNT = 1000

  override val testPath = File("./build/unittests-WalletPerformanceSpec-storage/")

  override fun beforeEach() {

    super.beforeEach()
  }

  override fun afterEach() {

    super.afterEach()
  }

  fun prepareTestTransactions(txCount: Int, data: BlockSampleData = BlockSampleData(db), genTxOption: Transaction? = null): MutableList<Pair<Hash, Transaction>> {
    val generationAddress = wallet.newAddress(db, "generation")
    val generationTx =
      if (genTxOption != null) {
        TransactionWithName("generation transaction", genTxOption)
      } else {
        data.generationTransaction("GenTx.B.BLK01", CoinAmount(50), generationAddress)
      }

    // Prepare test data.
    val transactions = mutableListOf< Pair<Hash, Transaction>>()

    var mergedCoin = data.getOutput(generationTx, 0)

    var testLoopCount = txCount

    transactions.add(Pair(generationTx.transaction.hash(), generationTx.transaction))

    while (testLoopCount > 0) {
      val accountName = "${Random().nextInt()}"
      val newAddress1 = wallet.newAddress(db, accountName)
      val newAddress2 = wallet.newAddress(db, accountName)
      val newAddress3 = wallet.newAddress(db, accountName)

      val coin1Amount = Random().nextInt(10).toLong()
      val coin2Amount = Random().nextInt(18).toLong()
      val splitTxOrg = data.normalTransaction(
        "splitTx-${testLoopCount}",
        spendingOutputs = listOf(mergedCoin),
        newOutputs = listOf(
          NewOutput(CoinAmount(coin1Amount), newAddress1),
          NewOutput(CoinAmount(coin2Amount), newAddress2),
          NewOutput(CoinAmount(50 - coin1Amount - coin2Amount), newAddress3)
        )
      )
      //      val splitTx = splitTxOrg.copy(transaction = splitTxOrg.transaction.copy(version=coin1Amount))

      val splitTx = splitTxOrg

      transactions.add(Pair(splitTx.transaction.hash(), splitTx.transaction))

      val mergeTx = data.normalTransaction(
        "mergeTx-${testLoopCount}",
        spendingOutputs = listOf(data.getOutput(splitTx, 0), data.getOutput(splitTx, 1), data.getOutput(splitTx, 2)),
        newOutputs = listOf(
          NewOutput(CoinAmount(50), newAddress3)
        )
      )

      transactions.add(Pair(mergeTx.transaction.hash(), mergeTx.transaction))

      mergedCoin = data.getOutput(mergeTx, 0)

      testLoopCount -= 2

      if (testLoopCount == (testLoopCount shr 10 shl 10)) {
        println("${testLoopCount} transactions remaining")
      }
    }

    return transactions
  }

  init {

    "encoding/decoding key/value".config(ignored=true) should "measure performance" {
      val data = BlockSampleData(db)

      val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
      root.setLevel(ch.qos.logback.classic.Level.WARN);

      wallet.importOutputOwnership(db, chain, "test account", data.Addr1.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr2.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr3.address, rescanBlockchain = false)

      println("Preparing Performance test data.")

      val transactions = prepareTestTransactions(TEST_LOOP_COUNT)

      val hashes = mutableListOf<ByteArray>()
      measureWithSize(TEST_LOOP_COUNT, "encode hash") {
        var totalSize = 0

        transactions.forEach { pair ->
          val txHash = pair.first
          //val tx = pair.second

          val rawHash: ByteArray = HashCodec.encode(txHash)
          val prefixedRawHash = byteArrayOf('A'.toByte()) + rawHash
          hashes.add(prefixedRawHash)
          totalSize += rawHash.size
        }

        totalSize
      }

      var prefixSum = 0
      measureWithSize(TEST_LOOP_COUNT, "decode hash") {
        var totalSize = 0

        hashes.forEach { prefixedRawHash ->

          //val rawPrefix = prefixedRawHash.take(1)
          val rawHash = prefixedRawHash.drop(1).toByteArray()
          prefixSum += HashCodec.decode(rawHash)!!.value.array[0].toInt()
          totalSize += prefixedRawHash.size
        }

        totalSize
      }


      val rawTransactions = mutableListOf<ByteArray>()
      measureWithSize(TEST_LOOP_COUNT, "encode transaction") {
        var totalSize = 0

        transactions.forEach { pair ->
          //val txHash = pair.first
          val tx = pair.second

          //val rawTx: ByteArray = TransactionCodec.serialize(tx)
          val rawTx = TransactionCodec.encode(tx)
          rawTransactions.add(rawTx)
          totalSize += rawTx.size
        }
        totalSize
      }

      var sum: Long = 0
      measureWithSize(TEST_LOOP_COUNT, "decode transaction") {
        var totalSize = 0
        rawTransactions.forEach { rawTx ->
          sum += TransactionCodec.decode(rawTx)!!.outputs[0].value
          totalSize += rawTx.size
        }
        totalSize
      }

      println("Prefix sum ${prefixSum}, Version sum ${sum}")


    }

/*
    "single thread perf test" should "measure performance for mining blocks" {
      val data = BlockSampleData(db)

      val B = data.Block

      val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
      root.setLevel(ch.qos.logback.classic.Level.WARN);

      wallet.importOutputOwnership(db, chain, "test account", data.Addr1.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr2.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr3.address, rescanBlockchain = false)

      chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
      chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
      chain.putBlock(db, B.BLK03a.header.hash(), B.BLK03a)

      println("Preparing Performance test data.")

      val transactions = prepareTestTransactions(TEST_LOOP_COUNT, data, B.BLK02.transactions[0])


      measure(TEST_LOOP_COUNT, "single thread Add Transaction") {
        // Drop the first transaction, which is the generation transaction already included in the first block.
        transactions.drop(1).forEach { pair ->
          val txHash = pair.first
          val tx = pair.second

          chain.withTransaction { transactingDatabase ->

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
            chain.txPool.addTransactionToPool(transactingDatabase, txHash, tx)
          }
        }
      }

      val w = StopWatch()

      //val MaxBlockSize = 1024 * 1024 * 2
      val MaxBlockSize = 128 * 1024
      val minedBlockOption = measure<Block?>(1, "mine block", {
        w.start("COINBASE_MESSAGE")
        val COINBASE_MESSAGE = CoinbaseData("height:${chain.getBestBlockHeight()}, ScaleChain by Kwanho, Chanwoo, Kangmo.".toByteArray())
        w.stop("COINBASE_MESSAGE")
        // Step 2 : Create the block template
        w.start("getBestBlockHash")
        val bestBlockHash = chain.getBestBlockHash(db)
        w.stop("getBestBlockHash")
        if (bestBlockHash != null) {

          w.start("blockTemplate")
          val blockMining = BlockMining(chain.db, chain.txDescIndex(), chain.txPool, chain)
          val blockTemplate = blockMining.getBlockTemplate(COINBASE_MESSAGE, data.minerAddress(), MaxBlockSize)
          w.stop("blockTemplate")

          //if (blockTemplate != null) {
            w.start("getBlockHeader")
            // Step 3 : Get block header
            val blockHeader = blockTemplate.getBlockHeader(Hash(bestBlockHash.value))
            w.stop("getBlockHeader")


            // Step 3 : Loop until we find a block header hash less than the threshold.
            //            do {
            // TODO : BUGBUG : Need to use chain.getDifficulty instead of using a fixed difficulty

            // TODO : BUGBUG : Remove scalechain.mining.header_hash_threshold configuration after the temporary project finishes

            // Check the best block hash once more.
            w.start("hash compare")
            if (bestBlockHash.value == chain.getBestBlockHash(db)!!.value) {
              w.stop("hash compare")
              // Step 5 : When a block is found, create the block and put it on the blockchain.
              // Also propate the block to the peer to peer network.
              w.start("create block")
              val block = blockTemplate.createBlock(blockHeader, blockHeader.nonce)
              w.stop("create block")

              w.start("calc block hash")
              block.header.hash()
              w.stop("calc block hash")
              block
            } else {
              null
            }
          //} else {
          //  println("Not enough signed transactions with the previous block hash.")
          //  null
          //}
        } else {
          println("The best block hash is not defined yet.")
          null
        }

      })

      println("result : ${w.toString()}")
      println("transactions in the block : ${minedBlockOption!!.transactions.size}")
    }
*/
      /*
    "single thread perf test".config(ignored=true) should "measure performance by adding transactions to the pool" {
      val data = BlockSampleData(db)

      val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
      root.setLevel(ch.qos.logback.classic.Level.WARN);

      wallet.importOutputOwnership(db, chain, "test account", data.Addr1.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr2.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr3.address, rescanBlockchain = false)

      println("Preparing Performance test data.")

      val transactions = prepareTestTransactions(TEST_LOOP_COUNT)

      measure(TEST_LOOP_COUNT, "single thread Add Transaction") {
        transactions.forEach { pair ->
          val txHash = pair.first
          val tx = pair.second

          chain.withTransaction { transactingDatabase ->
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
            chain.txPool.addTransactionToPool(transactingDatabase, txHash, tx)
          }
        }
      }
    }
*/
/*
    "multi thread perf test".config(ignored=true) should "measure performance by adding transactions to the pool"  {
      val data = BlockSampleData(db)

      val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
      root.setLevel(ch.qos.logback.classic.Level.WARN);

      wallet.importOutputOwnership(db, chain, "test account", data.Addr1.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr2.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr3.address, rescanBlockchain = false)

      //    val TEST_LOOP_COUNT = 100000

      println("Preparing Performance test data.")

      var transactionsMap = mutableMapOf < Int, MutableList<Pair<Hash, Transaction>>>()

      val threadCount = 4
      (0 until threadCount).forEach { i ->

        val transactions = prepareTestTransactions(TEST_LOOP_COUNT)
        transactionsMap[i] = transactions
      }


      val threads =
        (0 until threadCount).map { i ->

          object : Thread() {
            override fun run(): Unit {
              val transactions = transactionsMap[i]
              var addedCount = 0
              transactions!!.forEach { pair ->
                val hash = pair.first
                val tx = pair.second

                chain.txPool.addTransactionToPool(db, hash, tx)
                addedCount += 1
              }
              println("Added ${addedCount} transactions.")
            }
          }
        }

      val totalTransactions = TEST_LOOP_COUNT * threadCount

      measure(totalTransactions, "multithread add transaction") {
        threads.forEach { it.start() } // Start all threads
        threads.forEach { it.join() }  //
      }
    }
*/
/*
    "perftest".config(ignored=true) should "measure performance on register transaction"  {
      val data = BlockSampleData(db)

      val T = data.Tx

      val root: Logger = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
      root.setLevel(ch.qos.logback.classic.Level.WARN);


      val transactions = listOf(
        T.GEN01,
        T.GEN02, T.TX02,
        T.GEN03a, T.TX03, T.TX03a,
        T.GEN04a, T.TX04, T.TX04a,
        T.GEN05a, T.TX05a)

      val reverseTransactions = transactions.reversed()

      wallet.importOutputOwnership(db, chain, "test account", data.Addr1.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr2.address, rescanBlockchain = false)
      wallet.importOutputOwnership(db, chain, "test account", data.Addr3.address, rescanBlockchain = false)


      var testLoop = TEST_LOOP_COUNT

      println("Performance test started.")
      measure(TEST_LOOP_COUNT, "register/unregister transaction") {
        while (testLoop > 0) {
          // register all
          var txIndex: Int = -1
          transactions.map{ it.transaction }.forEach { tx : Transaction ->

            val txHash = tx.hash()
            txIndex += 1

            // registerTransaction is called in addTransactionToPool
            //wallet.registerTransaction(tx, Some(ChainBlock(10, B.BLK05a)), Some(txIndex))
            chain.txPool.addTransactionToPool(db, txHash, tx)
          }

          // unregister all
          reverseTransactions.map{ it.transaction }.forEach { tx : Transaction ->
            val txHash = tx.hash()
            chain.txPool.removeTransactionFromPool(db, txHash)
            wallet.unregisterTransaction(db, txHash, tx)
          }
          testLoop -= 1
        }
      }
    }
*/
  }
}