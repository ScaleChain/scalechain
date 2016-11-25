package io.scalechain.util

import java.util.concurrent.TimeUnit

import com.google.common.cache.*

/**
 * Keeps unsigned blocks for a specific time.
 */
class TimeBasedCache<K, V>(duration: Long, unit: TimeUnit) {
    val cache : Cache<K, V> =
    CacheBuilder.newBuilder().expireAfterWrite(duration, unit).build<K, V>()

    fun put(hashKey : K, block : V) {
        // Assumption : Cache is thread-safe
        //val incompleteBlock = cache.getIfPresent(hashKey)

        cache.put(hashKey, block)
    }

    fun get(hashKey : K) : V? {
        // Assumption : Cache is thread-safe
        val block = cache.getIfPresent(hashKey)
        return block
    }
}
