package io.scalechain.blockchain.proto.codec

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ProtocolCodecException
import java.nio.charset.StandardCharsets

import io.scalechain.crypto.HashFunctions


import io.scalechain.util.HexUtil
import io.scalechain.util.ArrayUtil

import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.primitive.Codecs
import io.scalechain.util.toByteArray

/** The envelope message that wraps actual payload.
 * Field Size,  Description,  Data type,  Comments
 * ================================================
 *           4,       magic,   uint32_t,  Magic value indicating message origin network, and used to seek to next message when stream state is unknown
 *          12,     command,   char[12],  ASCII string identifying the packet content, NULL padded (non-NULL padding results in packet rejected)
 *           4,      length,   uint32_t,  Length of payload in number of bytes
 *           4,    checksum,   uint32_t,  First 4 bytes of sha256(sha256(payload))
 *           ?,     payload,   uchar[],  The actual data
 */


data class Checksum(val value : ByteArray) {
    init {
        assert(value.size == Checksum.VALUE_SIZE)
    }

  override fun toString() = "Checksum(${HexUtil.kotlinHex(value)})"

  companion object {
      val VALUE_SIZE = 4

      fun fromHex(hexString : String) = Checksum(HexUtil.bytes(hexString))
  }
}

object ChecksumCodec : Codec<Checksum> {
    override fun transcode(io: CodecInputOutputStream, obj: Checksum?): Checksum? {
        val value = Codecs.fixedByteArray(Checksum.VALUE_SIZE).transcode(io, obj?.value)
        if (io.isInput) {
            return Checksum(value!!)
        }

        return null
    }
}


data class Magic(val value : ByteArray) {
    init {
        assert(value.size == Magic.VALUE_SIZE )
    }
    override fun toString() = "Magic(${HexUtil.kotlinHex(value)})"

    companion object {
        val VALUE_SIZE = 4

        val MAIN     = fromHex("D9B4BEF9")
        val TESTNET  = fromHex("DAB5BFFA")
        val TESTNET3 = fromHex("0709110B")
        val NAMECOIN = fromHex("FEB4BEF9")

        fun fromHex(hexString : String) = Magic(HexUtil.bytes(hexString))
/*
        val codec: Codec<Magic> = bytes(VALUE_SIZE).xmap(
            b => Magic.apply(b.reverse.toArray),
        m => ByteVector(m.value.array.reverse))
*/
    }
}

object MagicCodec : Codec<Magic> {
    override fun transcode(io: CodecInputOutputStream, obj: Magic?): Magic? {
        val value = Codecs.fixedReversedByteArray(Magic.VALUE_SIZE).transcode(io, obj?.value)
        if (io.isInput) {
            return Magic(value!!)
        }

        return null
    }
}

data class BitcoinMessageEnvelope(
    val magic    : Magic,
    val command  : String,
    val length   : Int,
    val checksum : Checksum,
    val payload  : ByteBuf) {
    //   override fun toString = s"Ping(BigInt(${scalaHex(nonce.toByteArray)}))"

    override fun toString() = """BitcoinMessageEnvelope($magic, \"${command}\", $length, $checksum, $payload)"""

    companion object {
      /**
       * Calculate checksum from a range of a byte array.
       * @param buffer The byte array to check.
       * @param offset The start offset of the buffer to calculate the checksum.
       * @param length The length of bytes starting from the offset to calculate the checksum.
       */
      fun checksum(buffer : ByteArray, offset : Int, length : Int) : Checksum {
        // OPTIMIZE : Directly calculate hash from the BitVector
        val hash = HashFunctions.hash256(buffer, offset, length)

        return Checksum(hash.value.copyOfRange(0,Checksum.VALUE_SIZE))
      }

      fun build(protocol:NetworkProtocol, message:ProtocolMessage) : BitcoinMessageEnvelope {
        val byteBuf = Unpooled.buffer()
        protocol.encode(byteBuf, message)

        val payload = byteBuf.toByteArray()

        return BitcoinMessageEnvelope(
          BitcoinConfiguration.config.magic,
          protocol.getCommand(message),
          payload.size,
          checksum(payload, 0, payload.size),
          byteBuf
        )
      }

      fun isMagicValid(magic : Magic) = magic == BitcoinConfiguration.config.magic

      fun verify(envelope : BitcoinMessageEnvelope) : Unit {

        if ( !isMagicValid(envelope.magic) )
          throw ProtocolCodecException( ErrorCode.IncorrectMagicValue )

        if ( envelope.length != envelope.payload.readableBytes() )
          throw ProtocolCodecException( ErrorCode.PayloadLengthMismatch)

        // BUGBUG : Try to avoid byte array copy.
        val payloadBytes = envelope.payload.toByteArray()
        if ( envelope.checksum != checksum( payloadBytes, 0, payloadBytes.size) )
          throw ProtocolCodecException( ErrorCode.PayloadChecksumMismatch)
      }
    }

}

