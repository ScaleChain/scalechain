package io.scalechain.crypto

import java.math.BigInteger
import java.security.SecureRandom
import java.util

import io.scalechain.util.Utils
import org.scalatest._

/**
  * Source code copied from : https://github.com/ACINQ/bitcoin-lib/blob/master/src/test/scala/fr/acinq/bitcoin/Base58Spec.scala
  * License : Apache v2.
  */
class Base58UtilSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  import Base58Util._

  "Base58" should "encode byte arrays" in {
    assert(encode("Hello World".getBytes("UTF-8")) === "JxF12TrwUP45BMd")
    assert(encode(BigInteger.valueOf(3471844090L).toByteArray) === "16Ho7Hs")
    assert(encode(new Array[Byte](1)) === "1")
    assert(encode(new Array[Byte](7)) === "1111111")
    assert(encode(new Array[Byte](0)) === "")
  }

  it should "decode strings" in {
    assert(util.Arrays.equals(decode("JxF12TrwUP45BMd"), "Hello World".getBytes("UTF-8")))
    assert(util.Arrays.equals(decode(""), new Array[Byte](0)))
    assert(util.Arrays.equals(decode("1"), new Array[Byte](1)))
    assert(util.Arrays.equals(decode("1111111"), new Array[Byte](7)))
    decode("93VYUMzRG9DdbRP72uQXjaWibbQwygnvaCu9DumcqDjGybD864T")
    intercept[NoSuchElementException] {
      decode("This isn't valid base58")
    }
  }

  "it" should "decode encoded string" in {
    for (i <- 1 to 1000) { // Because we are generating random numbers, test many times not to let the test case pass with some small randome number.
    val random = new SecureRandom()
      random.setSeed( random.generateSeed(32) )

      val originalValue : Array[Byte] = new Array[Byte](32)
      assert(originalValue.length == 32)
      random.nextBytes(originalValue)

      val encodedValue = encode(originalValue)

      val decodedValue = decode(encodedValue)

      val encodedValue2 = encode(decodedValue)

      originalValue.toList shouldBe decodedValue.toList
      encodedValue.toList shouldBe encodedValue2.toList
    }
  }

  "Base58Check" should "decode encoded string" in {
    val VERSION : Byte = 1
    for (i <- 1 to 1000) { // Because we are generating random numbers, test many times not to let the test case pass with some small randome number.
      val random = new SecureRandom()
      random.setSeed( random.generateSeed(32) )

      val originalValue : Array[Byte] = new Array[Byte](32)
      assert(originalValue.length == 32)
      random.nextBytes(originalValue)

      val encodedValue = Base58Check.encode(VERSION, originalValue)

      val (decodedVersion, decodedValue) = Base58Check.decode(encodedValue)

      val encodedValue2 = Base58Check.encode(VERSION, decodedValue)

      decodedValue.toList shouldBe originalValue.toList
      decodedVersion shouldBe decodedVersion
      encodedValue2.toList shouldBe encodedValue.toList
    }

  }
}
