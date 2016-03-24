package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.proto.codec.{AccountCodec, CodecTestUtil}
import io.scalechain.blockchain.proto.walletparts.Account
import io.scalechain.blockchain.storage.Storage
import io.scalechain.io.HexFileLoader
import org.apache.commons.io.FileUtils
import org.scalatest._
import scodec.bits.BitVector

/**
  * Created by mijeong on 2016. 3. 22..
  */
class AccountDatabaseSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with CodecTestUtil {
  this: Suite =>

  Storage.initialize()

  var db : AccountDatabase = null
  override def beforeEach() {
    val testPath = new File("./target/unittests-AccountDatabaseSpec")
    FileUtils.deleteDirectory( testPath )
    db = new AccountDatabase( new RocksDatabase( testPath ) )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }

  val rawAccountBytes = HexFileLoader.load("data/unittest/codec/wallet-account.hex")
  val rawAccountBits = BitVector.view(rawAccountBytes)

  implicit val codec = AccountCodec.codec
  val account : Account = decodeFully(rawAccountBits)

  val accountName = account.account
  val accountAddress1 = account.addresses(0)
  val accountAddress2 = account.addresses(1)

  "putAddressInfo" should "successfully put address info" in {

  }

  "putAddressInfo" should "hit an assertion if address already exists" in {

  }

  "getAddressInfo" should "successfully get address info" in {

  }

  "getAddressInfo" should "return None if address does not exist" in {

  }

  "getAccount" should "successfully get account" in {

  }

  "getAccount" should "return None if address does not exist" in {

  }

  "getReceivedAddress" should "successfully get received address" in {

  }

  "getPrivateKeyLocator" should "successfully get private key locator" in {

  }

}
