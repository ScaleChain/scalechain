package io.scalechain.wallet

import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.{ErrorCode, WalletException}
import org.scalatest._

/**
  * Created by kangmo on 5/18/16.
  */
trait WalletStoreAccountTestTrait : FlatSpec with WalletStoreTestDataTrait with Matchers{
  var store: WalletStore
  implicit var db : KeyValueDatabase

  "putOutputOwnership" should "be able to put an output ownership." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    scrubScript( store.getOutputOwnerships(Some(ACCOUNT1)) ).toSet shouldBe Set(ADDR1.address)
  }

  "putOutputOwnership" should "be able to put multiple output ownerships. (mixed)" in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT1, ADDR2.pubKeyScript)
    store.putOutputOwnership(ACCOUNT1, ADDR3.address)

    checkElementEquality(
      store.getOutputOwnerships(Some(ACCOUNT1)),
      Set(ADDR1.address, ADDR2.pubKeyScript, ADDR3.address)
    )
  }

  "putOutputOwnership" should "be able to put multiple output ownerships. (coin addresses only)" in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT1, ADDR2.address)
    store.putOutputOwnership(ACCOUNT1, ADDR3.address)
    checkElementEquality(
      store.getOutputOwnerships(Some(ACCOUNT1)),
      Set(ADDR1.address, ADDR2.address, ADDR3.address)
    )
  }

  "putOutputOwnership" should "be able to put multiple output ownerships. (public key scripts only)" in {
    checkElementEquality(
      store.getOutputOwnerships(Some(ACCOUNT1)),
      Set()
    )

    store.putOutputOwnership(ACCOUNT1, ADDR1.pubKeyScript)

    checkElementEquality(
      store.getOutputOwnerships(Some(ACCOUNT1)),
      Set(ADDR1.pubKeyScript)
    )

    store.putOutputOwnership(ACCOUNT1, ADDR2.pubKeyScript)

    checkElementEquality(
      store.getOutputOwnerships(Some(ACCOUNT1)),
      Set(ADDR1.pubKeyScript, ADDR2.pubKeyScript)
    )

    store.putOutputOwnership(ACCOUNT1, ADDR3.pubKeyScript)
    checkElementEquality(
      store.getOutputOwnerships(Some(ACCOUNT1)),
      Set(ADDR1.pubKeyScript, ADDR2.pubKeyScript, ADDR3.pubKeyScript)
    )
  }


  "getReceivingAddress" should "get nothing if no receiving address is attached to an account." in {
    store.getReceivingAddress(ACCOUNT1) shouldBe None
  }

  "getReceivingAddress" should "get a receiving address if a receiving address was attached to the account." in {
    store.putReceivingAddress(ACCOUNT1, ADDR1.address)
    store.getReceivingAddress(ACCOUNT1) shouldBe Some(ADDR1.address)
  }

  "putReceivingAddress" should "put a receiving address for an account" ignore {
    // No need to test this case, as it was tested in the following test.
    // "getReceivingAddress" should "get a receiving address if a receiving address was attached to the account." in
  }

  "putReceivingAddress" should "replace the previous receiving address if any." in {

    store.putReceivingAddress(ACCOUNT1, ADDR1.address)
    store.getReceivingAddress(ACCOUNT1) shouldBe Some(ADDR1.address)

    store.putReceivingAddress(ACCOUNT1, ADDR2.address)
    store.getReceivingAddress(ACCOUNT1) shouldBe Some(ADDR2.address)
  }

  "putReceivingAddress" should "put a public key script." in {
    store.putReceivingAddress(ACCOUNT1, ADDR1.pubKeyScript)
    store.getReceivingAddress(ACCOUNT1).map(scrubScript(_)) shouldBe Some(ADDR1.pubKeyScript)

    store.putReceivingAddress(ACCOUNT1, ADDR2.pubKeyScript)
    store.getReceivingAddress(ACCOUNT1).map(scrubScript(_)) shouldBe Some(ADDR2.pubKeyScript)
  }

  "getAccount" should "get nothing if the given output ownership was put for an account." in {
    store.getAccount(ADDR1.address) shouldBe None
  }

  "getAccount" should "get the account that has the given output ownership. (when output ownership count is 1 )." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.getAccount(ADDR1.address) shouldBe Some(ACCOUNT1)
  }

  "getAccount" should "get the account that has the given output ownership. (when output ownership count is 2 )." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT2, ADDR2.address)

    store.getAccount(ADDR1.address) shouldBe Some(ACCOUNT1)
    store.getAccount(ADDR2.address) shouldBe Some(ACCOUNT2)
  }

  "getOutputOwnerships" should "get all output ownerships if None is passed for the parameter." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT2, ADDR2.address)
    store.putOutputOwnership(ACCOUNT3, ADDR3.address)

    checkElementEquality(
      store.getOutputOwnerships(None),
      Set(ADDR1.address, ADDR2.address, ADDR3.address)
    )
  }

  "getOutputOwnerships" should "get output ownerships that an account has if Some(account) is passed for the parameter." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT2, ADDR2.address)
    checkElementEquality(
      store.getOutputOwnerships(Some(ACCOUNT1)),
      Set(ADDR1.address)
    )
    checkElementEquality(
      store.getOutputOwnerships(Some(ACCOUNT2)),
      Set(ADDR2.address)
    )
  }

  "getOutputOwnerships" should "NOT get output ownerships of another account." ignore {
    // No need to test this case, as it was tested in the following test.
    // "getOutputOwnerships" should "get output ownerships that an account has if Some(account) is passed for the parameter."
  }


  "getPrivateKeys(None)" should "get nothing if no private key was put." in {
    store.getPrivateKeys(None) shouldBe List()
  }

  "getPrivateKeys(None)" should "get all private keys put." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT2, ADDR2.address)
    store.putOutputOwnership(ACCOUNT3, ADDR3.address)
    store.putPrivateKeys(ADDR1.address, List(ADDR1.privateKey))
    store.putPrivateKeys(ADDR2.address, List(ADDR2.privateKey))
    store.putPrivateKeys(ADDR3.address, List(ADDR3.privateKey))
    store.getPrivateKeys(None).toSet shouldBe Set( ADDR1.privateKey, ADDR2.privateKey, ADDR3.privateKey)
  }

  "getPrivateKeys(addr)" should "get nothing if no private key was put for an address" in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.getPrivateKeys(Some(ADDR1.address)).toSet shouldBe Set()
  }

  "putPrivateKeys(addr)" should "put a private key for an output ownership." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putPrivateKeys(ADDR1.address, List(ADDR1.privateKey))
    store.getPrivateKeys(Some(ADDR1.address)).toSet shouldBe Set(ADDR1.privateKey)
  }

  "putPrivateKey" should "overwrite the previous private key for an output ownership." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putPrivateKeys(ADDR1.address, List(ADDR1.privateKey))
    store.putPrivateKeys(ADDR1.address, List(ADDR2.privateKey))
    store.getPrivateKeys(Some(ADDR1.address)).toSet shouldBe Set(ADDR2.privateKey)
  }

  "getPrivateKeys(pubKeyScript)" should "get nothing if no private key was put for a public key script" in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.pubKeyScript)
    store.getPrivateKeys(Some(ADDR1.pubKeyScript)).toSet shouldBe Set()
  }

  "getPrivateKeys(pubKeyScript)" should "get a private key for a public key script." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.pubKeyScript)
    store.putPrivateKeys(ADDR1.pubKeyScript, List(ADDR1.privateKey))
    store.getPrivateKeys(Some(ADDR1.pubKeyScript)).toSet shouldBe Set(ADDR1.privateKey)
  }

  "getPrivateKeys(pubKeyScript)" should "get a nothing even with a matching address." in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.pubKeyScript)
    store.putPrivateKeys(ADDR1.pubKeyScript, List(ADDR1.privateKey))
    // Try to get with address instead of pubKeyScript
    store.getPrivateKeys(Some(ADDR1.address)).toSet shouldBe Set()
  }

  "delOutputOwnership" should "delete an output ownership" in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)

    scrubScript( store.getOutputOwnerships(Some(ACCOUNT1)) ).toSet shouldBe Set(ADDR1.address)

    store.delOutputOwnership(ACCOUNT1, ADDR1.address)

    scrubScript( store.getOutputOwnerships(Some(ACCOUNT1)) ).toSet shouldBe Set()

  }

  "delOutputOwnership" should "delete multiple output ownerships" in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT1, ADDR2.address)
    store.putOutputOwnership(ACCOUNT1, ADDR3.address)

    scrubScript( store.getOutputOwnerships(Some(ACCOUNT1)) ).toSet shouldBe Set(ADDR1.address, ADDR2.address, ADDR3.address)

    store.delOutputOwnership(ACCOUNT1, ADDR2.address)

    scrubScript( store.getOutputOwnerships(Some(ACCOUNT1)) ).toSet shouldBe Set(ADDR1.address, ADDR3.address)
  }

  "delOutputOwnership" should "delete all output ownerships" in {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT1, ADDR2.address)
    store.putOutputOwnership(ACCOUNT1, ADDR3.address)

    scrubScript( store.getOutputOwnerships(Some(ACCOUNT1)) ).toSet shouldBe Set(ADDR1.address, ADDR2.address, ADDR3.address)

    store.delOutputOwnership(ACCOUNT1, ADDR1.address)
    store.delOutputOwnership(ACCOUNT1, ADDR2.address)
    store.delOutputOwnership(ACCOUNT1, ADDR3.address)

    scrubScript( store.getOutputOwnerships(Some(ACCOUNT1)) ).toSet shouldBe Set()
  }


  "putPrivateKey" should "throw an exception if the output ownership for the private key does not exist." in {
    val thrown = the<WalletException> thrownBy {
      store.putPrivateKeys(ADDR1.address, List(ADDR1.privateKey))
    }
    thrown.code shouldBe ErrorCode.OwnershipNotFound
  }
}
