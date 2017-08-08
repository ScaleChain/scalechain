package io.scalechain.blockchain.proto.codec

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ProtocolCodecException
import io.scalechain.blockchain.proto.Checksum
import io.scalechain.blockchain.proto.Magic
import io.scalechain.blockchain.proto.Ping
import io.scalechain.blockchain.proto.codec.EnvelopeData.envelope
import io.scalechain.blockchain.proto.codec.EnvelopeData.message
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import io.scalechain.util.toByteArray
import org.junit.runner.RunWith
import java.math.BigInteger
import java.util.*

object EnvelopeData {
  val message = Ping(BigInteger.valueOf(100L))
  val envelope = BitcoinMessageEnvelope.build(BitcoinProtocol(), message)
}

@RunWith(KTestJUnitRunner::class)
class BitcoinMessageEnvelopeSpec : FlatSpec(), Matchers {

  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
  }

  init {
    "toString" should "return a string that represents the given bitcoin envelope" {
      println( "envelope.toString()->" + envelope.toString() )
      envelope.toString() shouldBe """BitcoinMessageEnvelope(Magic("d9b4bef9"), "ping", 8, Checksum("2467f11d"), 256)"""
    }

    "checksum" should "calculate the checksum of a byte array" {
      val payload = envelope.payload.toByteArray()
      envelope.checksum shouldBe BitcoinMessageEnvelope.checksum(payload, 0, payload.size)
    }

    "build" should "return a message envelope" {
      envelope.magic shouldBe Magic(Bytes(bytes("d9b4bef9")))
      envelope.command shouldBe "ping"
      envelope.length shouldBe 8
      envelope.checksum shouldBe Checksum(Bytes(bytes("2467f11d")))
      envelope.payload.toByteArray().toList() shouldBe PingCodec.encode(message).toList()
    }

    "isMagicValid" should "return true only if the magic is valid" {
      BitcoinMessageEnvelope.isMagicValid(envelope.magic) shouldBe true
      BitcoinMessageEnvelope.isMagicValid(Magic.NAMECOIN) shouldBe false
    }

    "verify" should "throw ProtocolCodecException if the magic in the envelope is invalid" {
      val thrown = shouldThrow<ProtocolCodecException> {
        BitcoinMessageEnvelope.verify(envelope.copy(
          magic = Magic.NAMECOIN
        ))
      }
      thrown.code shouldBe ErrorCode.IncorrectMagicValue
    }

    "verify" should "throw ProtocolCodecException if the actual payload bytes does not match the length field in the envelope" {
      val thrown = shouldThrow<ProtocolCodecException> {
        BitcoinMessageEnvelope.verify(envelope.copy(
          length = envelope.length + 1
        ))
      }
      thrown.code shouldBe ErrorCode.PayloadLengthMismatch
    }

    "verify" should "throw ProtocolCodecException if the checksum field does not match the calcuated checksum of the payload" {
      val thrown = shouldThrow<ProtocolCodecException> {
        BitcoinMessageEnvelope.verify(envelope.copy(
          checksum = Checksum(Bytes(bytes("00 00 00 00")) )
        ))
      }
      thrown.code shouldBe ErrorCode.PayloadChecksumMismatch
    }


    "verify" should "throw no exception if the envelop is valid" {
      BitcoinMessageEnvelope.verify(envelope)
    }
  }
}

@RunWith(KTestJUnitRunner::class)
class BitcoinMessageEnvelopeCodecSpec : FlatSpec(), Matchers {
  val codec = BitcoinMessageEnvelopeCodec
  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
  }

  val ENCODED_COMMAND = "ping\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000".toByteArray()

  init {
    "decodeCommand" should "return unpadded command from zero-padded command" {
      BitcoinMessageEnvelopeCodec.decodeCommand(ENCODED_COMMAND) shouldBe "ping"
    }

    "encodeCommand" should "return zero-padded command from the given unpadded command" {
      BitcoinMessageEnvelopeCodec.encodeCommand("ping").toList() shouldBe ENCODED_COMMAND.toList()
    }

    fun checkTwoEnvelopesAreEqual(envelope1 : BitcoinMessageEnvelope, envelope2 : BitcoinMessageEnvelope) {
      envelope1.magic shouldBe envelope2.magic
      envelope1.command shouldBe envelope2.command
      envelope1.length shouldBe envelope2.length
      envelope1.checksum shouldBe envelope2.checksum
      envelope1.payload.toByteArray().toList() shouldBe envelope2.payload.toByteArray().toList()
    }

    "transcode" should "be able to roundtrip encode/decode" {
      val decodedEnvelope = codec.decode( codec.encode(envelope) )
      checkTwoEnvelopesAreEqual(envelope, decodedEnvelope!!)
    }

    "envelopSize" should "return the size of the envelope" {
      codec.envelopSize(0) shouldBe 4L  +  // magic
                                    12L +  // command
                                    4L  +  // UInt32L
                                    4L  +  // checksum
                                    0L     // payload

      codec.envelopSize(1) shouldBe 4L  +  // magic
                                    12L +  // command
                                    4L  +  // UInt32L
                                    4L  +  // checksum
                                    1L     // payload
    }

    "getPayloadLength" should "return the length of the payload" {
      val encodedEnvelope = codec.encode(envelope)
      val encodedByteBuf = Unpooled.wrappedBuffer( encodedEnvelope )
      codec.getPayloadLength(encodedByteBuf) shouldBe envelope.payload.readableBytes().toLong()

      // After calling getPayloadLength, we still need to be able to decode the envelope.
      val decodedEnvelope = codec.transcode(CodecInputOutputStream( encodedByteBuf, true), null)
      checkTwoEnvelopesAreEqual(envelope, decodedEnvelope!!)
    }

    "getMagic" should "return the magic value in the envelope" {
      val encodedEnvelope = codec.encode(envelope)
      val encodedByteBuf = Unpooled.wrappedBuffer( encodedEnvelope )
      codec.getMagic(encodedByteBuf) shouldBe envelope.magic

      // After calling getMagic, we still need to be able to decode the envelope.
      val decodedEnvelope = codec.transcode(CodecInputOutputStream( encodedByteBuf, true), null)
      checkTwoEnvelopesAreEqual(envelope, decodedEnvelope!!)

    }

    "decodable" should "return false if the readable bytes is less than the minium evelope bytes" {
      val buffer = Unpooled.buffer()

      /*
      // Minimum bytes for a message envelope
        4   // magic
        12  // command
        4   // UInt32L
        4   // checksum
      */
      buffer.writeBytes( bytes("""
        f9 be b4 d9
        00 00 00 00  00 00 00 00  00 00 00 00
        00 00 00 00
        00 00 00
      """))
      codec.decodable(buffer) shouldBe false
    }

    "decodable" should "return false if the magic value is invalid" {
      val buffer = Unpooled.buffer()

      buffer.writeBytes( bytes("""
        00 00 00 00
        00 00 00 00  00 00 00 00  00 00 00 00
        00 00 00 00
        00 00 00 00
      """))
      codec.decodable(buffer) shouldBe false
    }
  }
}

@RunWith(KTestJUnitRunner::class)
class BitcoinConfigurationSpec : FlatSpec(), Matchers {
  init {
    "magic of Bitcoin configuration" should "have the magic for the mainnet" {
      BitcoinConfiguration.config.magic shouldBe Magic.MAIN
    }
  }
}

