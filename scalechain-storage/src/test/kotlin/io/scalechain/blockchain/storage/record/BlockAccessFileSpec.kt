package io.scalechain.blockchain.storage.record

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.io.File
import java.nio.ByteBuffer
import io.scalechain.blockchain.storage.Storage
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class BlockAccessFileSpec : FlatSpec(), Matchers {
  val underlyingFile = File("./target/unittests-BlockAccessFileSpec")

  lateinit var file : BlockAccessFile

  fun openFile() = BlockAccessFile(underlyingFile, MAX_SIZE)

  override fun beforeEach() {
    if (underlyingFile.exists())
      underlyingFile.delete()

    file = openFile()

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    file.close()
  }

  /** Convert a string to a byte buffer.
    *
    * @param value The value to convert.
    * @return A byte buffer we got from the string value.
    */
  fun B(value : String) : ByteBuffer {
    return ByteBuffer.wrap( value.toByteArray() )
  }

  /** Convert a byte buffer to a byte array.
    *
    * @param buffer The byte buffer to convert.
    * @return The converted array of byte.
    */
  fun L(buffer : ByteBuffer) : List<Byte> {
    return buffer.array().toList()
  }


  /** Make sure that the offset and size attributes are set with the values we expect.
    *
    * @param offset The offset where we are reading/writing the file.
    * @param size The size of the file.
    */
  fun expect(offset : Long, size : Long): Unit {
    file.offset() shouldBe offset
    file.size() shouldBe size
  }

  init {
    Storage.initialize()

    "read" should "read values written by append" {
      expect(offset=0, size=0)

      file.append(B("abcd"))

      expect(offset=4, size=4)

      file.append(B("12"))

      expect(offset=6, size=6)

      L(file.read(0, 4)) shouldBe L(B("abcd"))
      L(file.read(4, 2)) shouldBe L(B("12"))
    }

    "append" should "append data at the end of the file even after reading data at the mid of the file " {
      expect(offset=0, size=0)

      file.append(B("abcd"))

      expect(offset=4, size=4)

      file.append(B("12"))

      expect(offset=6, size=6)

      L(file.read(0, 4)) shouldBe L(B("abcd"))

      file.append(B("XY"))

      expect(offset=8, size=8)
      L(file.read(4, 2)) shouldBe L(B("12"))
      L(file.read(6, 2)) shouldBe L(B("XY"))
    }

    "read" should "read values even after closing and re-opening the file." {
      file.append(B("abcd"))
      file.append(B("12"))

      file.close()
      file = openFile()

      expect(offset=0, size=6)

      L(file.read(4, 2)) shouldBe L(B("12"))
      L(file.read(0, 4)) shouldBe L(B("abcd"))
    }

    "append" should "append values even after closing and re-opening the file." {
      file.append(B("abcd"))
      file.append(B("12"))

      file.close()
      file = openFile()

      expect(offset=0, size=6)

      file.append(B("XY"))

      L(file.read(0, 4)) shouldBe L(B("abcd"))
      L(file.read(6, 2)) shouldBe L(B("XY"))
      L(file.read(4, 2)) shouldBe L(B("12"))
    }

    "moveTo" should "be able to move at the beginning of the file" {
      file.append(B("abcd"))
      file.append(B("12"))

      expect(offset=6, size=6)

      file.moveTo(0)
      expect(offset=0, size=6)
    }

    "moveTo" should "be able to move at the mid of the file" {
      file.append(B("abcd"))
      file.append(B("12"))

      expect(offset=6, size=6)
      file.read(0, 4)

      file.moveTo(2)
      expect(offset=2, size=6)
    }

    "moveTo" should "be able to move at the end of the file" {
      file.append(B("abcd"))
      file.append(B("12"))

      expect(offset=6, size=6)
      file.read(0, 4)

      file.moveTo(6)
      expect(offset=6, size=6)
    }

    "flush" should "flush data" {
      file.append(B("abcd"))
      file.append(B("12"))
      file.flush()
      file.close()

      file = openFile()
      L(file.read(4, 2)) shouldBe L(B("12"))
      L(file.read(0, 4)) shouldBe L(B("abcd"))
    }
  }

  companion object {
    val MAX_SIZE = 64L
  }
}
