package io.scalechain.wallet

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait

/**
  * Created by kangmo on 5/18/16.
  */
interface WalletStoreWalletTransactionTestTrait  : WalletStoreTestDataTrait, Matchers {

  fun testWalletStoreWalletTransaction(store: WalletStore, db : KeyValueDatabase) : FlatSpec {
    return object : FlatSpec() {
      init {
        val W = WalletStoreTestDataTrait
        val T = TransactionTestDataTrait

        "putWalletTransaction" should "put a wallet transaction." {
          store.putWalletTransaction(db, T.TXHASH1, W.WALLET_TX1)
          store.getWalletTransaction(db, T.TXHASH1) shouldBe W.WALLET_TX1
        }

        "putWalletTransaction" should "overwrite the previous wallet transaction." {
          store.putWalletTransaction(db, T.TXHASH1, W.WALLET_TX1)
          store.putWalletTransaction(db, T.TXHASH1, W.WALLET_TX2)
          store.getWalletTransaction(db, T.TXHASH1) shouldBe W.WALLET_TX2
        }

        "delWalletTransaction" should "do nothing if there was no transaction for a transaction hash." {
          store.delWalletTransaction(db, T.TXHASH1)
        }

        "delWalletTransaction" should "del a wallet transaction when there was only one transaction." {
          store.putWalletTransaction(db, T.TXHASH1, W.WALLET_TX1)
          store.delWalletTransaction(db, T.TXHASH1)
          store.getWalletTransaction(db, T.TXHASH1) shouldBe null
        }

        "delWalletTransaction" should "del a wallet transaction when there were many transactions." {
          store.putWalletTransaction(db, T.TXHASH1, W.WALLET_TX1)
          store.putWalletTransaction(db, T.TXHASH2, W.WALLET_TX2)
          store.delWalletTransaction(db, T.TXHASH1)
          store.getWalletTransaction(db, T.TXHASH1) shouldBe null
          store.getWalletTransaction(db, T.TXHASH2) shouldBe W.WALLET_TX2
        }

        "getWalletTransaction" should "get nothing if no wallet transaction was found for the given output transaction hash" {
          store.getWalletTransaction(db, T.TXHASH1) shouldBe null
        }

        "getWalletTransaction" should "get a wallet transaction if exists." {
          // No need to test, as it was tested in the following test case.
          // "putWalletTransaction" should "put a wallet output." in
        }
      }
    }
  }
}


