package io.scalechain.blockchain.proto.codec

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.SendHeaders
import io.scalechain.blockchain.proto.Verack
import org.junit.runner.RunWith

/**
 * Created by kangmo on 15/01/2017.
 */
@RunWith(KTestJUnitRunner::class)
class BitcoinProtocolSpec : FlatSpec(), Matchers {
  val protocol = BitcoinProtocol()

  init {
    "getCommand" should "return the command based on the given protocol message" {
      protocol.getCommand(Verack()) shouldBe "verack"
      protocol.getCommand(SendHeaders()) shouldBe "sendheaders"
    }

    "encode" should "successfully encode a message that can be decodable" {
      val buffer1 = Unpooled.buffer()
      val buffer2 = Unpooled.buffer()
      // We already have tests for each class that extends ProtocolMessage
      // Test if BitcoinProtocolCodec.encode works well for two protocol messages.
      protocol.encode(buffer1, Verack() )
      protocol.decode(buffer1, "verack") shouldBe Verack()

      protocol.encode(buffer2, SendHeaders() )
      protocol.decode(buffer2, "sendheaders") shouldBe SendHeaders()
    }

    "decode" should "return a message without leaving any input bytes" {
      val buffer = Unpooled.buffer()

      protocol.encode(buffer, Verack() )
      protocol.decode(buffer, "verack") shouldBe Verack()
      buffer.readableBytes() shouldBe 0
    }

    "decode" should "return two messages without leaving any input bytes" {
      val buffer = Unpooled.buffer()

      protocol.encode(buffer, Verack())
      protocol.encode(buffer, SendHeaders())

      protocol.decode(buffer, "verack") shouldBe Verack()
      protocol.decode(buffer, "sendheaders") shouldBe SendHeaders()
      buffer.readableBytes() shouldBe 0
    }

    "decode" should "return a message leaving incomplete input bytes" {
      val buffer = Unpooled.buffer()

      protocol.encode(buffer, Verack() )
      buffer.writeByte(100) // Write an additional byte

      protocol.decode(buffer, "verack") shouldBe Verack()
      buffer.readableBytes() shouldBe 1
      buffer.readByte()      shouldBe 100.toByte()
    }

    "decode" should "return two messages leaving incomplete input bytes" {
      val buffer = Unpooled.buffer()

      protocol.encode(buffer, Verack())
      protocol.encode(buffer, SendHeaders())
      buffer.writeByte(100) // Write an additional byte

      protocol.decode(buffer, "verack") shouldBe Verack()
      protocol.decode(buffer, "sendheaders") shouldBe SendHeaders()
      buffer.readableBytes() shouldBe 1
      buffer.readByte()      shouldBe 100.toByte()
    }

    "decode" should "throw IndexOutOfBoundsException if an invalid command is given" {
      val buffer = Unpooled.buffer()

      protocol.encode(buffer, Verack() )
      shouldThrow<IndexOutOfBoundsException> {
        protocol.decode(buffer, "version")
      }
    }
  }
}