object BitcoinMessageEnvelopeCodec : Codec<BitcoinMessageEnvelope> {
  val COMMAND_SIZE = 12

  private fun decodeCommand(zeroPaddedCommand : ByteArray): String {
    assert(zeroPaddedCommand.size == COMMAND_SIZE)

    // command is a 0 padded string. Get rid of trailing 0 values.
    val command: ByteArray = ArrayUtil.unpad(zeroPaddedCommand, 0.toByte())

    // BUGBUG : Dirty code. make it clean.
    return String(command, StandardCharsets.US_ASCII)
  }

  private fun encodeCommand(command: String) : ByteArray {
    assert(command.length <= COMMAND_SIZE)

    // BUGBUG : Dirty code. make it clean.
    val bytes = command.toByteArray(StandardCharsets.US_ASCII)
    // Pad the array with 0, to make the size to 12 bytes.
    return ArrayUtil.pad(bytes, COMMAND_SIZE /* targetLength */ , 0 /* value */)
  }

  val COMMAND_LENGTH = 12
  val PayloadLengthCodec = Codecs.UInt32L
    override fun transcode(io: CodecInputOutputStream, obj: BitcoinMessageEnvelope?): BitcoinMessageEnvelope? {
        val magic    = MagicCodec.transcode(io, obj?.magic)
        val command  = Codecs.fixedByteArray(COMMAND_LENGTH).transcode(io, if (obj==null) null else encodeCommand(obj.command))
        val length   = PayloadLengthCodec.transcode(io, obj?.length?.toLong())
        val checksum = ChecksumCodec.transcode(io, obj?.checksum)
        val payload  = io.fixedBytes(obj?.length ?: length!!.toInt() , obj?.payload)

        if (io.isInput) {
            return BitcoinMessageEnvelope(
                magic!!,
                decodeCommand(command!!),
                length!!.toInt(),
                checksum!!,
                payload!!
            )
        }
        return null
    }

  val PAYLOAD_LENGTH_SIZE = PayloadLengthCodec.encode(0L).size
  val MIN_ENVELOPE_BYTES = envelopSize(payloadLength=0)

  val PAYLOAD_LENGTH_OFFSET = Magic.VALUE_SIZE + COMMAND_LENGTH // command

  fun envelopSize(payloadLength : Long ) : Long {
    return Magic.VALUE_SIZE +
           COMMAND_LENGTH + // command
           PAYLOAD_LENGTH_SIZE + // length
           Checksum.VALUE_SIZE +
           payloadLength // payload
  }

  fun getPayloadLength(encodedByteBuf : ByteBuf) : Long {
    val payloadLengthByteBuf = Unpooled.buffer() // BUGBUG : Is this resulting in a performance issue?
    encodedByteBuf.getBytes(encodedByteBuf.readerIndex() + PAYLOAD_LENGTH_OFFSET, payloadLengthByteBuf, 0, PAYLOAD_LENGTH_SIZE)

    val payloadLength = PayloadLengthCodec.decode(payloadLengthByteBuf)
    return payloadLength!!
  }

  fun getMagic(encodedByteBuf : ByteBuf) : Magic {
    val magicByteBuf = Unpooled.buffer() // BUGBUG : Is this resulting in a performance issue?
    encodedByteBuf.getBytes(encodedByteBuf.readerIndex(), magicByteBuf, 0, Magic.VALUE_SIZE)

    val magic = MagicCodec.decode(magicByteBuf)
    return magic!!
  }

  fun decodable(encodedByteBuf : ByteBuf) : Boolean {
    if ( encodedByteBuf.readableBytes() < MIN_ENVELOPE_BYTES) {
      return false
    }

    if ( BitcoinMessageEnvelope.isMagicValid(getMagic(encodedByteBuf)) ) {
      val payloadLength = getPayloadLength(encodedByteBuf)

      return encodedByteBuf.readableBytes() >= envelopSize(payloadLength)
    } else {
      return false
    }
  }
}


data class BitcoinConfiguration( val magic : Magic ) {
    companion object {
        val config = BitcoinConfiguration(Magic.MAIN)
    }
}
