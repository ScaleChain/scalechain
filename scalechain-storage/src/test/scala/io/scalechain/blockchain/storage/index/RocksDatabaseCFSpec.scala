package io.scalechain.blockchain.storage.index

import java.io.File

import io.scalechain.blockchain.storage.Storage
import org.apache.commons.io.FileUtils
import org.rocksdb.{RocksDBException}
import org.scalatest._

/**
  * Created by mijeong on 2016. 3. 23..
  */
class RocksDatabaseCFSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var db : RocksDatabase = null

  override def beforeEach() {

    val testPath = new File("./target/unittests-RocksDatabaseCFSpec")
    FileUtils.deleteDirectory( testPath )
    db = new RocksDatabase( testPath )

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    db.close()
  }

  def L(arrayOption : Option[Array[Byte]]) =
    arrayOption.map(_.toList)

  "openWithColumnFamily" should "return an instance of RocksDB" in {
    db.openWithColumnFamily

    db shouldBe a[RocksDatabase]
  }

  "createColumnFamily" should "add new column family to existing column family list" in {
    db.openWithColumnFamily

    // list has default column family
    db.listColumnFamilies().size() shouldBe 1

    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    val address2 = "1N51L9K3G6TjQFpf4RChxho5qXsUMvbYhf"

    db.createColumnFamily(address1)
    db.createColumnFamily(address2)

    val columnFamilyNames = db.listColumnFamilies()

    columnFamilyNames.size() shouldBe 3
    new String(columnFamilyNames.get(1)) shouldBe address1
    new String(columnFamilyNames.get(2)) shouldBe address2
  }

  "createColumnFamily" should "hit an assertion if new column family is already exist" in {
    db.openWithColumnFamily

    val address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"

    intercept[RocksDBException] {
      db.createColumnFamily(address)
      db.createColumnFamily(address)
    }
  }

  "createColumnFamily" should "add more than 1,000,000 column family" in {
    db.openWithColumnFamily

    val max = 1000000
    val address = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"

    for( i <- 1 to max){
      db.createColumnFamily(address + i)
    }

    val columnFamilyNames = db.listColumnFamilies()
    columnFamilyNames.size() shouldBe max+1
  }

  "dropColumnFamily" should "drop the column family" in {
    db.openWithColumnFamily

    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    val address2 = "1N51L9K3G6TjQFpf4RChxho5qXsUMvbYhf"
    val address3 = "1N51L9K3G6TjQFpf4RChxho5qXsUMvbYh3"

    db.createColumnFamily(address1)
    db.createColumnFamily(address2)
    db.createColumnFamily(address3)

    val beforeColumnFamilyNames = db.listColumnFamilies()
    beforeColumnFamilyNames.size() shouldBe 4

    val index = db.getColumnFamilyIndex(address1)
    db.dropColumnFamily(index)

    val afterColumnFamilyNames = db.listColumnFamilies()
    afterColumnFamilyNames.size() shouldBe 3
  }

  "putObject(column family, key, value)/getObject(column family, key)" should "store data and get data" in {
    db.openWithColumnFamily

    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    db.createColumnFamily(address1)

    val columnFamilyHandle = db.getColumnFamilyHandle(address1)
    val key = "test account key"
    val value = "test account value"

    db.put(columnFamilyHandle, key.getBytes(), value.getBytes())
    L(db.get(columnFamilyHandle, key.getBytes())) shouldBe L(Some(value.getBytes()))
  }

  "putObject(column family, key, value)" should "overwrite an existing data" in {
    db.openWithColumnFamily

    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    db.createColumnFamily(address1)

    val columnFamilyHandle = db.getColumnFamilyHandle(address1)
    val key = "test account key"
    val value1 = "test account value1"
    val value2 = "test account value2"

    db.put(columnFamilyHandle, key.getBytes(), value1.getBytes())
    L(db.get(columnFamilyHandle, key.getBytes())) shouldBe L(Some(value1.getBytes()))

    db.put(columnFamilyHandle, key.getBytes(), value2.getBytes())
    L(db.get(columnFamilyHandle, key.getBytes())) shouldBe L(Some(value2.getBytes()))
  }

  "getObject(column family, key)" should "return None if the key is not exist" in {
    db.openWithColumnFamily

    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    db.createColumnFamily(address1)

    val columnFamilyHandle = db.getColumnFamilyHandle(address1)
    val key1 = "test account key1"
    val key2 = "test account key2"
    val value = "test account value"

    db.put(columnFamilyHandle, key1.getBytes(), value.getBytes())
    db.get(columnFamilyHandle, key2.getBytes()) shouldBe None
  }

  "getKeys(column family)" should "return all keys of column family" in {
    db.openWithColumnFamily

    val address1 = "12n9PRqdYQp9DPGV9yghbJHsoMZcd5xcum"
    db.createColumnFamily(address1)

    val columnFamilyHandle = db.getColumnFamilyHandle(address1)
    val key1 = "test account key1"
    val key2 = "test account key2"
    val value1 = "test account value1"
    val value2 = "test account value2"

    db.put(columnFamilyHandle, key1.getBytes(), value1.getBytes())
    db.put(columnFamilyHandle, key2.getBytes(), value2.getBytes())

    val keys = db.getKeys(columnFamilyHandle)
    keys.size() shouldBe 2
    new String(keys.get(0)) shouldBe key1
    new String(keys.get(1)) shouldBe key2
  }

}
