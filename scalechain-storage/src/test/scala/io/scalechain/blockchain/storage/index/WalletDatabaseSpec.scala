package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.proto.{WalletInfo, Hash, Wallet}
import io.scalechain.blockchain.proto.codec.{WalletCodec, CodecTestUtil}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.Storage
import io.scalechain.io.HexFileLoader
import org.apache.commons.io.FileUtils
import org.scalatest._
import scodec.bits.BitVector

/**
  * Created by mijeong on 2016. 3. 15..
  */
class WalletDatabaseSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with CodecTestUtil {
  this: Suite =>

  Storage.initialize()

  var db : WalletDatabase = null

  override def beforeEach() {
    val testPath = new File("./target/unittests-WalletDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    db = new WalletDatabase( new RocksDatabase( testPath ) )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }

  val rawWalletBytes = HexFileLoader.loadWallet("data/unittest/codec/wallet-size289000.hex")
  val rawWalletBits = BitVector.view(rawWalletBytes)

  implicit val codec = WalletCodec.codec
  val wallet : Wallet = decodeFully(rawWalletBits)

  val walletHash = Hash(HashCalculator.walletHeaderHash(wallet.header))

  "putWalletInfo/getWalletInfo" should "successfully put/get data" in {
    db.getWalletInfo(walletHash) shouldBe None

    val walletInfo = WalletInfo(
      1,
      wallet.header
    )

    db.putWalletInfo(walletHash, walletInfo)
    db.getWalletInfo(walletHash) shouldBe Some(walletInfo)
  }
}
