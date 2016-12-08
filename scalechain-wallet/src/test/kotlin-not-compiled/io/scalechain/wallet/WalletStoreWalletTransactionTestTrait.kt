package io.scalechain.wallet

import io.scalechain.blockchain.storage.index.KeyValueDatabase
import org.scalatest.*

/**
  * Created by kangmo on 5/18/16.
  */
trait WalletStoreWalletTransactionTestTrait : FlatSpec with WalletStoreTestDataTrait with BeforeAndAfterEach with Matchers{
  var store : WalletStore
  implicit var db : KeyValueDatabase

  "putWalletTransaction" should "put a wallet transaction." {
    store.putWalletTransaction(TXHASH1, WALLET_TX1)
    store.getWalletTransaction(TXHASH1) shouldBe WALLET_TX1)
  }

  "putWalletTransaction" should "overwrite the previous wallet transaction." {
    store.putWalletTransaction(TXHASH1, WALLET_TX1)
    store.putWalletTransaction(TXHASH1, WALLET_TX2)
    store.getWalletTransaction(TXHASH1) shouldBe WALLET_TX2)
  }

  "delWalletTransaction" should "do nothing if there was no transaction for a transaction hash." {
    store.delWalletTransaction(TXHASH1)
  }

  "delWalletTransaction" should "del a wallet transaction when there was only one transaction." {
    store.putWalletTransaction(TXHASH1, WALLET_TX1)
    store.delWalletTransaction(TXHASH1)
    store.getWalletTransaction(TXHASH1) shouldBe null
  }

  "delWalletTransaction" should "del a wallet transaction when there were many transactions." {
    store.putWalletTransaction(TXHASH1, WALLET_TX1)
    store.putWalletTransaction(TXHASH2, WALLET_TX2)
    store.delWalletTransaction(TXHASH1)
    store.getWalletTransaction(TXHASH1) shouldBe null
    store.getWalletTransaction(TXHASH2) shouldBe WALLET_TX2)
  }

  "getWalletTransaction" should "get nothing if no wallet transaction was found for the given output transaction hash" {
    store.getWalletTransaction(TXHASH1) shouldBe null
  }

  "getWalletTransaction" should "get a wallet transaction if exists." {
    // No need to test, as it was tested in the following test case.
    // "putWalletTransaction" should "put a wallet output." in
  }
}


