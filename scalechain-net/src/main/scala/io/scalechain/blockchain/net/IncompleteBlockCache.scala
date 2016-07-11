package io.scalechain.blockchain.net

import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, Cache}
import io.scalechain.blockchain.proto.{Hash, Block, Transaction}

import scala.collection.mutable


case class IncompleteBlock(block : Option[Block], signingTxs : Set[Transaction]) {
  def hasEnoughSigningTransactions(requiredSigningTransactions : Int) : Boolean = {
    block.isDefined && signingTxs.size >= requiredSigningTransactions
  }
}

object IncompleteBlockCache extends IncompleteBlockCache(10, TimeUnit.MINUTES)

/**
  * Keeps unsigned blocks for a specific time.
  */
class IncompleteBlockCache(duration: Long, unit: TimeUnit) {
  val cache : Cache[Hash, IncompleteBlock] =
    CacheBuilder.newBuilder().expireAfterWrite(duration, unit).
      build[Hash, IncompleteBlock]

  def addBlock(blockHash : Hash, block : Block) : IncompleteBlock  = {
    synchronized {
      val incompleteBlock = cache.getIfPresent(blockHash)

      val newIncompleteBlock =
        if (incompleteBlock == null) {
          // The block was not received yet.
          IncompleteBlock(Some(block), Set())
        } else {
          incompleteBlock.copy( block = Some(block) )
        }
      cache.put(blockHash, newIncompleteBlock)
      newIncompleteBlock
    }
  }

  def addSigningTransaction(blockHash : Hash, tx : Transaction) : IncompleteBlock = {
    synchronized {
      val incompleteBlock = cache.getIfPresent(blockHash)
      val newIncompleteBlock =
        if (incompleteBlock == null) {
          // The block was not received yet.
          IncompleteBlock(None, Set(tx))
        } else {
          incompleteBlock.copy( signingTxs = incompleteBlock.signingTxs ++ Set(tx) )
        }
      cache.put(blockHash, newIncompleteBlock)
      newIncompleteBlock
    }
  }

  def getBlock(blockHash : Hash) : Option[IncompleteBlock] = {
    synchronized {
      val block = cache.getIfPresent(blockHash)
      if (block == null) None else Some(block)
    }
  }
}
