package io.scalechain.test

import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

// Create one when we write a performance testing case using Kotlin.
interface PerformanceTestTrait {

  fun sha256(value : ByteArray) : ByteArray {
    return MessageDigest.getInstance("SHA-1").digest(value)
  }

  fun rand32bytes() = sha256(BigInteger.valueOf(Random().nextLong()).toByteArray())

  fun<T> measure(loopCount: Int, subject: String, block: (() -> T)) : T {
    println("Performance test started. ${subject}.")
    val startTimestamp = System.currentTimeMillis()

    val returnValue = block()

    val elapsedMilliSecond = (System.currentTimeMillis() - startTimestamp)
    println("Elapsed millisecond : ${elapsedMilliSecond}")
    println("Total loops : ${loopCount}")
    println("Loops per second : ${loopCount.toDouble() / elapsedMilliSecond * 1000} /s ")

    return returnValue
  }

  fun measureWithSize(loopCount: Int, subject: String, block: (() -> Int)) : Unit {
    println("Performance test started. ${subject}.")
    val startTimestamp = System.currentTimeMillis()

    val totalSizeProcessed = block()

    val elapsedMilliSecond = (System.currentTimeMillis() - startTimestamp)
    println("Elapsed millisecond : ${elapsedMilliSecond}")
    println("Total loops : ${loopCount}")
    println("Loops per second : ${loopCount.toDouble() / elapsedMilliSecond * 1000} /s ")
    println("Size per second : ${totalSizeProcessed.toDouble() / elapsedMilliSecond * 1000} /s ")
  }

  fun prepareKeyValue(count: Int): List<Pair<ByteArray, ByteArray>> {
    val buffer = mutableListOf<Pair<ByteArray, ByteArray>>()
    for (i in 0 .. count) {
      // 32 byte key
      val key = rand32bytes()
      // 256 byte value
      val value =
        rand32bytes() +
          rand32bytes() +
          rand32bytes() +
          rand32bytes() +
          rand32bytes() +
          rand32bytes() +
          rand32bytes() +
          rand32bytes()
      buffer.add(Pair(key, value))
    }
    return buffer
  }
}
