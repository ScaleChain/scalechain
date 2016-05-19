package io.scalechain.util

import java.math.BigInteger
import java.security.SecureRandom

import org.scalatest._

class UtilsSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
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

  "bigIntegerToBytes" should "roundtrip" in {
    for (i <- 1 to 1000) { // Because we are generating random numbers, test many times not to let the test case pass with some small randome number.
      val random = new SecureRandom()
      random.setSeed( random.generateSeed(32) )

      val originalKeyArray : Array[Byte] = new Array[Byte](32)
      assert(originalKeyArray.length == 32)
      random.nextBytes(originalKeyArray)

      val originalBigInteger = Utils.bytesToBigInteger(originalKeyArray)

      val encodedKeyArray = Utils.bigIntegerToBytes(originalBigInteger, 32)

      val createdBigInteger = Utils.bytesToBigInteger(encodedKeyArray)

      encodedKeyArray.toList shouldBe originalKeyArray.toList
      createdBigInteger shouldBe originalBigInteger
    }
  }
}