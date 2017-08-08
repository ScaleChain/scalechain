package io.scalechain.wallet

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.storage.index.KeyValueDatabase

import io.scalechain.blockchain.transaction.TransactionTestData
import io.scalechain.test.ShouldSpec
import io.scalechain.wallet.WalletStoreTestData

/**
  * Created by kangmo on 5/18/16.
  */
interface WalletStoreWalletOutputTest : ShouldSpec, WalletStoreTestInterface {
  var store: WalletStore
  var db : KeyValueDatabase

  fun addTests() {
    val W = WalletStoreTestData
    val T = TransactionTestData

    "putWalletOutput" should "put a wallet output." {
      store.putWalletOutput(db, T.OUTPOINT1, W.WALLET_OUTPUT1)
      store.getWalletOutput(db, T.OUTPOINT1) shouldBe W.WALLET_OUTPUT1
    }

    "putWalletOutput" should "overwrite the previous wallet output." {
      store.putWalletOutput(db, T.OUTPOINT1, W.WALLET_OUTPUT1)
      store.putWalletOutput(db, T.OUTPOINT1, W.WALLET_OUTPUT2)
      store.getWalletOutput(db, T.OUTPOINT1) shouldBe W.WALLET_OUTPUT2
    }

    "delWalletOutput" should "do nothing if the wallet output was not found." {
      store.delWalletOutput(db, T.OUTPOINT1)
    }

    "delWalletOutput" should "del a wallet output." {
      store.putWalletOutput(db, T.OUTPOINT1, W.WALLET_OUTPUT1)
      store.delWalletOutput(db, T.OUTPOINT1)
      store.getWalletOutput(db, T.OUTPOINT1) shouldBe null
    }

    "getWalletOutput" should "get nothing if no wallet output was found for the given outpoint." {
      store.putWalletOutput(db, T.OUTPOINT1, W.WALLET_OUTPUT1)
      store.getWalletOutput(db, T.OUTPOINT2) shouldBe null
    }

    "getWalletOutput" should "get a wallet output if it exists." {
      // No need to test, as it was tested in the following test case.
      // "putWalletOutput" should "put a wallet output." in
    }

    "markWalletOutputSpent" should "mark an output spent" {
      store.putWalletOutput(db, T.OUTPOINT1, W.WALLET_OUTPUT1)
      store.markWalletOutputSpent(db, T.OUTPOINT1, true)
      store.getWalletOutput(db, T.OUTPOINT1)?.spent shouldBe true
    }

    "markWalletOutputSpent" should "mark an output spent from unspent" {
      store.putWalletOutput(db, T.OUTPOINT1, W.WALLET_OUTPUT1)
      store.markWalletOutputSpent(db, T.OUTPOINT1, false)
      store.markWalletOutputSpent(db, T.OUTPOINT1, true)
      store.getWalletOutput(db, T.OUTPOINT1)?.spent shouldBe true
    }


    "markWalletOutputSpent" should "mark an output unspent" {
      store.putWalletOutput(db, T.OUTPOINT1, W.WALLET_OUTPUT1)
      store.markWalletOutputSpent(db, T.OUTPOINT1, true)
      store.markWalletOutputSpent(db, T.OUTPOINT1, false)
      store.getWalletOutput(db, T.OUTPOINT1)?.spent shouldBe false
    }

    "markWalletOutputSpent" should "return false if no wallet output was found for an out point." {
      store.markWalletOutputSpent(db, T.OUTPOINT1, true) shouldBe false
    }

    "markWalletOutputSpent" should "return true if a wallet output was found for an out point." {
      store.putWalletOutput(db, T.OUTPOINT1, W.WALLET_OUTPUT1)
      store.markWalletOutputSpent(db, T.OUTPOINT1, true) shouldBe true
    }
  }
}
