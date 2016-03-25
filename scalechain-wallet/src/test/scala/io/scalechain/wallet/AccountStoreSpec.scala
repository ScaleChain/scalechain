package io.scalechain.wallet

import io.scalechain.blockchain.{RpcException, ErrorCode}
import io.scalechain.util.HexUtil._
import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 4..
  */
class AccountStoreSpec extends FlatSpec with ShouldMatchers {
  this: Suite =>

  "getAccount" should "return Account instance if the parameter exists" in {

    val accountStore = new AccountStore()
    val coinAddress = CoinAddress("1AxwNopFNkQFuDsMgCS5ev2GvzB5fWo1ct", "unknown", bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7"), bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d711"))

    accountStore.getAccount(coinAddress) shouldBe a [Account]
  }

  "getAccount" should "throw an exception if the addreses is invalid" in {

    val accountStore = new AccountStore()
    val coinAddress = CoinAddress("1AxwNopFNkQFuDsMgCS5ev2GvzB5fWo1c222t", "unknown", bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d7"), bytes("0210a8167c757b3f6277506ed016ebdbcc1003eb62f72b378e05776287863db2d711"))

    val thrown = the [RpcException] thrownBy accountStore.getAccount(coinAddress)
    thrown.code shouldBe ErrorCode.RpcInvalidAddress
  }

  "isValid" should "return boolean value if the parameter exists" in {

    val accountStore = new AccountStore()
    val account = "test"

    accountStore.isValid(account) shouldBe a [java.lang.Boolean]
  }

  "isValid" should "return true value if the account name is valid" in {

    val accountStore = new AccountStore()
    val account = "test"

    accountStore.isValid(account) shouldEqual(true)
  }

  "isValid" should "return false value if the account name is invalid" in {

    val accountStore = new AccountStore()
    val account = "*"

    accountStore.isValid(account) shouldEqual(false)
  }
}
