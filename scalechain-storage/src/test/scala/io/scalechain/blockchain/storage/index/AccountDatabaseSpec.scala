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

  "putAccountAddress/getAccountAddress" should "successfully put/get data" in {

    db.getAccountAddress(accountName+"1") shouldBe None

    db.putAccountAddress(accountName+"1", accountAddress1.address)
    db.putAccountAddress(accountName+"2", accountAddress2.address)

    db.getAccountAddress(accountName+"1") shouldBe accountAddress1.address
    db.getAccountAddress(accountName+"2") shouldBe accountAddress2.address
  }

  "putAccountCount/getAccountCount" should "successfully put/get data" in {

    db.getAccountCount(accountName) shouldBe None

    db.putAccountAddress(accountName+"1", accountAddress1.address)
    db.putAccountAddress(accountName+"2", accountAddress2.address)
    db.putAccountCount(accountName, 2)


    db.getAccountCount(accountName) shouldBe 2
  }

  "putAccountCount" should "hit an assertion if account count is not number" in {

    db.putAccountAddress(accountName+"1", accountAddress1.address)
    db.putAccountAddress(accountName+"2", accountAddress2.address)

    intercept[AssertionError] {
      db.putAccountCount(accountName, "abc")
    }
  }

  "putAccountAddress" should "hit an assertion if address is not valid" in {

    intercept[AssertionError] {
      db.putAccountAddress(accountName+"1", "1dfdiekjfdkji30rf8df")
    }
  }

  "putAddressInfo/getAddressInfo" should "successfully put/get data" in {

    db.getAddressInfo(accountAddress1.address) shouldBe None

    val addressInfo = AddressInfo(
      account.account,
      accountAddress1.purpose,
      accountAddress1.publicKey
    )

    db.putAddressInfo(accountAddress1.address, addressInfo)

    db.getAddressInfo(accountAddress1.address) shouldBe addressInfo
  }

  "putAddressInfo" should "hit an assertion if address is not valid" in {

    val addressInfo = AddressInfo(
      account.account,
      accountAddress1.purpose,
      accountAddress1.publicKey
    )

    intercept[AssertionError] {
      db.putAddressInfo("12dhfusdhufhsd9eejlk", addressInfo)
    }
  }
}
