package io.scalechain.wallet

import org.scalatest.{Suite, ShouldMatchers, FlatSpec}

/**
  * Created by mijeong on 2016. 3. 8..
  */
class AccountSpec extends FlatSpec with ShouldMatchers {
  this: Suite =>

  "newAddress" should "return CoinAddress instance" in {

    val account = new Account("test")
    account.newAddress shouldBe a [CoinAddress]
  }

}
