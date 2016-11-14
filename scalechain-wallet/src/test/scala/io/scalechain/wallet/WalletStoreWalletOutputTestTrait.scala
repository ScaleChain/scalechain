package io.scalechain.wallet

import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.{ErrorCode, WalletException}
import org.scalatest._

/**
  * Created by kangmo on 5/18/16.
  */
trait WalletStoreWalletOutputTestTrait extends FlatSpec with WalletStoreTestDataTrait with Matchers{
  var store : WalletStore
  implicit var db : KeyValueDatabase

  "putWalletOutput" should "put a wallet output." in {
    store.putWalletOutput(OUTPOINT1, WALLET_OUTPUT1)
    store.getWalletOutput(OUTPOINT1) shouldBe Some(WALLET_OUTPUT1)
  }

  "putWalletOutput" should "overwrite the previous wallet output." in {
    store.putWalletOutput(OUTPOINT1, WALLET_OUTPUT1)
    store.putWalletOutput(OUTPOINT1, WALLET_OUTPUT2)
    store.getWalletOutput(OUTPOINT1) shouldBe Some(WALLET_OUTPUT2)
  }

  "delWalletOutput" should "do nothing if the wallet output was not found." in {
    store.delWalletOutput(OUTPOINT1)
  }

  "delWalletOutput" should "del a wallet output." in {
    store.putWalletOutput(OUTPOINT1, WALLET_OUTPUT1)
    store.delWalletOutput(OUTPOINT1)
    store.getWalletOutput(OUTPOINT1) shouldBe None
  }

  "getWalletOutput" should "get nothing if no wallet output was found for the given outpoint." in {
    store.putWalletOutput(OUTPOINT1, WALLET_OUTPUT1)
    store.getWalletOutput(OUTPOINT2) shouldBe None
  }

  "getWalletOutput" should "get a wallet output if it exists." ignore {
    // No need to test, as it was tested in the following test case.
    // "putWalletOutput" should "put a wallet output." in
  }

  "markWalletOutputSpent" should "mark an output spent" in {
    store.putWalletOutput(OUTPOINT1, WALLET_OUTPUT1)
    store.markWalletOutputSpent(OUTPOINT1, true)
    store.getWalletOutput(OUTPOINT1).map(_.spent) shouldBe Some(true)
  }

  "markWalletOutputSpent" should "mark an output spent from unspent" in {
    store.putWalletOutput(OUTPOINT1, WALLET_OUTPUT1)
    store.markWalletOutputSpent(OUTPOINT1, false)
    store.markWalletOutputSpent(OUTPOINT1, true)
    store.getWalletOutput(OUTPOINT1).map(_.spent) shouldBe Some(true)
  }


  "markWalletOutputSpent" should "mark an output unspent" in {
    store.putWalletOutput(OUTPOINT1, WALLET_OUTPUT1)
    store.markWalletOutputSpent(OUTPOINT1, true)
    store.markWalletOutputSpent(OUTPOINT1, false)
    store.getWalletOutput(OUTPOINT1).map(_.spent) shouldBe Some(false)
  }

  "markWalletOutputSpent" should "return false if no wallet output was found for an out point." in {
    store.markWalletOutputSpent(OUTPOINT1, true) shouldBe false
  }

  "markWalletOutputSpent" should "return true if a wallet output was found for an out point." in {
    store.putWalletOutput(OUTPOINT1, WALLET_OUTPUT1)
    store.markWalletOutputSpent(OUTPOINT1, true) shouldBe true
  }
}
