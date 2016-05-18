package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.storage.index.RocksDatabase
import org.apache.commons.io.FileUtils
import org.scalatest._

class WalletStoreSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers
  with WalletStoreAccountTestTrait
  with WalletStoreOutPointTestTrait
  with WalletStoreTransactionHashTestTrait
  with WalletStoreWalletOutputTestTrait
  with WalletStoreWalletTransactionTestTrait {
  this: Suite =>

  var store : WalletStore = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-RocksDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    store = new WalletStore( testPath )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    store.close()
    store = null
  }
}
