package io.scalechain.wallet

import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 4..
  */
class AccountStoreSpec extends FlatSpec with ShouldMatchers {
  this: Suite =>

  "getAccount" should "return Account instance if the parameter exists" in {

    val accountStore = new AccountStore()
    val coinAddress = CoinAddress("mjSk1Ny9spzU2fouzYgLqGUD8U41iR35QN")

    accountStore.getAccount(coinAddress) shouldBe a [Account]
  }

}
