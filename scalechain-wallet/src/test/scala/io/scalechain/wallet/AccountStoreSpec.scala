package io.scalechain.wallet

import io.scalechain.blockchain.{WalletException, ErrorCode}
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

  "getAccount" should "throw an exception if the addreses is invalid" in {

    val accountStore = new AccountStore()
    val coinAddress = CoinAddress("1AGNa15ZQXAZUgFiqJ2i7Z2DPU2J6hW62ia32d4s")

    val thrown = the [WalletException] thrownBy accountStore.getAccount(coinAddress)
    thrown.code shouldBe ErrorCode.AddressNotValid
  }

}
