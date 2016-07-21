package io.scalechain.blockchain.transaction

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.TransactionVerificationException
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.BlockIndex
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import org.slf4j.LoggerFactory

import scala.collection._

object BlockVerifier {
  case class TransactionTracker(block : Block, transaction : Transaction, mergedScriptOption : Option[MergedScript], count : Int)

  val txFailureMapByMessage = mutable.HashMap[String, TransactionTracker]()
  def putTransactionVerificationFailure(message : String, block : Block, transaction : Transaction, mergedScriptOption : Option[MergedScript]): Unit = {
    val tracker = txFailureMapByMessage.get(message)
    val count = if (tracker.isDefined) tracker.get.count else 0

    // Keep the simplest transaction on the map.
    val (simplestBlock, simplestTransaction, simplestMergedScriptOption) = if (tracker.isDefined) {
      if ( transaction.inputs.size + transaction.outputs.size <
           tracker.get.transaction.inputs.size + tracker.get.transaction.outputs.size )
        (block, transaction, mergedScriptOption)
      else
        (tracker.get.block, tracker.get.transaction, tracker.get.mergedScriptOption)
    } else {
      (block, transaction, mergedScriptOption)
    }

    txFailureMapByMessage(message) = TransactionTracker(simplestBlock, simplestTransaction, simplestMergedScriptOption, count + 1)
  }

  def getFailures() : String = {
    val builder = new StringBuilder()
    for ( (message, tracker) <- txFailureMapByMessage) {
      builder.append(s"[$message] count = ${tracker.count}\n")
      builder.append("------------------------------------\n")
      builder.append(s"blockHash=${tracker.block.header.hash}\n")
/*
      builder.append("------------------------------------\n")
      builder.append(s"block=${tracker.block}\n")
*/
      builder.append("------------------------------------\n")
      builder.append(s"transaction=${tracker.transaction}\n")
      builder.append("------------------------------------\n")
      if ( tracker.mergedScriptOption.isDefined) {
        builder.append(s"${tracker.mergedScriptOption.get}\n")
      }
      builder.append("------------------------------------\n")


      builder.append("\n")
    }
    builder.toString
  }

  def statistics() : String = {
    val builder = new StringBuilder()
    builder.append(s"Transaction verification statistics : ${NormalTransactionVerifier.stats()}" )
    builder.append(s"List of transaction failures : \n ${BlockVerifier.getFailures}")
    builder.toString
  }

  var blockCount = -1
  def increaseBlockCount() : Int = {
    blockCount += 1
    blockCount
  }
}
/**
  * Created by kangmo on 3/15/16.
  */
class BlockVerifier(block : Block)(implicit db : KeyValueDatabase) {
  private val logger = Logger( LoggerFactory.getLogger(classOf[BlockVerifier]) )

  def verify(chainView : BlockchainView) : Unit = {
    // (1) verify the hash of the block is within the difficulty level
    // TODO : Implement

    // (2) verify each transaction in the block
    block.transactions.map { transaction =>
      try {
        new TransactionVerifier(transaction).verify(chainView)
      } catch {
        case e: TransactionVerificationException => {
          // Because the exception is defined in the util layer, we could not use the MergedScript type, but AnyRef.
          // We need to convert the AnyRef back to MergedScript.
          val mergedScriptOption = if (e.debuggingInfo.isDefined) {
            if (e.debuggingInfo.get.isInstanceOf[MergedScript]) {
              Some( e.debuggingInfo.get.asInstanceOf[MergedScript] )
            } else None
          } else None

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
    }

    val blockCount = BlockVerifier.increaseBlockCount

    // For every 2000 blocks, print statistics.
    if (blockCount % 2000 == 0) {
      logger.trace(s"[${blockCount}] block verifier statistics : ${BlockVerifier.statistics()}" )
    }

    // throw new BlockVerificationException
  }
}
