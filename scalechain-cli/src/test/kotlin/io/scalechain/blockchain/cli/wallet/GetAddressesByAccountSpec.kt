package io.scalechain.blockchain.cli.wallet

import com.google.gson.JsonPrimitive
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.wallet.GetAddressesByAccount
import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.StringListResult
import io.scalechain.blockchain.cli.APITestSuite
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(KTestJUnitRunner::class)
class GetAddressesByAccountSpec : APITestSuite() {
  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
  }

  init {
    val defaultAccount = AccountNameSample.default()
    val account1 = AccountNameSample.random()
    val account2 = AccountNameSample.random()

    val accounts = listOf(defaultAccount, account1, account2)

    "GetAddressesByAccount" should "return an empty list(address from the default account \"\") when no account parameter" {
      val result = invoke(GetAddressesByAccount, listOf())
      result.right().toString() shouldBe "StringListResult(value=[])"
    }

    "GetAddressesByAccount" should "return empty list each for each account" {
      accounts.forEach {
        val result = invoke(GetAddressesByAccount, listOf(JsonPrimitive(it)))
        val addresses = (result.right()!! as StringListResult).value

        addresses should haveSize(0)
      }
    }

    "GetAddressesByAccount" should "return one address for ''(default account)" {
      val address = Data.Addr1.address

      wallet.importOutputOwnership(chain.db, chain, defaultAccount, address, false)

      val result = invoke(GetAddressesByAccount, listOf(JsonPrimitive(defaultAccount)))
      val addresses = (result.right()!! as StringListResult).value

      addresses should haveSize(1)
      addresses[0] shouldBe address.stringKey()
    }

    "GetAddressesByAccount" should "return two address for account1" {
      val address1 = Data.Addr1.address
      val address2 = Data.Addr2.address

      wallet.importOutputOwnership(chain.db, chain, account1, address1, false)
      wallet.importOutputOwnership(chain.db, chain, account1, address2, false)

      val result = invoke(GetAddressesByAccount, listOf(JsonPrimitive(account1)))
      val addresses = (result.right()!! as StringListResult).value

      addresses should haveSize(2)
      assertTrue(addresses.contains(address1.stringKey()))
      assertTrue(addresses.contains(address2.stringKey()))
    }

    "GetAddressesByAccount" should "return three address for account2" {
      val address1 = Data.Addr1.address
      val address2 = Data.Addr2.address
      val address3 = Data.Addr3.address

      listOf(address1, address2, address3).forEach {
        wallet.importOutputOwnership(chain.db, chain, account2, it, false)
      }

      val result = invoke(GetAddressesByAccount, listOf(JsonPrimitive(account2)))
      val addresses = (result.right()!! as StringListResult).value

      addresses should haveSize(3)
      listOf(address1, address2, address3).forEach {
        assertTrue(addresses.contains(it.stringKey()))
      }
    }
  }
}

