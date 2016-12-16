package io.scalechain.wallet

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.WalletException
import io.scalechain.blockchain.transaction.OutputOwnership
import io.scalechain.blockchain.transaction.PrivateKey
import io.scalechain.blockchain.transaction.TransactionTestData
import io.scalechain.test.ShouldSpec
import io.scalechain.test.ShouldSpec.Companion.shouldThrow
import io.scalechain.wallet.WalletStoreTestData

/**
  * Created by kangmo on 5/18/16.
  */

interface WalletStoreAccountTest : ShouldSpec, WalletStoreTestInterface {
  var store: WalletStore
  var db : KeyValueDatabase

  fun addTests() {
    val W = WalletStoreTestData
    val T = TransactionTestData
    "putOutputOwnership" should "be able to put an output ownership." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      scrubScript( store.getOutputOwnerships(db, W.ACCOUNT1) ).toSet() shouldBe setOf(T.ADDR1.address)
    }

    "putOutputOwnership" should "be able to put multiple output ownerships. (mixed)" {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR2.pubKeyScript)
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR3.address)

      checkElementEquality(
        store.getOutputOwnerships(db, W.ACCOUNT1),
        setOf(T.ADDR1.address, T.ADDR2.pubKeyScript, T.ADDR3.address)
      )
    }

    "putOutputOwnership" should "be able to put multiple output ownerships. (coin addresses only)" {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR2.address)
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR3.address)
      checkElementEquality(
        store.getOutputOwnerships(db, W.ACCOUNT1),
        setOf(T.ADDR1.address, T.ADDR2.address, T.ADDR3.address)
      )
    }

    "putOutputOwnership" should "be able to put multiple output ownerships. (public key scripts only)" {
      checkElementEquality(
        store.getOutputOwnerships(db, W.ACCOUNT1),
        setOf<OutputOwnership>()
      )

      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.pubKeyScript)

      checkElementEquality(
        store.getOutputOwnerships(db, W.ACCOUNT1),
        setOf(T.ADDR1.pubKeyScript)
      )

      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR2.pubKeyScript)

      checkElementEquality(
        store.getOutputOwnerships(db, W.ACCOUNT1),
        setOf(T.ADDR1.pubKeyScript, T.ADDR2.pubKeyScript)
      )

      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR3.pubKeyScript)
      checkElementEquality(
        store.getOutputOwnerships(db, W.ACCOUNT1),
        setOf(T.ADDR1.pubKeyScript, T.ADDR2.pubKeyScript, T.ADDR3.pubKeyScript)
      )
    }


    "getReceivingAddress" should "get nothing if no receiving address is attached to an account." {
      store.getReceivingAddress(db, W.ACCOUNT1) shouldBe null
    }

    "getReceivingAddress" should "get a receiving address if a receiving address was attached to the account." {
      store.putReceivingAddress(db, W.ACCOUNT1, T.ADDR1.address)
      store.getReceivingAddress(db, W.ACCOUNT1) shouldBe T.ADDR1.address
    }

    "putReceivingAddress" should "put a receiving address for an account" {
      // No need to test this case, as it was tested in the following test.
      // "getReceivingAddress" should "get a receiving address if a receiving address was attached to the account." in
    }

    "putReceivingAddress" should "replace the previous receiving address if any." {

      store.putReceivingAddress(db, W.ACCOUNT1, T.ADDR1.address)
      store.getReceivingAddress(db, W.ACCOUNT1) shouldBe T.ADDR1.address

      store.putReceivingAddress(db, W.ACCOUNT1, T.ADDR2.address)
      store.getReceivingAddress(db, W.ACCOUNT1) shouldBe T.ADDR2.address
    }

    "putReceivingAddress" should "put a public key script." {
      store.putReceivingAddress(db, W.ACCOUNT1, T.ADDR1.pubKeyScript)
      scrubScript( store.getReceivingAddress(db, W.ACCOUNT1)!! ) shouldBe T.ADDR1.pubKeyScript

      store.putReceivingAddress(db, W.ACCOUNT1, T.ADDR2.pubKeyScript)
      scrubScript( store.getReceivingAddress(db, W.ACCOUNT1)!! ) shouldBe T.ADDR2.pubKeyScript
    }

    "getAccount" should "get nothing if the given output ownership was put for an account." {
      store.getAccount(db, T.ADDR1.address) shouldBe null
    }

    "getAccount" should "get the account that has the given output ownership. (when output ownership count is 1 )." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.getAccount(db, T.ADDR1.address) shouldBe W.ACCOUNT1
    }

    "getAccount" should "get the account that has the given output ownership. (when output ownership count is 2 )." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putOutputOwnership(db, W.ACCOUNT2, T.ADDR2.address)

      store.getAccount(db, T.ADDR1.address) shouldBe W.ACCOUNT1
      store.getAccount(db, T.ADDR2.address) shouldBe W.ACCOUNT2
    }

    "getOutputOwnerships" should "get all output ownerships if None is passed for the parameter." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putOutputOwnership(db, W.ACCOUNT2, T.ADDR2.address)
      store.putOutputOwnership(db, W.ACCOUNT3, T.ADDR3.address)

      checkElementEquality(
        store.getOutputOwnerships(db, null),
        setOf(T.ADDR1.address, T.ADDR2.address, T.ADDR3.address)
      )
    }

    "getOutputOwnerships" should "get output ownerships that an account has if Some(account) is passed for the parameter." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putOutputOwnership(db, W.ACCOUNT2, T.ADDR2.address)
      checkElementEquality(
        store.getOutputOwnerships(db, W.ACCOUNT1),
        setOf(T.ADDR1.address)
      )
      checkElementEquality(
        store.getOutputOwnerships(db, W.ACCOUNT2),
        setOf(T.ADDR2.address)
      )
    }

    "getOutputOwnerships" should "NOT get output ownerships of another account." {
      // No need to test this case, as it was tested in the following test.
      // "getOutputOwnerships" should "get output ownerships that an account has if Some(account) is passed for the parameter."
    }


    "getPrivateKeys(db, None)" should "get nothing if no private key was put." {
      store.getPrivateKeys(db, null) shouldBe listOf<PrivateKey>()
    }

    "getPrivateKeys(db, None)" should "get all private keys put." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putOutputOwnership(db, W.ACCOUNT2, T.ADDR2.address)
      store.putOutputOwnership(db, W.ACCOUNT3, T.ADDR3.address)
      store.putPrivateKeys(db, T.ADDR1.address, listOf(T.ADDR1.privateKey))
      store.putPrivateKeys(db, T.ADDR2.address, listOf(T.ADDR2.privateKey))
      store.putPrivateKeys(db, T.ADDR3.address, listOf(T.ADDR3.privateKey))
      store.getPrivateKeys(db, null).toSet() shouldBe setOf( T.ADDR1.privateKey, T.ADDR2.privateKey, T.ADDR3.privateKey)
    }

    "getPrivateKeys(db, addr)" should "get nothing if no private key was put for an address" {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.getPrivateKeys(db, T.ADDR1.address).toSet() shouldBe setOf<PrivateKey>()
    }

    "putPrivateKeys(db, addr)" should "put a private key for an output ownership." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putPrivateKeys(db, T.ADDR1.address, listOf(T.ADDR1.privateKey))
      store.getPrivateKeys(db, T.ADDR1.address).toSet() shouldBe setOf(T.ADDR1.privateKey)
    }

    "putPrivateKey" should "overwrite the previous private key for an output ownership." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putPrivateKeys(db, T.ADDR1.address, listOf(T.ADDR1.privateKey))
      store.putPrivateKeys(db, T.ADDR1.address, listOf(T.ADDR2.privateKey))
      store.getPrivateKeys(db, T.ADDR1.address).toSet() shouldBe setOf(T.ADDR2.privateKey)
    }

    "getPrivateKeys(db, pubKeyScript)" should "get nothing if no private key was put for a public key script" {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.pubKeyScript)
      store.getPrivateKeys(db, T.ADDR1.pubKeyScript).toSet() shouldBe setOf<PrivateKey>()
    }

    "getPrivateKeys(db, pubKeyScript)" should "get a private key for a public key script." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.pubKeyScript)
      store.putPrivateKeys(db, T.ADDR1.pubKeyScript, listOf(T.ADDR1.privateKey))
      store.getPrivateKeys(db, T.ADDR1.pubKeyScript).toSet() shouldBe setOf(T.ADDR1.privateKey)
    }

    "getPrivateKeys(db, pubKeyScript)" should "get a nothing even with a matching address." {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.pubKeyScript)
      store.putPrivateKeys(db, T.ADDR1.pubKeyScript, listOf(T.ADDR1.privateKey))
      // Try to get with address instead of pubKeyScript
      store.getPrivateKeys(db, T.ADDR1.address).toSet() shouldBe setOf<PrivateKey>()
    }

    "delOutputOwnership" should "delete an output ownership" {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)

      scrubScript( store.getOutputOwnerships(db, W.ACCOUNT1) ).toSet() shouldBe setOf(T.ADDR1.address)

      store.delOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)

      scrubScript( store.getOutputOwnerships(db, W.ACCOUNT1) ).toSet() shouldBe setOf<OutputOwnership>()

    }

    "delOutputOwnership" should "delete multiple output ownerships" {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR2.address)
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR3.address)

      scrubScript( store.getOutputOwnerships(db, W.ACCOUNT1) ).toSet() shouldBe setOf(T.ADDR1.address, T.ADDR2.address, T.ADDR3.address)

      store.delOutputOwnership(db, W.ACCOUNT1, T.ADDR2.address)

      scrubScript( store.getOutputOwnerships(db, W.ACCOUNT1) ).toSet() shouldBe setOf(T.ADDR1.address, T.ADDR3.address)
    }

    "delOutputOwnership" should "delete all output ownerships" {
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR2.address)
      store.putOutputOwnership(db, W.ACCOUNT1, T.ADDR3.address)

      scrubScript( store.getOutputOwnerships(db, W.ACCOUNT1) ).toSet() shouldBe setOf(T.ADDR1.address, T.ADDR2.address, T.ADDR3.address)

      store.delOutputOwnership(db, W.ACCOUNT1, T.ADDR1.address)
      store.delOutputOwnership(db, W.ACCOUNT1, T.ADDR2.address)
      store.delOutputOwnership(db, W.ACCOUNT1, T.ADDR3.address)

      scrubScript( store.getOutputOwnerships(db, W.ACCOUNT1) ).toSet() shouldBe setOf<OutputOwnership>()
    }


    "putPrivateKey" should "throw an exception if the output ownership for the private key does not exist." {
      val thrown = shouldThrow<WalletException> {
        store.putPrivateKeys(db, T.ADDR1.address, listOf(T.ADDR1.privateKey))
      }
      thrown.code shouldBe ErrorCode.OwnershipNotFound
    }
  }
}
