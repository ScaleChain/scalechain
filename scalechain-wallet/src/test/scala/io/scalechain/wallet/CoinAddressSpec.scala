package io.scalechain.wallet

import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 4..
  */
class CoinAddressSpec extends FlatSpec with ShouldMatchers {
  this: Suite =>

  "isValid" should "return boolean value if the parameter exists" in {

    val coinAddress = CoinAddress("mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN")
    coinAddress.isValid shouldBe a [java.lang.Boolean]
  }
}
