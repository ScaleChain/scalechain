package io.scalechain.crypto

import java.math.BigInteger
import java.security.SecureRandom
import java.util

import io.scalechain.util.Base58Util
import org.scalatest._

/**
  * Source code copied from : https://github.com/ACINQ/bitcoin-lib/blob/master/src/test/scala/fr/acinq/bitcoin/Base58Spec.scala
  * License : Apache v2.
  */
class Base58CheckSpec : FlatSpec with BeforeAndAfterEach with Matchers {
  this: Suite =>

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  import Base58Util._

  "Base58Check" should "decode encoded string" in {
    val VERSION : Byte = 1
    for (i <- 1 to 1000) { // Because we are generating random numbers, test many times not to let the test case pass with some small randome number.
    val random = SecureRandom()
      random.setSeed( random.generateSeed(32) )

      val originalValue : ByteArray = ByteArray(32)
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
