package io.scalechain.wallet

import io.scalechain.blockchain.storage.index.KeyValueDatabase
import org.scalatest._

/**
  * Created by kangmo on 5/18/16.
  */
trait WalletStoreWalletTransactionTestTrait : FlatSpec with WalletStoreTestDataTrait with BeforeAndAfterEach with Matchers{
  var store : WalletStore
  implicit var db : KeyValueDatabase

  "putWalletTransaction" should "put a wallet transaction." in {
    store.putWalletTransaction(TXHASH1, WALLET_TX1)
    store.getWalletTransaction(TXHASH1) shouldBe Some(WALLET_TX1)
  }

  "putWalletTransaction" should "overwrite the previous wallet transaction." in {
    store.putWalletTransaction(TXHASH1, WALLET_TX1)
    store.putWalletTransaction(TXHASH1, WALLET_TX2)
    store.getWalletTransaction(TXHASH1) shouldBe Some(WALLET_TX2)
  }

  "delWalletTransaction" should "do nothing if there was no transaction for a transaction hash." in {
    store.delWalletTransaction(TXHASH1)
  }

  "delWalletTransaction" should "del a wallet transaction when there was only one transaction." in {
    store.putWalletTransaction(TXHASH1, WALLET_TX1)
    store.delWalletTransaction(TXHASH1)
    store.getWalletTransaction(TXHASH1) shouldBe None
  }

  "delWalletTransaction" should "del a wallet transaction when there were many transactions." in {
    store.putWalletTransaction(TXHASH1, WALLET_TX1)
    store.putWalletTransaction(TXHASH2, WALLET_TX2)
    store.delWalletTransaction(TXHASH1)
    store.getWalletTransaction(TXHASH1) shouldBe None
    store.getWalletTransaction(TXHASH2) shouldBe Some(WALLET_TX2)
  }

  "getWalletTransaction" should "get nothing if no wallet transaction was found for the given output transaction hash" in {
    store.getWalletTransaction(TXHASH1) shouldBe None
  }

  "getWalletTransaction" should "get a wallet transaction if exists." in {
    // No need to test, as it was tested in the following test case.
    // "putWalletTransaction" should "put a wallet output." in
  }
}


