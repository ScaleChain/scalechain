package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.TransactionVerificationException
import io.scalechain.blockchain.proto.{Transaction, Block}
import io.scalechain.blockchain.storage.BlockIndex
import org.slf4j.LoggerFactory

import scala.collection._

object BlockVerifier {
  case class TransactionTracker(transaction : Transaction, count : Int)

  val txFailureMapByMessage = mutable.HashMap[String, TransactionTracker]()
  def putTransactionVerificationFailure(message : String, transaction : Transaction): Unit = {
    val tracker = txFailureMapByMessage.get(message)
    val count = if (tracker.isDefined) tracker.get.count else 0

    // Keep the simplest transaction on the map.
    val simplestTransaction = if (tracker.isDefined) {
      if ( transaction.inputs.size + transaction.outputs.size <
           tracker.get.transaction.inputs.size + tracker.get.transaction.outputs.size )
        transaction
      else
        tracker.get.transaction
    } else {
      transaction
    }

    txFailureMapByMessage(message) = TransactionTracker(simplestTransaction, count + 1)
  }

  def getFailures() : String = {
    val builder = new StringBuilder()
    for ( (message, tracker) <- txFailureMapByMessage) {
      builder.append(s"[$message] count = ${tracker.count}\n")
      builder.append(s"${tracker.transaction}\n")
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
class BlockVerifier(block : Block) {
  private val logger = LoggerFactory.getLogger(classOf[BlockVerifier])

  def verify(blockIndex : BlockIndex) : Unit = {
    // (1) verify the hash of the block is within the difficulty level
    // TODO : Implement

    // (2) verify each transaction in the block
    block.transactions.map { transaction =>
      try {
        new TransactionVerifier(transaction).verify(blockIndex)
      } catch {
        case e: TransactionVerificationException => {
          //logger.warn(s"Transaction verification failed. transaction : ${transaction}, error : ${e.message}" )
          BlockVerifier.putTransactionVerificationFailure(e.message, transaction)
        }
      }
    }

    val blockCount = BlockVerifier.increaseBlockCount

    // For every 2000 blocks, print statistics.
    if (blockCount % 2000 == 0) {
      logger.info(s"[${blockCount}] block verifier statistics : ${BlockVerifier.statistics()}" )
    }

    // throw new BlockVerificationException
  }
}
