package io.scalechain.blockchain.proto.codec

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.nio.charset.StandardCharsets

import io.scalechain.crypto.HashFunctions


import io.scalechain.util.HexUtil
import io.scalechain.util.ArrayUtil

import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.readableByteCount
import io.scalechain.blockchain.proto.codec.primitive.Codecs

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
      val MIN_DATA_BITS = 24 * 8

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

        return BitcoinMessageEnvelope(
          BitcoinConfiguration.config.magic,
          protocol.getCommand(message),
          byteBuf.readableByteCount(),
          checksum(byteBuf.array(), byteBuf.readerIndex(), byteBuf.readableByteCount()),
          byteBuf
        )
      }
/*
      fun verify(envelope : BitcoinMessageEnvelope) : Unit {

        if ( envelope.magic != BitcoinConfiguration.config.magic)
          throw ProtocolCodecException( ErrorCode.IncorrectMagicValue )

        if ( envelope.length != envelope.payload.readableByteCount() )
          throw ProtocolCodecException( ErrorCode.PayloadLengthMismatch)

        // BUGBUG : Try to avoid byte array copy.
        if ( envelope.checksum != checksum( envelope.payload.toByteArray() ) )
          throw ProtocolCodecException( ErrorCode.PayloadChecksumMismatch)
      }
*/
/*
      fun encode(msg: BitcoinMessageEnvelope) : {
        for {
          magic <- Magic.codec.encode(BitcoinConfiguration.config.magic)
          // BUBUG : DIRTY_CODE, remove .map(_.asInstanceOf<Byte>)
          command <- bytes(12).encode(ByteVector(encodeCommand(msg.command).map(_.asInstanceOf<Byte>)))
          length <- uint32L.encode(msg.length)
          checksum <- Checksum.codec.encode(msg.checksum)
        } yield magic ++ command ++ length ++ checksum ++ msg.payload
      }

      fun decode(bits: BitVector) : scodec.Attempt<scodec.DecodeResult<BitcoinMessageEnvelope>> {
        for {
          magic <- Magic.codec.decode(bits)
          command <- bytes(12).decode(magic.remainder)
          length <- uint32L.decode(command.remainder)
          checksum <- Checksum.codec.decode(length.remainder)
          (payload, rest) = checksum.remainder.splitAt(length.value * 8)
        } yield scodec.DecodeResult(
                  BitcoinMessageEnvelope(
                    magic.value,
                    // BUBUG : DIRTY_CODE, remove : map(_.asInstanceOf<java.lang.Byte>)
                    decodeCommand(command.value.toArray.map(_.asInstanceOf<java.lang.Byte>)),
                    length.value.toInt,
                    checksum.value,
                    payload),
                  rest)
      }

      val codec = Codec<BitcoinMessageEnvelope>(encode _, decode _)
*/
    }

}

object BitcoinMessageEnvelopeCodec : Codec<BitcoinMessageEnvelope> {
    override fun transcode(io: CodecInputOutputStream, obj: BitcoinMessageEnvelope?): BitcoinMessageEnvelope? {
        val magic    = MagicCodec.transcode(io, obj?.magic)
        val command  = Codecs.fixedByteArray(12).transcode(io, obj?.command?.toByteArray())
        val length   = Codecs.UInt32L.transcode(io, obj?.length?.toLong())
        val checksum = ChecksumCodec.transcode(io, obj?.checksum)
        val payload  = io.fixedBytes(obj?.length ?: length!!.toInt() , obj?.payload)

        if (io.isInput) {
            return BitcoinMessageEnvelope(
                magic!!,
                String(command!!),
                length!!.toInt(),
                checksum!!,
                payload!!
            )
        }
        return null
    }
}


data class BitcoinConfiguration( val magic : Magic ) {
    companion object {
        val config = BitcoinConfiguration(Magic.MAIN)
    }
}
