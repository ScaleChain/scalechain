package io.scalechain.util

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.netty.buffer.Unpooled
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class ByteBufExtSpec : FlatSpec(), Matchers {

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()

    // tear-down code
    //
  }

  private fun testUnsignedIntLE(value:Long, expectedByteArray : ByteArray) = {
    val buffer = Unpooled.buffer()
    buffer.writeUnsignedIntLE(value)
    buffer.toByteArray() shouldBe expectedByteArray
  }
  private fun testUnsignedInt(value:Long, expectedByteArray : ByteArray) = {
    val buffer = Unpooled.buffer()
    buffer.writeUnsignedInt(value)
    buffer.toByteArray() shouldBe expectedByteArray
  }
  private fun testUnsignedShortLE(value:Int, expectedByteArray : ByteArray) = {
    val buffer = Unpooled.buffer()
    buffer.writeUnsignedShortLE(value)
    buffer.toByteArray() shouldBe expectedByteArray
  }
  private fun testUnsignedShort(value:Int, expectedByteArray : ByteArray) = {
    val buffer = Unpooled.buffer()
    buffer.writeUnsignedShort(value)
    buffer.toByteArray() shouldBe expectedByteArray
  }


  init {
    "from(string)" should "return a byte buffer that has the bytes from the given hex string" {
      ByteBufExt.from("") shouldBe Unpooled.buffer()
      ByteBufExt.from("00") shouldBe Unpooled.wrappedBuffer(byteArrayOf(0))
      ByteBufExt.from("01") shouldBe Unpooled.wrappedBuffer(byteArrayOf(1))
      ByteBufExt.from("0102") shouldBe Unpooled.wrappedBuffer(byteArrayOf(1,2))
    }
    "from(byteArray)" should "return a byte buffer that has the given bytes" {
      ByteBufExt.from(byteArrayOf()) shouldBe Unpooled.buffer()
      ByteBufExt.from(byteArrayOf(0)) shouldBe Unpooled.wrappedBuffer(byteArrayOf(0))
      ByteBufExt.from(byteArrayOf(1)) shouldBe Unpooled.wrappedBuffer(byteArrayOf(1))
      ByteBufExt.from(byteArrayOf(1,2)) shouldBe Unpooled.wrappedBuffer(byteArrayOf(1,2))
    }
    "Limits.UINT_MAX" should "have the maximum unsigned integer" {
      Limits.UINT_MAX shouldBe (1L shl 32) - 1
      Limits.USHORT_MAX shouldBe (1 shl 16) - 1
    }
    "ByteBuf.writeUnsignedIntLE" should "hit an assertion if the value is out of range" {
      shouldThrow<AssertionError> {
        Unpooled.buffer().writeUnsignedIntLE(-1)
      }
      shouldThrow<AssertionError> {
        Unpooled.buffer().writeUnsignedIntLE(Limits.UINT_MAX+1)
      }
    }
    "ByteBuf.writeUnsignedInt" should "hit an assertion if the value is out of range" {
      shouldThrow<AssertionError> {
        Unpooled.buffer().writeUnsignedInt(-1)
      }
      shouldThrow<AssertionError> {
        Unpooled.buffer().writeUnsignedInt(Limits.UINT_MAX+1)
      }
    }
    "ByteBuf.writeUnsignedShortLE" should "hit an assertion if the value is out of range" {
      shouldThrow<AssertionError> {
        Unpooled.buffer().writeUnsignedShortLE(-1)
      }
      shouldThrow<AssertionError> {
        Unpooled.buffer().writeUnsignedShortLE(Limits.USHORT_MAX+1)
      }
    }
    "ByteBuf.writeUnsignedShort" should "hit an assertion if the value is out of range" {
      shouldThrow<AssertionError> {
        Unpooled.buffer().writeUnsignedShort(-1)
      }
      shouldThrow<AssertionError> {
        Unpooled.buffer().writeUnsignedShort(Limits.USHORT_MAX+1)
      }
    }

    "ByteBuf.writeUnsignedIntLE" should "write the given value correctly" {
      testUnsignedIntLE(0, byteArrayOf(0,0,0,0))
      testUnsignedIntLE(1, byteArrayOf(1,0,0,0))
      testUnsignedIntLE(256, byteArrayOf(0,1,0,0))
      testUnsignedIntLE(256 * 256, byteArrayOf(0,0,1,0))
      testUnsignedIntLE(256 * 256 * 256, byteArrayOf(0,0,0,1))
      testUnsignedIntLE(Limits.UINT_MAX, byteArrayOf(-1,-1,-1,-1))
    }
    "ByteBuf.writeUnsignedInt" should "write the given value correctly" {
      testUnsignedInt(0, byteArrayOf(0,0,0,0))
      testUnsignedInt(1, byteArrayOf(0,0,0,1))
      testUnsignedInt(256, byteArrayOf(0,0,1,0))
      testUnsignedInt(256 * 256, byteArrayOf(0,1,0,0))
      testUnsignedInt(256 * 256 * 256, byteArrayOf(1,0,0,0))
      testUnsignedInt(Limits.UINT_MAX, byteArrayOf(-1,-1,-1,-1))
    }
    "ByteBuf.writeUnsignedShortLE" should "write the given value correctly" {
      testUnsignedShortLE(0, byteArrayOf(0,0))
      testUnsignedShortLE(1, byteArrayOf(1,0))
      testUnsignedShortLE(256, byteArrayOf(0,1))
      testUnsignedShortLE(Limits.USHORT_MAX, byteArrayOf(-1,-1))
    }
    "ByteBuf.writeUnsignedShort" should "write the given value correctly" {
      testUnsignedShortLE(0, byteArrayOf(0,0))
      testUnsignedShortLE(1, byteArrayOf(1,0))
      testUnsignedShortLE(256, byteArrayOf(0,1))
      testUnsignedShortLE(Limits.USHORT_MAX, byteArrayOf(-1,-1))
    }
    "ByteBuf.toByteArray" should "return byte array from the given byte buffer" {
      ByteBufExt.from(byteArrayOf()).toByteArray().toList()    shouldBe byteArrayOf().toList()
      ByteBufExt.from(byteArrayOf(1)).toByteArray().toList()   shouldBe byteArrayOf(1).toList()
      ByteBufExt.from(byteArrayOf(1,2)).toByteArray().toList() shouldBe byteArrayOf(1,2).toList()
    }
  }
}