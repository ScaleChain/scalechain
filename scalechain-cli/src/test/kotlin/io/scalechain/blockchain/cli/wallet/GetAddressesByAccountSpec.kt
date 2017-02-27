package io.scalechain.blockchain.cli.wallet

import com.google.gson.JsonPrimitive
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.api.command.wallet.GetAddressesByAccount
import io.scalechain.blockchain.api.domain.StringListResult
import io.scalechain.blockchain.cli.APITestSuite
import io.scalechain.blockchain.transaction.OutputOwnership
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class GetAddressesByAccountSpec : APITestSuite() {
  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
  }

  init {
    val accounts = listOf("Account1", "Account2", "Account3")
    "GetAccountAddress() for each account" should "return empty list each" {
      accounts.forEach {
        val result = invoke(GetAddressesByAccount, listOf(JsonPrimitive(it)))
        val addresses = (result.right()!! as StringListResult).value

        addresses should haveSize(0)
      }
    }

    "GetAccountAddress() for each account" should "return one address each" {
      val accountAddressMap = mapOf<String, OutputOwnership>(
        Pair(accounts[0], Data.Addr1.address),
        Pair(accounts[1], Data.Addr2.address),
        Pair(accounts[2], Data.Addr3.address)
      )

      wallet.importOutputOwnership(chain.db, chain, accounts[0], accountAddressMap[accounts[0]]!!, false)
      wallet.importOutputOwnership(chain.db, chain, accounts[1], accountAddressMap[accounts[1]]!!, false)
      wallet.importOutputOwnership(chain.db, chain, accounts[2], accountAddressMap[accounts[2]]!!, false)

      accountAddressMap.entries.forEach {
        val account = it.key
        val address = it.value.stringKey()

        val result = invoke(GetAddressesByAccount, listOf(JsonPrimitive(account)))
        val addresses = (result.right()!! as StringListResult).value

        addresses should haveSize(1)
        addresses[0] shouldBe address
      }
    }
  }
}
