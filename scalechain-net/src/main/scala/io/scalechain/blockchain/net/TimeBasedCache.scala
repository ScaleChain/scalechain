package io.scalechain.blockchain.net

import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, Cache}
import io.scalechain.blockchain.proto.{BlockHeader, Hash, Block, Transaction}

import scala.collection.mutable


/**
  * Keeps unsigned blocks for a specific time.
  */
class TimeBasedCache[T <: Object](duration: Long, unit: TimeUnit) {
  val cache : Cache[Hash, T] =
    CacheBuilder.newBuilder().expireAfterWrite(duration, unit).build[Hash, T]

  def put(hashKey : Hash, block : T) : Unit  = {
    synchronized {
      val incompleteBlock = cache.getIfPresent(hashKey)

      cache.put(hashKey, block)
    }
  }

  def get(hashKey : Hash) : Option[T] = {
    synchronized {
      val block = cache.getIfPresent(hashKey)
      if (block == null) None else Some(block)
    }
  }
}
