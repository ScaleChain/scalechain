package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.{ErrorCode, WalletException}
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 5/18/16.
  */
trait WalletStoreOutPointTestTrait extends FlatSpec with WalletStoreTestDataTrait with BeforeAndAfterEach with ShouldMatchers{
  var store : WalletStore
  implicit var db : KeyValueDatabase

  def prepareOutPointTest() {
    store.putOutputOwnership(ACCOUNT1, ADDR1.address)
    store.putOutputOwnership(ACCOUNT2, ADDR2.address)
    store.putOutputOwnership(ACCOUNT3, ADDR3.address)
  }

  "putTransactionOutPoint" should "put an out point per output ownership." in {
    prepareOutPointTest()

    assert(store.ownershipExists(ADDR1.address))
    assert(store.ownershipExists(ADDR2.address))
    assert(store.ownershipExists(ADDR3.address))

    store.putTransactionOutPoint(ADDR1.address, OUTPOINT1)
    store.getTransactionOutPoints(Some(ADDR1.address)).toSet shouldBe Set(OUTPOINT1)
  }

  "putTransactionOutPoint" should "put many out points per output ownership." in {
    prepareOutPointTest()

    store.putTransactionOutPoint(ADDR1.address, OUTPOINT1)
    store.putTransactionOutPoint(ADDR1.address, OUTPOINT2)
    store.putTransactionOutPoint(ADDR1.address, OUTPOINT3)
    store.getTransactionOutPoints(Some(ADDR1.address)).toSet shouldBe Set(OUTPOINT1, OUTPOINT2, OUTPOINT3)
  }

  "delTransactionOutPoint" should "do nothing if the out point was not found for an ownership." in {
    prepareOutPointTest()

    store.delTransactionOutPoint(ADDR1.address, OUTPOINT1)
  }

  "delTransactionOutPoint" should "del an out point when it was the only out point for an ownership." in {
    prepareOutPointTest()

    store.putTransactionOutPoint(ADDR1.address, OUTPOINT1)
    store.delTransactionOutPoint(ADDR1.address, OUTPOINT1)
    store.getTransactionOutPoints(Some(ADDR1.address)).toSet shouldBe Set()
  }

  "getTransactionOutPoints" should "del an out point when it was NOT the only out point for an ownership." in {
    prepareOutPointTest()

    store.putTransactionOutPoint(ADDR1.address, OUTPOINT1)
    store.putTransactionOutPoint(ADDR1.address, OUTPOINT2)
    store.putTransactionOutPoint(ADDR1.address, OUTPOINT3)
    store.delTransactionOutPoint(ADDR1.address, OUTPOINT1)
    store.getTransactionOutPoints(Some(ADDR1.address)).toSet shouldBe Set(OUTPOINT2, OUTPOINT3)
  }

  "getTransactionOutPoints" should "get all out points for all output ownerships if None is passed for the parameter." in {
    prepareOutPointTest()

    store.putTransactionOutPoint(ADDR1.address, OUTPOINT1)
    store.putTransactionOutPoint(ADDR2.address, OUTPOINT2)
    store.putTransactionOutPoint(ADDR3.address, OUTPOINT3)

    store.getTransactionOutPoints(None).toSet shouldBe Set(OUTPOINT1, OUTPOINT2, OUTPOINT3)
  }


  "putTransactionOutPoint" should "throw an exception if the output ownership does not exist." in {
    prepareOutPointTest()

    val thrown = the[WalletException] thrownBy {
      store.putTransactionOutPoint(ADDR1.pubKeyScript, OUTPOINT1)
    }
    thrown.code shouldBe ErrorCode.OwnershipNotFound
  }
}
