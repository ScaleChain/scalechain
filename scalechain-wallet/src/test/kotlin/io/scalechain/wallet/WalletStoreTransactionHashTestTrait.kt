package io.scalechain.wallet

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.storage.index.KeyValueDatabase

import io.scalechain.blockchain.transaction.TransactionTestDataTrait

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.WalletException
import io.scalechain.blockchain.proto.Hash

/**
  * Created by kangmo on 5/18/16.
  */
interface WalletStoreTransactionHashTestTrait : WalletStoreTestDataTrait, Matchers {

  fun testWalletStoreTransactionHash(store: WalletStore, db : KeyValueDatabase) : FlatSpec {
    return object : FlatSpec() {
      init {
        val W = WalletStoreTestDataTrait
        val T = TransactionTestDataTrait


        fun prepareTxHashTest() {
          store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
          store.putOutputOwnership(db, W.ACCOUNT2, T.ADDR2.address)
          store.putOutputOwnership(db, W.ACCOUNT3, T.ADDR3.address)
        }

        "putTransactionHash" should "put a transaction hash per output ownership." {
          prepareTxHashTest()

          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH1)
          store.getTransactionHashes(db, T.ADDR1.address).toSet() shouldBe setOf(T.TXHASH1)
        }

        "putTransactionHash" should "put many transaction hashes per output ownership." {
          prepareTxHashTest()

          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH1)
          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH2)
          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH3)
          store.getTransactionHashes(db, T.ADDR1.address).toSet() shouldBe setOf(T.TXHASH1, T.TXHASH2, T.TXHASH3)
        }

        "delTransactionHash" should "do nothing if there was no hash for an ownership." {
          prepareTxHashTest()

          store.delTransactionHash(db, T.ADDR1.address, T.TXHASH1)
        }

        "delTransactionHash" should "del a transaction hash when it was the only hash for an ownership." {
          prepareTxHashTest()

          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH1)
          store.delTransactionHash(db, T.ADDR1.address, T.TXHASH1)
          store.getTransactionHashes(db, T.ADDR1.address).toSet() shouldBe setOf<Hash>()
        }

        "delTransactionHash" should "del a transaction hash when it was NOT the only hash for an ownership." {
          prepareTxHashTest()

          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH1)
          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH2)
          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH3)
          store.delTransactionHash(db, T.ADDR1.address, T.TXHASH2)
          store.getTransactionHashes(db, T.ADDR1.address).toSet() shouldBe setOf(T.TXHASH1, T.TXHASH3)
        }

        "getTransactionHashes(db, none)" should "get nothing if no transaction hash was put." {
          prepareTxHashTest()

          store.getTransactionHashes(db, null).toSet() shouldBe setOf<Hash>()
        }

        "getTransactionHashes(db, addr)" should "get nothing if no transaction hash was put." {
          prepareTxHashTest()

          store.getTransactionHashes(db, T.ADDR1.address).toSet() shouldBe setOf<Hash>()
        }

        "getTransactionHashes(db, none)" should "get all transaction hashes" {
          prepareTxHashTest()

          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH1)
          store.putTransactionHash(db, T.ADDR2.address, T.TXHASH2)
          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH3)

          store.getTransactionHashes(db, null).toSet() shouldBe setOf(T.TXHASH1, T.TXHASH2, T.TXHASH3)
        }

        "getTransactionHashes(db, addr)" should "get all transaction hashes only for the address" {
          prepareTxHashTest()

          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH1)
          store.putTransactionHash(db, T.ADDR2.address, T.TXHASH2)
          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH3)

          store.getTransactionHashes(db, T.ADDR1.address).toSet() shouldBe setOf(T.TXHASH1, T.TXHASH3)
        }


        "getTransactionHashes".config(ignored=true) should "get all transaction hashes for an output ownership." {
          // No need to implement, already tested in the following test case.
          // "putTransactionHash" should "put many transaction hashes per output ownership."
        }

        "ownershipExists" should "return true if the ownership exists" {
          prepareTxHashTest()

          store.ownershipExists(db,T.ADDR1.address) shouldBe true
        }

        "ownershipExists" should "return false if the ownership does not exist" {
          prepareTxHashTest()

          store.ownershipExists(db,T.ADDR1.pubKeyScript) shouldBe false
        }

        "putTransactionHash" should "throw an exception if the output ownership does not exist." {
          prepareTxHashTest()

          val thrown = shouldThrow<WalletException> {
            store.putTransactionHash(db, T.ADDR1.pubKeyScript, T.TXHASH1)
          }
          thrown.code shouldBe ErrorCode.OwnershipNotFound
        }

        "getTransactionHashes" should "should get hashes for an account even though the same hash belongs to multiple addresses" {
          prepareTxHashTest()

          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH1)
          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH2)
          store.putTransactionHash(db, T.ADDR1.address, T.TXHASH3)
          store.putTransactionHash(db, T.ADDR2.address, T.TXHASH2)
          store.putTransactionHash(db, T.ADDR2.address, T.TXHASH3)
          store.putTransactionHash(db, T.ADDR3.address, T.TXHASH1)
          store.putTransactionHash(db, T.ADDR3.address, T.TXHASH3)

          store.getTransactionHashes(db, T.ADDR1.address).toSet() shouldBe setOf(T.TXHASH1, T.TXHASH2, T.TXHASH3)
          store.getTransactionHashes(db, T.ADDR2.address).toSet() shouldBe setOf(T.TXHASH2, T.TXHASH3)
          store.getTransactionHashes(db, T.ADDR3.address).toSet() shouldBe setOf(T.TXHASH1, T.TXHASH3)

        }
      }
    }
  }
}
