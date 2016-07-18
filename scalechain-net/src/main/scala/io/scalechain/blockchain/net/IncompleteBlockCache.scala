package io.scalechain.blockchain.net

import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, Cache}
import io.scalechain.blockchain.proto.{Hash, Block, Transaction}

import scala.collection.mutable


/**
  * An incomplete block either nodes did not meet consensus, nor the block was not received yet.
  * The incomplete block is a block that is either not approved by BFT or it is approved, but the block itself was not received.
  * @param block
  * @param consensus true if nodes agreed on consensus.
  */
case class IncompleteBlock(block : Option[Block], consensus : Boolean) {
  def metConsensus() : Boolean = {
    block.isDefined && consensus
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
          IncompleteBlock(Some(block), false)
        } else {
          incompleteBlock.copy( block = Some(block) )
        }
      cache.put(blockHash, newIncompleteBlock)
      newIncompleteBlock
    }
  }

  def addConsensus(blockHash : Hash) : IncompleteBlock = {
    synchronized {
      val incompleteBlock = cache.getIfPresent(blockHash)
      val newIncompleteBlock =
        if (incompleteBlock == null) {
          // The block was not received yet.
          IncompleteBlock(None, true)
        } else {
          incompleteBlock.copy( consensus = true )
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
