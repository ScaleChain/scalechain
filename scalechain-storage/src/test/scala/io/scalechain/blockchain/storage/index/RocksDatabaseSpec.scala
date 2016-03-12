package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.proto.{RecordLocator, FileNumber}
import io.scalechain.blockchain.proto.codec.{RecordLocatorCodec, FileNumberCodec}
import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class RocksDatabaseSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var db : RocksDatabase = null


  override def beforeEach() {

    val testPath = "./target/unittests-RocksDatabaseSpec"
    FileUtils.deleteDirectory(new File(testPath))
    db =  new RocksDatabase(testPath)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }

  /** Convert a string to a byte array.
    *
    * @param value The string to convert to a byte array.
    * @return The converted byte array.
    */
  def B(value : String) = value.getBytes

  "getObject(rawKey)" should "return a value which was put" in {
    val C = FileNumberCodec

    db.getObject(B("k1"))(C) shouldBe None
    db.putObject(B("k1"), FileNumber(1))(C)
    db.getObject(B("k1"))(C) shouldBe Some(FileNumber(1))
  }

  "putObject(rawKey)" should "overwrite an existing value" in {
    val C = FileNumberCodec

    db.getObject(B("k1"))(C) shouldBe None
    db.putObject(B("k1"), FileNumber(1))(C)
    db.getObject(B("k1"))(C) shouldBe Some(FileNumber(1))

    db.putObject(B("k1"), FileNumber(2))(C)
    db.getObject(B("k1"))(C) shouldBe Some(FileNumber(2))
  }

  "putObject(rawKey)" should "store multiple keys" in {
    val C = FileNumberCodec

    db.getObject(B("k1"))(C) shouldBe None
    db.getObject(B("k2"))(C) shouldBe None
    db.getObject(B("k3"))(C) shouldBe None

    db.putObject(B("k1"), FileNumber(1))(C)
    db.putObject(B("k2"), FileNumber(2))(C)
    db.putObject(B("k3"), FileNumber(3))(C)

    db.getObject(B("k1"))(C) shouldBe Some(FileNumber(1))
    db.getObject(B("k2"))(C) shouldBe Some(FileNumber(2))
    db.getObject(B("k3"))(C) shouldBe Some(FileNumber(3))
  }

  val PREFIX1 : Byte = '1'
  val PREFIX2 : Byte = '2'

  "getObject(objectKey)" should "return a value which was put" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe None
    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1,2))(F, R)
    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe Some(RecordLocator(1,2))
  }

  "putObject(objectKey)" should "overwrite an existing value" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe None

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1,2))(F, R)
    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe Some(RecordLocator(1,2))

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1000,2000))(F, R)
    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe Some(RecordLocator(1000,2000))
  }

  "putObject(objectKey)" should "store multiple keys" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe None
    db.getObject(PREFIX1, FileNumber(2))(F, R) shouldBe None
    db.getObject(PREFIX1, FileNumber(3))(F, R) shouldBe None

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1,2))(F, R)
    db.putObject(PREFIX1, FileNumber(2), RecordLocator(3,2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(5,2))(F, R)

    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe Some(RecordLocator(1,2))
    db.getObject(PREFIX1, FileNumber(2))(F, R) shouldBe Some(RecordLocator(3,2))
    db.getObject(PREFIX1, FileNumber(3))(F, R) shouldBe Some(RecordLocator(5,2))
  }

  "putObject(objectKey)" should "store multiple keys with multiple prefixes" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe None
    db.getObject(PREFIX2, FileNumber(2))(F, R) shouldBe None
    db.getObject(PREFIX1, FileNumber(3))(F, R) shouldBe None

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1,2))(F, R)
    db.putObject(PREFIX2, FileNumber(2), RecordLocator(3,2))(F, R)
    db.putObject(PREFIX1, FileNumber(3), RecordLocator(5,2))(F, R)

    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe Some(RecordLocator(1,2))
    db.getObject(PREFIX2, FileNumber(1))(F, R) shouldBe None
    db.getObject(PREFIX1, FileNumber(2))(F, R) shouldBe None
    db.getObject(PREFIX2, FileNumber(2))(F, R) shouldBe Some(RecordLocator(3,2))
    db.getObject(PREFIX1, FileNumber(3))(F, R) shouldBe Some(RecordLocator(5,2))
    db.getObject(PREFIX2, FileNumber(3))(F, R) shouldBe None

    db.delObject(PREFIX1, FileNumber(1))(F)
    db.delObject(PREFIX2, FileNumber(2))(F)
    db.delObject(PREFIX1, FileNumber(3))(F)

    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe None
    db.getObject(PREFIX2, FileNumber(2))(F, R) shouldBe None
    db.getObject(PREFIX1, FileNumber(3))(F, R) shouldBe None
  }


  "delObject(objectKey)" should "delete a key" in {
    val F = FileNumberCodec
    val R = RecordLocatorCodec

    db.putObject(PREFIX1, FileNumber(1), RecordLocator(1,2))(F, R)
    db.delObject(PREFIX1, FileNumber(1))(F)

    db.getObject(PREFIX1, FileNumber(1))(F, R) shouldBe None
  }

  "delObject(objectKey)" should "do nothing if we try to remove a non-existent key" in {
    val F = FileNumberCodec

    db.delObject(PREFIX1, FileNumber(1))(F)
  }


  /** Convert an option of a byte array to an option of a byte list.
    * Because Scala uses referential equality check on arrays,
    * we need to convert arrays to lists.
    *
    * For lists, Scala checks each items in the list for the equality check.
    *
    * @param arrayOption The option of an array to convert.
    */
  def L(arrayOption : Option[Array[Byte]]) =
    arrayOption.map(_.toList)


  "get" should "return a value which was put" in {
    db.get(B("k1")) shouldBe None
    db.put(B("k1"), B("v1"))
    L(db.get(B("k1"))) shouldBe L(Some(B("v1")))
  }

  "put" should "overwrite an existing value" in {
    db.get(B("k1")) shouldBe None
    db.put(B("k1"), B("v1"))
    L(db.get(B("k1"))) shouldBe L(Some(B("v1")))

    db.put(B("k1"), B("v1004"))
    L(db.get(B("k1"))) shouldBe L(Some(B("v1004")))
  }

  "put" should "store multiple keys" in {
    db.get(B("k1")) shouldBe None
    db.get(B("k2")) shouldBe None
    db.get(B("k3")) shouldBe None

    db.put(B("k1"), B("v1"))
    db.put(B("k2"), B("v2"))
    db.put(B("k3"), B("v3"))

    L(db.get(B("k1"))) shouldBe L(Some(B("v1")))
    L(db.get(B("k2"))) shouldBe L(Some(B("v2")))
    L(db.get(B("k3"))) shouldBe L(Some(B("v3")))
  }

  "del" should "delete a key" in {
    db.get(B("k1")) shouldBe None
    db.put(B("k1"), B("v1"))
    L(db.get(B("k1"))) shouldBe L(Some(B("v1")))
    db.del(B("k1"))
    db.get(B("k1")) shouldBe None
  }

  "del" should "do nothing if we try to remove a non-existent key" in {
    db.del(B("k1"))
  }
}
