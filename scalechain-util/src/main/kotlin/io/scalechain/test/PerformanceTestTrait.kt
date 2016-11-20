package io.scalechain.test

import java.math.BigInteger
import java.security.MessageDigest

import org.scalatest.*

import scala.collection.mutable.ListBuffer
import scala.util.Random

// Create one when we write a performance testing case using Kotlin.
/*
trait PerformanceTestTrait : FlatSpec with Matchers {

  fun sha256(value : Array<Byte>) : Array<Byte> {
    MessageDigest.getInstance("SHA-1").digest(value).array
  }

  fun rand32bytes = sha256(BigInteger.valueOf(Random.nextLong).toByteArray)

  fun measure<T>(subject: String)(block: => T)(implicit loopCount: Int) : T {
    println(s"Performance test started. ${subject}.")
    val startTimestamp = System.currentTimeMillis()

    val returnValue = block

    val elapsedSecond = (System.currentTimeMillis() - startTimestamp) / 1000d
    println(s"Elapsed second : ${elapsedSecond}")
    println(s"Total loops : ${loopCount}")
    println(s"Loops per second : ${loopCount / elapsedSecond} /s ")

    returnValue
  }

  fun measureWithSize(subject: String)(block: => Int)(implicit loopCount: Int) : Unit {
    println(s"Performance test started. ${subject}.")
    val startTimestamp = System.currentTimeMillis()

    val totalSizeProcessed = block

    val elapsedSecond = (System.currentTimeMillis() - startTimestamp) / 1000d
    println(s"Elapsed second : ${elapsedSecond}")
    println(s"Total loops : ${loopCount}")
    println(s"Loops per second : ${loopCount / elapsedSecond} /s ")
    println(s"Size per second : ${totalSizeProcessed / elapsedSecond} /s ")
  }

  fun prepareKeyValue(count: Long): List<(Array<Byte>, Array<Byte>)> {
    val buffer = ListBuffer<(Array<Byte>, Array<Byte>)>()
    for (i <- 0L to count) {
      // 32 byte key
      val key = rand32bytes
      // 256 byte value
      val value =
        rand32bytes ++
          rand32bytes ++
          rand32bytes ++
          rand32bytes ++
          rand32bytes ++
          rand32bytes ++
          rand32bytes ++
          rand32bytes
      buffer.append((key, value))
    }
    buffer.toList
  }
}
*/