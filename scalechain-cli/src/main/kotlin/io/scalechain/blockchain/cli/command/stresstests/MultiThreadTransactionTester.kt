package io.scalechain.blockchain.cli.command.stresstests

import java.io.File

import io.scalechain.blockchain.proto.Transaction

import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.util.HexUtil
import io.scalechain.util.ListExt

// (Thread group index, transaction) => Unit
interface TransactionWithGroupListener {
  fun onTransaction(txGroupIndex : Int, transaction : Transaction) : Unit
}
// (Thread group index, raw transaction string) => Unit
interface RawTransactionWithGroupListener {
  fun onRawTransaction(txGroupIndex : Int, rawTransaction : String) : Unit
}

// Thread group index => Whether to run the thread group
interface ThreadGroupIndexFilter {
  fun filterByGroupIndex(txGroupIndex : Int) : Boolean
}

/**
  * Run tests in parallel for each transaction group, written by generaterawtransaction RPC.
  */
class MultiThreadTransactionTester(private val threadGroupIndexFilter : ThreadGroupIndexFilter? = null) {
  /**
    * Read transaction group files, run test cases for each transaction in the group. Run the tests for each group in parallel.
    *
    * @param initialSplitTxTest
    * @param transactionTests
    */
  fun testTransaction(initialSplitTxTest : TransactionWithGroupListener, transactionTests : List<TransactionWithGroupListener>) : Unit {

    fun toTxWithGroupListener( txGroupListener : TransactionWithGroupListener ) : RawTransactionWithGroupListener {
      return object : RawTransactionWithGroupListener {
        override fun onRawTransaction(txGroupIndex : Int, rawTransaction : String) : Unit {
          val rawTransactionBytes = HexUtil.bytes(rawTransaction)
          val transaction = TransactionCodec.decode(rawTransactionBytes)!!
          return txGroupListener.onTransaction(txGroupIndex, transaction)
        }
      }
    }

    val txTests = transactionTests.map { testCase ->
      toTxWithGroupListener(testCase)
    }
    testRawTransaction( toTxWithGroupListener(initialSplitTxTest), txTests )
  }

  fun testRawTransaction(initialSplitTxTestWithGroup : RawTransactionWithGroupListener, transactionTests : List<RawTransactionWithGroupListener>) : Unit {
    // Step 2 : Load each transaction group file, run tests in parallel for each group.
    val threads =
      (0 until transactionTests.size).filter { i ->
        if (threadGroupIndexFilter != null) { // Filter threads first if any threadGroupIndexFilter is defined.
          threadGroupIndexFilter.filterByGroupIndex(i)
        } else {
          true
        }
      }.map { i ->
        object : Thread() {
          override fun run(): Unit {

            // Step 1 : Send the initial split transaction.
            val initialTxReader = TransactionReader( File( GenerateRawTransactions.initialSplitTransactionFileName() ) )

            val initialSplitTxTest = object : RawTransactionListener {
              override fun onRawTransaction(rawTransaction : String) : Unit {
                initialSplitTxTestWithGroup.onRawTransaction(i, rawTransaction)
              }
            }

            initialTxReader.read(initialSplitTxTest)

            // Step 2 : Send transactions in files.
            val txTest = transactionTests[i]
            val txReader = TransactionReader( File( GenerateRawTransactions.transactionGroupFileName(i) ) )

            var txCount = 0
            val countShowingTest = object : RawTransactionListener {
              override fun onRawTransaction(rawTransaction : String) : Unit {
                txTest.onRawTransaction(i, rawTransaction)
                txCount += 1
                if ( ((txCount shr 7) shl 7) == txCount) { // txCount % 128 = 0
                  println("Processed ${txCount} transactions")
                }
              }
            }

            txReader.read(countShowingTest)
          }
        }
      }

    threads.forEach { it.start() } // Start all threads
    threads.forEach { it.join() }  // Join all threads
  }

  /**
    * Run the same test for all transaction groups and the initial split transaction.
    *
    * @param txGroupCount The number of transaction groups
    * @param transactionTest The test to run for each test.
    */
  fun testTransaction(txGroupCount : Int, transactionTest : TransactionWithGroupListener) : Unit {
    testTransaction(transactionTest, ListExt.fill(txGroupCount, transactionTest))
  }

  fun testRawTransaction(txGroupCount : Int, transactionTest : RawTransactionWithGroupListener) : Unit {
    testRawTransaction(transactionTest, ListExt.fill(txGroupCount, transactionTest))
  }
}
