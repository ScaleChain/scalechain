package io.scalechain.blockchain.proto.codec

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.SendHeaders
import io.scalechain.blockchain.proto.Verack
import io.scalechain.blockchain.proto.Version
import org.junit.runner.RunWith

/**
  * Created by kangmo on 11/2/15.
  */
@RunWith(KTestJUnitRunner::class)
class BitcoinProtocolCodecSpec : FlatSpec(), Matchers {
  val codec = BitcoinProtocolCodec(BitcoinProtocol())

  init {
    "encode" should "successfully encode a message that can be decodable" {
      val messageList = mutableListOf<Any>()
      val buffer1 = Unpooled.buffer()
      val buffer2 = Unpooled.buffer()
      // We already have tests for each class that extends ProtocolMessage
      // Test if BitcoinProtocolCodec.encode works well for two protocol messages.
      codec.encode(Verack(), buffer1)
      codec.decode(buffer1, messageList)
      messageList shouldBe listOf(Verack())

      codec.encode(SendHeaders(), buffer2)
      codec.decode(buffer2, messageList)
      messageList shouldBe listOf(Verack(), SendHeaders())
    }

    "decode" should "return a message without leaving any input bytes" {
      val messageList = mutableListOf<Any>()
      val buffer = Unpooled.buffer()

      codec.encode(Verack(), buffer)
      codec.decode(buffer, messageList)
      messageList shouldBe listOf(Verack())
      buffer.readableBytes() shouldBe 0
    }

    "decode" should "return two messages without leaving any input bytes" {
      val messageList = mutableListOf<Any>()
      val buffer = Unpooled.buffer()

      codec.encode(Verack(), buffer)
      codec.encode(SendHeaders(), buffer)

      codec.decode(buffer, messageList)
      messageList shouldBe listOf(Verack(), SendHeaders())
      buffer.readableBytes() shouldBe 0
    }

    "decode" should "return a message leaving incomplete input bytes" {
      val messageList = mutableListOf<Any>()
      val buffer = Unpooled.buffer()

      codec.encode(Verack(), buffer)
      buffer.writeByte(100) // Write an additional byte

      codec.decode(buffer, messageList)
      messageList shouldBe listOf(Verack())
      buffer.readableBytes() shouldBe 1
      buffer.readByte()      shouldBe 100.toByte()
    }

    "decode" should "return two messages leaving incomplete input bytes" {
      val messageList = mutableListOf<Any>()
      val buffer = Unpooled.buffer()

      codec.encode(Verack(), buffer)
      codec.encode(SendHeaders(), buffer)
      buffer.writeByte(100) // Write an additional byte

      codec.decode(buffer, messageList)
      messageList shouldBe listOf(Verack(), SendHeaders())
      buffer.readableBytes() shouldBe 1
      buffer.readByte()      shouldBe 100.toByte()
    }
  }
}
