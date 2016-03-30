package io.scalechain.blockchain.storage.index

import java.io.File
import java.util._

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{AccountCodec, CodecTestUtil}
import io.scalechain.blockchain.proto.walletparts.Account
import io.scalechain.blockchain.storage.Storage
import io.scalechain.io.HexFileLoader
import io.scalechain.util.HexUtil._
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
    db = new AccountDatabase( new RocksDatabaseWithCF(testPath, new ArrayList[String]()))

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }

  val rawAccountBytes = HexFileLoader.loadWallet("data/unittest/codec/wallet-account.hex")
  val rawAccountBits = BitVector.view(rawAccountBytes)

  implicit val codec = AccountCodec.codec
  val account : Account = decodeFully(rawAccountBits)

  val accountName = account.account
  val accountAddress1 = account.addresses(0)
  val accountAddress2 = account.addresses(1)

  "putAddressInfo/getAddressInfo" should "successfully put/get address info" in {

    val addressKey = AddressKey(
      address = accountAddress1.address
    )

    val addressInfo = AddressInfo(
      account = accountName,
      purpose = accountAddress1.purpose,
      publicKey = accountAddress1.publicKey,
      privateKeyLocator = None
    )

    db.putAddressInfo(accountName, addressKey, addressInfo)
    db.getAddressInfo(accountName, addressKey) shouldBe Some(addressInfo)
  }

  "putAddressInfo" should "hit an assertion if there is an attempt to change fixed values" in {

    val addressKey = AddressKey(
      address = accountAddress1.address
    )

    val addressInfo1 = AddressInfo(
      account = accountName,
      purpose = accountAddress1.purpose,
      publicKey = accountAddress1.publicKey,
      privateKeyLocator = None
    )

    db.putAddressInfo(accountName, addressKey, addressInfo1)

    val addressInfo2 = AddressInfo(
      account = "changedAccount",
      purpose = accountAddress1.purpose,
      publicKey = accountAddress1.publicKey,
      privateKeyLocator = None
    )

    intercept[AssertionError] {
      db.putAddressInfo(accountName, addressKey, addressInfo2)
    }

    val changedPublicKey = bytes(
      """
      7b 1e ab e0 20 9b 1f e7  94 12 45 75 ef 80 70 57
      c7 7a da 21 38 ae 4f a8  d6 c4 de 03 98 a1 4f 3f
      3f
      """)

    val addressInfo3 = AddressInfo(
      account = accountName,
      purpose = accountAddress1.purpose,
      publicKey = changedPublicKey,
      privateKeyLocator = None
    )

    intercept[AssertionError] {
      db.putAddressInfo(accountName, addressKey, addressInfo3)
    }
  }

  "getAddressInfo" should "return None if address does not exist" in {

    val addressKey1 = AddressKey(
      address = accountAddress1.address
    )

    val addressKey2 = AddressKey(
      address = accountAddress2.address
    )

    val addressInfo1 = AddressInfo(
      account = accountName,
      purpose = accountAddress1.purpose,
      publicKey = accountAddress1.publicKey,
      privateKeyLocator = None
    )

    db.putAddressInfo(accountName, addressKey1, addressInfo1)

    db.getAddressInfo(accountName, addressKey1) shouldBe Some(addressInfo1)
    db.getAddressInfo(accountName, addressKey2) shouldBe None
  }

  "getAccount" should "successfully get account" in {

    val columnFamilyName = "addressaccount"

    val addressKey = AddressKey(
      address = accountAddress1.address
    )

    val accountInfo = AccountInfo(
      account = accountName
    )

    db.putAccount(columnFamilyName, addressKey, accountInfo)
    db.getAccount(columnFamilyName, addressKey) shouldBe Some(accountName)
  }

  "getAccount" should "return None if address does not exist" in {

    val columnFamilyName = "addressaccount"

    val addressKey1 = AddressKey(
      address = accountAddress1.address
    )

    val addressKey2 = AddressKey(
      address = accountAddress2.address
    )

    val accountInfo = AccountInfo(
      account = accountName
    )

    db.putAccount(columnFamilyName, addressKey1, accountInfo)
    db.getAccount(columnFamilyName, addressKey1) shouldBe Some(accountName)
    db.getAccount(columnFamilyName, addressKey2) shouldBe None
  }

  "getReceivedAddress" should "successfully get received address" in {

    val addressKey1 = AddressKey(
      address = accountAddress1.address
    )

    val addressInfo1 = AddressInfo(
      account = accountName,
      purpose = accountAddress1.purpose,
      publicKey = accountAddress1.publicKey,
      privateKeyLocator = None
    )

    val addressKey2 = AddressKey(
      address = accountAddress2.address
    )

    val addressInfo2 = AddressInfo(
      account = accountName,
      purpose = accountAddress2.purpose,
      publicKey = accountAddress2.publicKey,
      privateKeyLocator = None
    )

    db.putAddressInfo(accountName, addressKey1, addressInfo1)
    db.putAddressInfo(accountName, addressKey2, addressInfo2)
    db.getReceiveAddress(accountName) shouldBe Some(addressInfo2)
  }

  "getPrivateKeyLocator" should "successfully get private key locator" in {

    val addressKey = AddressKey(
      address = accountAddress1.address
    )

    val addressInfo = AddressInfo(
      account = accountName,
      purpose = accountAddress1.purpose,
      publicKey = accountAddress1.publicKey,
      privateKeyLocator = Some(FileRecordLocator(
        fileIndex = 1,
        RecordLocator(
          offset = 10,
          size = 200
        )))
    )

    db.putAddressInfo(accountName, addressKey, addressInfo)
    db.getPrivateKeyLocator(accountName, addressKey) shouldBe addressInfo.privateKeyLocator
  }

}
