package io.scalechain.wallet

import io.scalechain.util.HexUtil._
import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 4..
  */
class CoinAddressSpec extends FlatSpec with ShouldMatchers {
  this: Suite =>

  "isValid" should "return boolean value if the parameter exists" in {

    val coinAddress = CoinAddress("1AGNa15ZQXAZUgFiqJ2i7Z2DPU2J6hW62i", "unknown", bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7"), bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d711"))
    coinAddress.isValid shouldBe a [java.lang.Boolean]
  }

  "isValid" should "return false if the address has invalid length" in {

    val coinAddress = CoinAddress("1AGNa15ZQXAZUgFiqJ2i7Z2DPU2J6hW62ia32d4s", "unknown", bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7"), bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d711"))
    coinAddress.isValid shouldEqual(false)
  }

  "isValid" should "return false if the address is invalid" in {

    val coinAddress = CoinAddress("1ANNa15ZQXAZUgFiqJ2i7Z2DPU2J6hW62i", "unknown", bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7"), bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d711"))
    coinAddress.isValid shouldEqual(false)
  }

  "isValid" should "return true if the address is valid" in {

    val coinAddress = CoinAddress("1AGNa15ZQXAZUgFiqJ2i7Z2DPU2J6hW62i", "unknown", bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7"), bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d711"))
    coinAddress.isValid shouldEqual(true)
  }
}
