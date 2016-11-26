package io.scalechain.blockchain.transaction

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.TransactionVerificationException
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.BlockIndex
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 3/15/16.
  */
class BlockVerifier(private val db : KeyValueDatabase, private val block : Block) {
  private val logger = LoggerFactory.getLogger(BlockVerifier.javaClass)

  fun verify(chainView : BlockchainView) : Unit {
    // (1) verify the hash of the block is within the difficulty level
    // TODO : Implement

    // (2) verify each transaction in the block
    block.transactions.map { transaction ->
      try {
        TransactionVerifier(db, transaction).verify(chainView)
      } catch(e: TransactionVerificationException) {
        // Because the exception is defined in the util layer, we could not use the MergedScript type, but AnyRef.
        // We need to convert the AnyRef back to MergedScript.
        val mergedScriptOption =
          if (e.debuggingInfo != null) {
            if (e.debuggingInfo is MergedScript) {
              e.debuggingInfo as MergedScript
            } else null
          } else null

        //logger.warn(s"Transaction verification failed. transaction : ${transaction}, error : ${e.message}" )
        BlockVerifier.putTransactionVerificationFailure(e.message, block, transaction, mergedScriptOption)
/*
        if (block.header.hashPrevBlock == Hash("000000006f6709b76bed31001b32309167757007aa4fb899f8168c8e9c084b1a")) {
          println(s"after block:\n$block\n")
          //      System.exit(-1)
        }
*/
      }
    }

    val blockCount = BlockVerifier.increaseBlockCount()

    // For every 2000 blocks, print statistics.
    if (blockCount % 2000 == 0) {
      logger.trace("<${blockCount}> block verifier statistics : ${BlockVerifier.statistics()}" )
    }

    // throw BlockVerificationException
  }

  companion object {
    data class TransactionTracker(val block : Block, val transaction : Transaction, val mergedScriptOption : MergedScript?, val count : Int)

    val txFailureMapByMessage = mutableMapOf<String, TransactionTracker>()
    fun putTransactionVerificationFailure(message : String, block : Block, transaction : Transaction, mergedScriptOption : MergedScript?): Unit {
      val tracker = txFailureMapByMessage.get(message)
      val count = tracker?.count ?: 0

      // Keep the simplest transaction on the map.
      val (simplestBlock, simplestTransaction, simplestMergedScriptOption) =
          if (tracker != null) {
            if (transaction.inputs.size + transaction.outputs.size < tracker.transaction.inputs.size + tracker.transaction.outputs.size) {
              Triple(block, transaction, mergedScriptOption)
            } else {
              Triple(tracker.block, tracker.transaction, tracker.mergedScriptOption)
            }
          } else {
            Triple(block, transaction, mergedScriptOption)
          }

      val newTracker = TransactionTracker(simplestBlock, simplestTransaction, simplestMergedScriptOption, count + 1)
      txFailureMapByMessage.put(message, newTracker)
    }

    fun getFailures() : String {
      val builder = StringBuilder()
      for ( (message, tracker) in txFailureMapByMessage) {
        builder.append("<$message> count = ${tracker.count}\n")
        builder.append("------------------------------------\n")
        builder.append("blockHash=${tracker.block.header.hash()}\n")
/*
      builder.append("------------------------------------\n")
      builder.append(s"block=${tracker.block}\n")
*/
        builder.append("------------------------------------\n")
        builder.append("transaction=${tracker.transaction}\n")
        builder.append("------------------------------------\n")
        if ( tracker.mergedScriptOption != null) {
          builder.append("${tracker.mergedScriptOption}\n")
        }
        builder.append("------------------------------------\n")

        builder.append("\n")
      }
      return builder.toString()
    }

    fun statistics() : String {
      val builder = StringBuilder()
      builder.append("Transaction verification statistics : ${NormalTransactionVerifier.stats()}" )
      builder.append("List of transaction failures : \n ${BlockVerifier.getFailures()}")
      return builder.toString()
    }

    var blockCount = -1
    fun increaseBlockCount() : Int {
      blockCount += 1
      return blockCount
    }
  }
}
