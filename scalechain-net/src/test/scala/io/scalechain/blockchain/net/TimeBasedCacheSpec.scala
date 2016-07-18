package io.scalechain.blockchain.net

import java.io.File
import java.util.concurrent.TimeUnit

import io.scalechain.blockchain.chain.NewOutput
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.transaction.{CoinAmount, TransactionTestDataTrait}
import io.scalechain.wallet.{WalletBasedBlockSampleData, WalletTestTrait}
import org.scalatest.{Suite, Matchers, BeforeAndAfterEach, FlatSpec}
import HashSupported._

class TimeBasedCacheSpec extends FlatSpec with WalletTestTrait with BeforeAndAfterEach with TransactionTestDataTrait with Matchers {

  this: Suite =>

  Storage.initialize()

  var data : WalletBasedBlockSampleData = null
  val testPath = new File("./target/unittests-IncompleteBlockCacheSpec-storage/")

  var cache : TimeBasedCache[Block] = null

  val CACHE_KEEP_MILLISECONDS = 10

  override def beforeEach() {
    super.beforeEach()

    data = new WalletBasedBlockSampleData(wallet)
    cache = new TimeBasedCache[Block](CACHE_KEEP_MILLISECONDS, TimeUnit.MILLISECONDS)
  }

  override def afterEach() {
    super.afterEach()

    data = null
    cache = null
  }

  "getBlock" should "return None if no block/transaction was added" in {
    val blockHash = data.Block.BLK02.header.hash
    cache.get(blockHash) shouldBe None
  }

  "getBlock" should "return an IncompleteBlock if a signing transaction was added" in {
    val blockHash = data.Block.BLK02.header.hash

    cache.put(blockHash, data.Block.BLK02)

    cache.get(blockHash) shouldBe Some(data.Block.BLK02)
  }

  "getBlock" should "return None if a signing transaction was added but expired" in {
    val blockHash = data.Block.BLK02.header.hash

    cache.put(blockHash, data.Block.BLK02)

    Thread.sleep(CACHE_KEEP_MILLISECONDS + 10)

    cache.get(blockHash) shouldBe None
  }


}