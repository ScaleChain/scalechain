package io.scalechain.wallet

import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.{ErrorCode, WalletException}
import org.scalatest._

/**
  * Created by kangmo on 5/18/16.
  */
trait WalletStoreTransactionHashTestTrait extends FlatSpec with WalletStoreTestDataTrait with BeforeAndAfterEach with Matchers{
  var store : WalletStore
  implicit var db : KeyValueDatabase

  def prepareTxHashTest() {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT2, ADDR2.address)
    store.putOutputOwnership(ACCOUNT3, ADDR3.address)
  }

  "putTransactionHash" should "put a transaction hash per output ownership." in {
    prepareTxHashTest()

    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.getTransactionHashes(Some(ADDR1.address)).toSet shouldBe Set(TXHASH1)
  }

  "putTransactionHash" should "put many transaction hashes per output ownership." in {
    prepareTxHashTest()

    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.putTransactionHash(ADDR1.address, TXHASH2)
    store.putTransactionHash(ADDR1.address, TXHASH3)
    store.getTransactionHashes(Some(ADDR1.address)).toSet shouldBe Set(TXHASH1, TXHASH2, TXHASH3)
  }

  "delTransactionHash" should "do nothing if there was no hash for an ownership." in {
    prepareTxHashTest()

    store.delTransactionHash(ADDR1.address, TXHASH1)
  }

  "delTransactionHash" should "del a transaction hash when it was the only hash for an ownership." in {
    prepareTxHashTest()

    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.delTransactionHash(ADDR1.address, TXHASH1)
    store.getTransactionHashes(Some(ADDR1.address)).toSet shouldBe Set()
  }

  "delTransactionHash" should "del a transaction hash when it was NOT the only hash for an ownership." in {
    prepareTxHashTest()

    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.putTransactionHash(ADDR1.address, TXHASH2)
    store.putTransactionHash(ADDR1.address, TXHASH3)
    store.delTransactionHash(ADDR1.address, TXHASH2)
    store.getTransactionHashes(Some(ADDR1.address)).toSet shouldBe Set(TXHASH1, TXHASH3)
  }

  "getTransactionHashes(none)" should "get nothing if no transaction hash was put." in {
    prepareTxHashTest()

    store.getTransactionHashes(None).toSet shouldBe Set()
  }

  "getTransactionHashes(addr)" should "get nothing if no transaction hash was put." in {
    prepareTxHashTest()

    store.getTransactionHashes(Some(ADDR1.address)).toSet shouldBe Set()
  }

  "getTransactionHashes(none)" should "get all transaction hashes" in {
    prepareTxHashTest()

    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.putTransactionHash(ADDR2.address, TXHASH2)
    store.putTransactionHash(ADDR1.address, TXHASH3)

    store.getTransactionHashes(None).toSet shouldBe Set(TXHASH1, TXHASH2, TXHASH3)
  }

  "getTransactionHashes(addr)" should "get all transaction hashes only for the address" in {
    prepareTxHashTest()

    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.putTransactionHash(ADDR2.address, TXHASH2)
    store.putTransactionHash(ADDR1.address, TXHASH3)

    store.getTransactionHashes(Some(ADDR1.address)).toSet shouldBe Set(TXHASH1, TXHASH3)
  }


  "getTransactionHashes" should "get all transaction hashes for an output ownership." ignore {
    // No need to implement, already tested in the following test case.
    // "putTransactionHash" should "put many transaction hashes per output ownership."
  }

  "ownershipExists" should "return true if the ownership exists" in {
    prepareTxHashTest()

    store.ownershipExists(ADDR1.address) shouldBe true
  }

  "ownershipExists" should "return false if the ownership does not exist" in {
    prepareTxHashTest()

    store.ownershipExists(ADDR1.pubKeyScript) shouldBe false
  }

  "putTransactionHash" should "throw an exception if the output ownership does not exist." in {
    prepareTxHashTest()

    val thrown = the[WalletException] thrownBy {
      store.putTransactionHash(ADDR1.pubKeyScript, TXHASH1)
    }
    thrown.code shouldBe ErrorCode.OwnershipNotFound
  }

  "getTransactionHashes" should "should get hashes for an account even though the same hash belongs to multiple addresses" in {
    prepareTxHashTest()

    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.putTransactionHash(ADDR1.address, TXHASH2)
    store.putTransactionHash(ADDR1.address, TXHASH3)
    store.putTransactionHash(ADDR2.address, TXHASH2)
    store.putTransactionHash(ADDR2.address, TXHASH3)
    store.putTransactionHash(ADDR3.address, TXHASH1)
    store.putTransactionHash(ADDR3.address, TXHASH3)

    store.getTransactionHashes(Some(ADDR1.address)).toSet shouldBe Set(TXHASH1, TXHASH2, TXHASH3)
    store.getTransactionHashes(Some(ADDR2.address)).toSet shouldBe Set(TXHASH2, TXHASH3)
    store.getTransactionHashes(Some(ADDR3.address)).toSet shouldBe Set(TXHASH1, TXHASH3)

  }


}
