package io.scalechain.blockchain.storage.record

import java.io.File
import java.nio.ByteBuffer

import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.record.BlockAccessFile
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockAccessFileSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  Storage.initialize()

  var file : BlockAccessFile = null
  val MAX_SIZE = 64

  val underlyingFile = new File("./target/unittests-BlockAccessFileSpec")
  def openFile() = {
    new BlockAccessFile(underlyingFile, MAX_SIZE)
  }

  override def beforeEach() {
    if (underlyingFile.exists())
      underlyingFile.delete()

    file = openFile()

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    file.close()
  }

  /** Convert a string to a byte buffer.
    *
    * @param value The value to convert.
    * @return A byte buffer we got from the string value.
    */
  def B(value : String) : ByteBuffer = {
    ByteBuffer.wrap( value.getBytes )
  }

  /** Make sure that the offset and size attributes are set with the values we expect.
    *
    * @param offset The offset where we are reading/writing the file.
    * @param size The size of the file.
    */
  def expect(offset : Long, size : Long): Unit = {
    file.offset() shouldBe offset
    file.size() shouldBe size
  }

  "read" should "read values written by append" in {
    expect(offset=0, size=0)

    file.append(B("abcd"))

    expect(offset=4, size=4)

    file.append(B("12"))

    expect(offset=6, size=6)

    file.read(0, 4) shouldBe B("abcd")
    file.read(4, 2) shouldBe B("12")
  }

  "append" should "append data at the end of the file even after reading data at the mid of the file " in {
    expect(offset=0, size=0)

    file.append(B("abcd"))

    expect(offset=4, size=4)

    file.append(B("12"))

    expect(offset=6, size=6)

    file.read(0, 4) shouldBe B("abcd")

    file.append(B("XY"))

    expect(offset=8, size=8)
    file.read(4, 2) shouldBe B("12")
    file.read(6, 2) shouldBe B("XY")
  }

  "read" should "read values even after closing and re-opening the file." in {
    file.append(B("abcd"))
    file.append(B("12"))

    file.close()
    file = openFile()

    expect(offset=0, size=6)

    file.read(4, 2) shouldBe B("12")
    file.read(0, 4) shouldBe B("abcd")
  }

  "append" should "append values even after closing and re-opening the file." in {
    file.append(B("abcd"))
    file.append(B("12"))

    file.close()
    file = openFile()

    expect(offset=0, size=6)

    file.append(B("XY"))

    file.read(0, 4) shouldBe B("abcd")
    file.read(6, 2) shouldBe B("XY")
    file.read(4, 2) shouldBe B("12")
  }

  "moveTo" should "be able to move at the beginning of the file" in {
    file.append(B("abcd"))
    file.append(B("12"))

    expect(offset=6, size=6)

    file.moveTo(0)
    expect(offset=0, size=6)
  }

  "moveTo" should "be able to move at the mid of the file" in {
    file.append(B("abcd"))
    file.append(B("12"))

    expect(offset=6, size=6)
    file.read(0, 4)

    file.moveTo(2)
    expect(offset=2, size=6)
  }

  "moveTo" should "be able to move at the end of the file" in {
    file.append(B("abcd"))
    file.append(B("12"))

    expect(offset=6, size=6)
    file.read(0, 4)

    file.moveTo(6)
    expect(offset=6, size=6)
  }

  "flush" should "flush data" in {
    file.append(B("abcd"))
    file.append(B("12"))
    file.flush()
    file.close()

    file = openFile()
    file.read(4, 2) shouldBe B("12")
    file.read(0, 4) shouldBe B("abcd")
  }

}
