package io.scalechain.blockchain.net

import java.io.File
import java.util.concurrent.TimeUnit

import io.scalechain.blockchain.chain.NewOutput
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.transaction.{CoinAmount, TransactionTestDataTrait}
import io.scalechain.wallet.{WalletBasedBlockSampleData, WalletTestTrait}
import org.scalatest.{Suite, Matchers, BeforeAndAfterEach, FlatSpec}


class BlockSigningHistorySpec extends FlatSpec with BeforeAndAfterEach with Matchers {

  this: Suite =>

  var history : BlockSigningHistory = null
  val HISTORY_KEEP_MILLISECONDS = 10

  override def beforeEach() {
    super.beforeEach()

    history = new BlockSigningHistory(HISTORY_KEEP_MILLISECONDS, TimeUnit.MILLISECONDS)
  }

  override def afterEach() {
    history = null

    super.afterEach()
  }

  val Hash1 = Hash("1")
  val Hash2 = Hash("2")
  val Hash3 = Hash("3")

  "didSignOn" should "return false if not signed yet" in {
    history.didSignOn(Hash1) shouldBe false
    history.didSignOn(Hash2) shouldBe false
    history.didSignOn(Hash3) shouldBe false
  }

  "didSignOn" should "return true if signed" in {
    history.signedOn(Hash1)
    history.signedOn(Hash3)
    history.didSignOn(Hash1) shouldBe true
    history.didSignOn(Hash2) shouldBe false
    history.didSignOn(Hash3) shouldBe true
  }


  "didSignOn" should "return false if signed but HISTORY_KEEP_SECONDS passed." in {
    history.signedOn(Hash1)
    history.signedOn(Hash3)
    Thread.sleep(HISTORY_KEEP_MILLISECONDS + 10)
    history.didSignOn(Hash1) shouldBe false
    history.didSignOn(Hash2) shouldBe false
    history.didSignOn(Hash3) shouldBe false
  }
}