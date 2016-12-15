package io.scalechain.wallet

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File

import io.scalechain.blockchain.storage.index.KeyValueDatabase

import io.scalechain.blockchain.transaction.TransactionTestData
import io.scalechain.wallet.WalletStoreTestData

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.WalletException
import io.scalechain.blockchain.proto.OutPoint

/**
  * Created by kangmo on 5/18/16.
  */
interface WalletStoreOutPointTest : WalletStoreTestInterface, Matchers {

  fun testWalletStoreOutPoint(store: WalletStore, db : KeyValueDatabase) : FlatSpec {
    return object : FlatSpec() {
      init {
        val W = WalletStoreTestData
        val T = TransactionTestData

        fun prepareOutPointTest() {
          store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
          store.putOutputOwnership(db, W.ACCOUNT2, T.ADDR2.address)
          store.putOutputOwnership(db, W.ACCOUNT3, T.ADDR3.address)
        }

        "putTransactionOutPoint" should "put an out point per output ownership." {
          prepareOutPointTest()

          assert(store.ownershipExists(db, T.ADDR1.address))
          assert(store.ownershipExists(db, T.ADDR2.address))
          assert(store.ownershipExists(db, T.ADDR3.address))

          store.putTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT1)
          store.getTransactionOutPoints(db, T.ADDR1.address).toSet() shouldBe setOf(T.OUTPOINT1)
        }

        "putTransactionOutPoint" should "put many out points per output ownership." {
          prepareOutPointTest()

          store.putTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT1)
          store.putTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT2)
          store.putTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT3)
          store.getTransactionOutPoints(db, T.ADDR1.address).toSet() shouldBe setOf(T.OUTPOINT1, T.OUTPOINT2, T.OUTPOINT3)
        }

        "delTransactionOutPoint" should "do nothing if the out point was not found for an ownership." {
          prepareOutPointTest()

          store.delTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT1)
        }

        "delTransactionOutPoint" should "del an out point when it was the only out point for an ownership." {
          prepareOutPointTest()

          store.putTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT1)
          store.delTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT1)
          store.getTransactionOutPoints(db, T.ADDR1.address).toSet() shouldBe setOf<OutPoint>()
        }

        "getTransactionOutPoints" should "del an out point when it was NOT the only out point for an ownership." {
          prepareOutPointTest()

          store.putTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT1)
          store.putTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT2)
          store.putTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT3)
          store.delTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT1)
          store.getTransactionOutPoints(db, T.ADDR1.address).toSet() shouldBe setOf(T.OUTPOINT2, T.OUTPOINT3)
        }

        "getTransactionOutPoints" should "get all out points for all output ownerships if None is passed for the parameter." {
          prepareOutPointTest()

          store.putTransactionOutPoint(db, T.ADDR1.address, T.OUTPOINT1)
          store.putTransactionOutPoint(db, T.ADDR2.address, T.OUTPOINT2)
          store.putTransactionOutPoint(db, T.ADDR3.address, T.OUTPOINT3)

          store.getTransactionOutPoints(db, null).toSet() shouldBe setOf(T.OUTPOINT1, T.OUTPOINT2, T.OUTPOINT3)
        }


        "putTransactionOutPoint" should "throw an exception if the output ownership does not exist." {
          prepareOutPointTest()

          val thrown = shouldThrow<WalletException> {
            store.putTransactionOutPoint(db, T.ADDR1.pubKeyScript, T.OUTPOINT1)
          }
          thrown.code shouldBe ErrorCode.OwnershipNotFound
        }
      }
    }
  }
}
