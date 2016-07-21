package io.scalechain.util

import java.util.concurrent.TimeUnit

import com.google.common.cache._

/**
  * Keeps unsigned blocks for a specific time.
  */
class TimeBasedCache[K <: Object, V <: Object](duration: Long, unit: TimeUnit) {
  val cache : Cache[K, V] =
    CacheBuilder.newBuilder().expireAfterWrite(duration, unit).build[K, V]

  def put(hashKey : K, block : V) : Unit  = {
    // Assumption : Cache is thread-safe
    val incompleteBlock = cache.getIfPresent(hashKey)

    cache.put(hashKey, block)
  }

  def get(hashKey : K) : Option[V] = {
    // Assumption : Cache is thread-safe
    val block = cache.getIfPresent(hashKey)
    if (block == null) None else Some(block)
  }
}
