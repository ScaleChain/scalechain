package io.scalechain.blockchain.net

import java.util.concurrent.TimeUnit

import com.google.common.cache.CacheBuilder
import com.google.common.cache.Cache
import io.scalechain.blockchain.proto.BlockHeader
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.Transaction


/**
  * Keeps unsigned blocks for a specific time.
  */
class TimeBasedCache<T <: Object>(duration: Long, unit: TimeUnit) {
  val processors = Runtime.getRuntime().availableProcessors()
  val cache : Cache<Hash, T> =
    CacheBuilder.newBuilder().expireAfterWrite(duration, unit).concurrencyLevel(processors).build<Hash, T>

  fun put(hashKey : Hash, block : T) : Unit  {
    // Assumption : cache accepts concurrent put/get
    val incompleteBlock = cache.getIfPresent(hashKey)

    cache.put(hashKey, block)
  }

  fun get(hashKey : Hash) : Option<T> {
    // Assumption : cache accepts concurrent put/get

    val block = cache.getIfPresent(hashKey)
    if (block == null) None else Some(block)
  }
}
