package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.blockchain.transaction.ChainEnvironmentFactory
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

  if (ChainEnvironmentFactory.getActive().isEmpty)
    ChainEnvironmentFactory.create("testnet")

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
