package io.scalechain.wallet

import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 4..
  */
class CoinAddressSpec extends FlatSpec with ShouldMatchers {
  this: Suite =>

  "isValid" should "return boolean value if the parameter exists" in {

    val coinAddress = CoinAddress("1AGNa15ZQXAZUgFiqJ2i7Z2DPU2J6hW62i", "unknown")
    coinAddress.isValid shouldBe a [java.lang.Boolean]
  }

  "isValid" should "return false if the address has invalid length" in {

    val coinAddress = CoinAddress("1AGNa15ZQXAZUgFiqJ2i7Z2DPU2J6hW62ia32d4s", "unknown")
    coinAddress.isValid shouldEqual(false)
  }

  "isValid" should "return false if the address is invalid" in {

    val coinAddress = CoinAddress("1ANNa15ZQXAZUgFiqJ2i7Z2DPU2J6hW62i", "unknown")
    coinAddress.isValid shouldEqual(false)
  }

  "isValid" should "return true if the address is valid" in {

    val coinAddress = CoinAddress("1AGNa15ZQXAZUgFiqJ2i7Z2DPU2J6hW62i", "unknown")
    coinAddress.isValid shouldEqual(true)
  }
}
