package io.scalechain.wallet

import io.scalechain.blockchain.{ErrorCode, WalletException}
import org.scalatest._

/**
  * Created by kangmo on 5/18/16.
  */
trait WalletStoreTransactionHashTestTrait extends FlatSpec with WalletStoreTestDataTrait with BeforeAndAfterEach with ShouldMatchers{
  var store : WalletStore

  override def beforeEach() {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT2, ADDR2.pubKeyScript)
    store.putOutputOwnership(ACCOUNT3, ADDR3.address)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
  }

  "putTransactionHash" should "put a transaction hash per output ownership." in {
    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.getTransactionHashes(ADDR1.address) shouldBe List(TXHASH1)
  }

  "putTransactionHash" should "put many transaction hashes per output ownership." in {
    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.putTransactionHash(ADDR1.address, TXHASH2)
    store.putTransactionHash(ADDR1.address, TXHASH3)
    store.getTransactionHashes(ADDR1.address) shouldBe List(TXHASH1, TXHASH2, TXHASH3)
  }

  "delTransactionHash" should "do nothing if there was no hash for an ownership." in {
    store.delTransactionHash(ADDR1.address, TXHASH1)
  }

  "delTransactionHash" should "del a transaction hash when it was the only hash for an ownership." in {
    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.delTransactionHash(ADDR1.address, TXHASH1)
    store.getTransactionHashes(ADDR1.address) shouldBe List()
  }

  "delTransactionHash" should "del a transaction hash when it was NOT the only hash for an ownership." in {
    store.putTransactionHash(ADDR1.address, TXHASH1)
    store.putTransactionHash(ADDR1.address, TXHASH2)
    store.putTransactionHash(ADDR1.address, TXHASH3)
    store.delTransactionHash(ADDR1.address, TXHASH2)
    store.getTransactionHashes(ADDR1.address) shouldBe List(TXHASH1, TXHASH3)
  }

  "getTransactionHashes" should "get nothing if no transaction hash was put." in {
    store.getTransactionHashes(ADDR1.address) shouldBe List()
  }

  "getTransactionHashes" should "get all transaction hashes for an output ownership." ignore {
    // No need to implement, already tested in the following test case.
    // "putTransactionHash" should "put many transaction hashes per output ownership."
  }

  "putTransactionHash" should "throw an exception if the output ownership does not exist." in {
    val thrown = the[WalletException] thrownBy {
      store.putTransactionHash(ADDR1.address, TXHASH1)
    }
    thrown.code shouldBe ErrorCode.AddressNotFound
  }
}
