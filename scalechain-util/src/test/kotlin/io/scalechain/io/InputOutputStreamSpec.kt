package io.scalechain.io

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.scalechain.util.toByteArray
import org.junit.runner.RunWith
import io.scalechain.util.ByteBufExt

@RunWith(KTestJUnitRunner::class)
class InputOutputStreamSpec : FlatSpec(), Matchers {

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
  init {
    "fixedBytes" should "read bytes if isInput is true" {
      val byteBuf : ByteBuf = Unpooled.buffer()
      byteBuf.writeBytes("test".toByteArray())
      val io = InputOutputStream(byteBuf, isInput = true)
      String(io.fixedBytes(4, null).toByteArray()) shouldBe "test"

      shouldThrow<IndexOutOfBoundsException> {
        // If we try to read it again, we hit IndexOutOfBoundsException exception.
        io.fixedBytes(1, null)
      }
    }

    "fixedBytes" should "read bytes continuously if isInput is true" {
      val byteBuf : ByteBuf = Unpooled.buffer()
      byteBuf.writeBytes("helloworld".toByteArray())
      val io = InputOutputStream(byteBuf, isInput = true)
      String(io.fixedBytes(5, null).toByteArray()) shouldBe "hello"
      String(io.fixedBytes(5, null).toByteArray()) shouldBe "world"
    }


    "fixedBytes" should "write bytes if isInput is false" {
      val byteBuf : ByteBuf = Unpooled.buffer()
      val io = InputOutputStream(byteBuf, isInput = false)
      io.fixedBytes(4, ByteBufExt.from("test".toByteArray()))
      String(byteBuf.toByteArray()) shouldBe "test"

      // We can call toByteArray multiple times without affecting the readIndex on the ByteBuf.
      String(byteBuf.toByteArray()) shouldBe "test"
    }

    "fixedBytes" should "write bytes continuously if isInput is false" {
      val byteBuf : ByteBuf = Unpooled.buffer()
      val io = InputOutputStream(byteBuf, isInput = false)
      val sourceByteBuf = ByteBufExt.from("helloworld".toByteArray())
      io.fixedBytes(5, sourceByteBuf)
      io.fixedBytes(5, sourceByteBuf)
      String(byteBuf.toByteArray()) shouldBe "helloworld"
    }


    "fixedBytes" should "return an empty bytebuf if byte length 0 given isInput=true" {
      val byteBuf : ByteBuf = Unpooled.buffer()
      val io = InputOutputStream(byteBuf, isInput = true)
      io.fixedBytes(0, null).readableBytes() shouldBe 0
      io.fixedBytes(0, null).toByteArray().isEmpty() shouldBe true
    }

    "fixedBytes" should "write nothing if byte length 0 given isInput=false" {
      val byteBuf : ByteBuf = Unpooled.buffer()
      val io = InputOutputStream(byteBuf, isInput = false)
      io.fixedBytes(0, Unpooled.buffer())
      byteBuf.readableBytes() shouldBe 0
      byteBuf.toByteArray().isEmpty() shouldBe true
    }

    "fixedBytes" should "hit an assertion if byte length is less than 0" {
      val byteBuf : ByteBuf = Unpooled.buffer()
      val io = InputOutputStream(byteBuf, isInput = true)
      shouldThrow<AssertionError> {
        io.fixedBytes(-1, null)
      }
    }

    "fixedBytes" should "throw KotlinNullPointerException if bytes parameter is null even though isInput is false" {
      val byteBuf : ByteBuf = Unpooled.buffer()
      val io = InputOutputStream(byteBuf, isInput = false)
      shouldThrow<AssertionError> {
        io.fixedBytes(1, null)
      }
    }

    "fixedBytes" should "throw IndexOutOfBoundsException if isInput is true without enough data to read" {
      val byteBuf : ByteBuf = ByteBufExt.from("test".toByteArray())
      val io = InputOutputStream(byteBuf, isInput = false)

      shouldThrow<IndexOutOfBoundsException> {
        io.fixedBytes(5, ByteBufExt.from("test")) // Read 5 bytes even though byteBuf has only four bytes("test")
      }
    }

    "fixedBytes" should "throw IndexOutOfBoundsException if isInput is false without enough destination buffer to write" {
      val byteBuf : ByteBuf = Unpooled.buffer(0, 3 /* max capacity */)
      val io = InputOutputStream(byteBuf, isInput = false)

      shouldThrow<IndexOutOfBoundsException> {
        io.fixedBytes(4, ByteBufExt.from("test")) // Try to write four bytes("test") even though the destination buffer has only three byte capacity.
      }
    }

    "fixedBytes" should "throw IndexOutOfBoundsException if isInput is false without enough source data" {
      val byteBuf : ByteBuf = Unpooled.buffer()
      val io = InputOutputStream(byteBuf, isInput = false)

      shouldThrow<IndexOutOfBoundsException> {
        io.fixedBytes(5, ByteBufExt.from("test".toByteArray())) // Try to write five bytes even though the source buffer has only four bytes("test")
      }
    }

  }
}