package io.scalechain.wallet

import io.kotlintest.matchers.Matchers

import io.scalechain.blockchain.transaction.TransactionTestDataTrait

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.WalletTransaction
import io.scalechain.blockchain.proto.WalletOutput
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.transaction.OutputOwnership
import io.scalechain.util.HexUtil.bytes

/**
  * Created by kangmo on 5/18/16.
  */
interface WalletStoreTestDataTrait : TransactionTestDataTrait, Matchers {

  fun generateWalletOutput(transaction : Transaction) : WalletOutput {
    return WalletOutput(
      blockindex    = 100L,
      coinbase      = true,
      spent         = false,
      transactionOutput = transaction.outputs[0]
    )
  }

  fun generateWalletTransaction(transaction : Transaction) : WalletTransaction {
    return WalletTransaction(
      blockHash        = Hash(bytes("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
      blockIndex       = 11,
      blockTime        = 1411051649,
      transactionId    = transaction.hash(),
      addedTime        = 1418695703L,
      transactionIndex = 1,
      transaction = transaction
    )
  }

  fun checkElementEquality(actualOwnerships : List<OutputOwnership>, expectedOwnerships : Set<OutputOwnership> )  {
//    scrubScript( actualOwnerships.toList.sortBy(_.stringKey()) ) shouldBe expectedOwnerships.sortBy(_.stringKey())
      scrubScript( actualOwnerships ).toSet()  shouldBe expectedOwnerships
  }

  companion object {
    private val W = object : WalletStoreTestDataTrait {}

    val WALLET_OUTPUT1 = W.generateWalletOutput(W.transaction1())
    val WALLET_OUTPUT2 = W.generateWalletOutput(W.transaction2())
    val WALLET_OUTPUT3 = W.generateWalletOutput(W.transaction3())

    val WALLET_TX1 = W.generateWalletTransaction(W.transaction1())
    val WALLET_TX2 = W.generateWalletTransaction(W.transaction2())
    val WALLET_TX3 = W.generateWalletTransaction(W.transaction3())

    val ACCOUNT1 = "acc1"
    val ACCOUNT2 = "acc2"
    val ACCOUNT3 = "acc3"

  }
}
