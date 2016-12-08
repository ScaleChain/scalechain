package io.scalechain.test

import java.math.BigInteger
import java.security.MessageDigest

import scala.collection.mutable.ListBuffer
import scala.util.Random

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec

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

    val elapsedSecond = (System.currentTimeMillis() - startTimestamp) / 1000
    println("Elapsed second : ${elapsedSecond}")
    println("Total loops : ${loopCount}")
    println("Loops per second : ${loopCount / elapsedSecond} /s ")

    return returnValue
  }

  fun measureWithSize(loopCount: Int, subject: String, block: (() -> Int)) : Unit {
    println("Performance test started. ${subject}.")
    val startTimestamp = System.currentTimeMillis()

    val totalSizeProcessed = block()

    val elapsedSecond = (System.currentTimeMillis() - startTimestamp) / 1000
    println("Elapsed second : ${elapsedSecond}")
    println("Total loops : ${loopCount}")
    println("Loops per second : ${loopCount / elapsedSecond} /s ")
    println("Size per second : ${totalSizeProcessed / elapsedSecond} /s ")
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
