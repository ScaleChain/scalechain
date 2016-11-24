package io.scalechain.blockchain.proto.codec
/*
import java.nio.charset.StandardCharsets

import io.scalechain.blockchain.proto.codec.primitive.VarInt
import io.scalechain.blockchain.{ProtocolCodecException, ErrorCode}
import io.scalechain.crypto.HashFunctions
import io.scalechain.util.{ByteArray, ComparableArray, HexUtil, ArrayUtil}
import ByteArray._
import io.scalechain.blockchain.proto.ProtocolMessage
import scodec.Codec
import scodec.codecs._
import scodec.bits.{ByteVector, BitVector}
import HexUtil.scalaHex

/** The envelope message that wraps actual payload.
 * Field Size,  Description,  Data type,  Comments
 * ================================================
 *           4,       magic,   uint32_t,  Magic value indicating message origin network, and used to seek to next message when stream state is unknown
 *          12,     command,   char[12],  ASCII string identifying the packet content, NULL padded (non-NULL padding results in packet rejected)
 *           4,      length,   uint32_t,  Length of payload in number of bytes
 *           4,    checksum,   uint32_t,  First 4 bytes of sha256(sha256(payload))
 *           ?,     payload,   uchar[],  The actual data
 */

object Checksum {
  val VALUE_SIZE = 4

  fun fromHex(hexString : String) = Checksum(HexUtil.bytes(hexString))

  val codec: Codec<Checksum> = bytes(VALUE_SIZE).xmap(
    b => Checksum.apply(b.toArray), // internal byte order, should not reverse bytes.
    c => ByteVector(c.value.array)) // internal byte order, should not reverse bytes.
}

data class Checksum(value : ByteArray) {
  assert(value.length == Checksum.VALUE_SIZE)

  override fun toString = s"Checksum($value)"
}

object Magic {
  val VALUE_SIZE = 4

  val MAIN     = fromHex("D9B4BEF9")
  val TESTNET  = fromHex("DAB5BFFA")
  val TESTNET3 = fromHex("0709110B")
  val NAMECOIN = fromHex("FEB4BEF9")

  fun fromHex(hexString : String) = Magic(HexUtil.bytes(hexString))

  val codec: Codec<Magic> = bytes(VALUE_SIZE).xmap(
    b => Magic.apply(b.reverse.toArray),
    m => ByteVector(m.value.array.reverse))
}

data class Magic(value : ByteArray) {
  assert(value.length == Magic.VALUE_SIZE )
  override fun toString = s"Magic($value)"
}

data class BitcoinMessageEnvelope(
  magic    : Magic,
  command  : String,
  length   : Int,
  checksum : Checksum,
  payload  : BitVector) {
  //   override fun toString = s"Ping(BigInt(${scalaHex(nonce.toByteArray)}))"

  override fun toString() {
    s"""BitcoinMessageEnvelope($magic, \"${command}\", $length, $checksum, $payload)"""
  }
}

data class BitcoinConfiguration( magic : Magic )

object BitcoinConfiguration {
  val config = BitcoinConfiguration(Magic.MAIN)
}

object BitcoinMessageEnvelope {
  val MIN_DATA_BITS = 24 * 8

  val COMMAND_SIZE = 12

  private fun decodeCommand(zeroPaddedCommand : Array<java.lang.Byte>): String {
    assert(zeroPaddedCommand.length == COMMAND_SIZE)

    // command is a 0 padded string. Get rid of trailing 0 values.
    val command: Array<java.lang.Byte> = ArrayUtil.unpad(zeroPaddedCommand, 0)

    // BUGBUG : Dirty code. make it clean.
    String(command.map(_.asInstanceOf<Byte>), StandardCharsets.US_ASCII)
  }

  private fun encodeCommand(command: String) : Array<java.lang.Byte> {
    assert(command.length <= COMMAND_SIZE)

    // BUGBUG : Dirty code. make it clean.
    val bytes = command.getBytes(StandardCharsets.US_ASCII).map(_.asInstanceOf<java.lang.Byte>)
    // Pad the array with 0, to make the size to 12 bytes.
    ArrayUtil.pad(bytes, COMMAND_SIZE /* targetLength */ , 0 /* value */)
  }

  fun checksum(payload : BitVector) : Checksum {
    // OPTIMIZE : Directly calculate hash from the BitVector
    val hash = HashFunctions.hash256(payload.toByteArray)
    //val hash = HashFunctions.hash256(payload.toByteBuffer.array())

    Checksum(hash.value.slice(0,Checksum.VALUE_SIZE))
  }

  fun build(protocol:NetworkProtocol, message:ProtocolMessage) : BitcoinMessageEnvelope {
    val payload = protocol.encode(message)

    assert(payload.length % 8 == 0)
    val payloadByteCount = (payload.length.toInt / 8)

    BitcoinMessageEnvelope(
      BitcoinConfiguration.config.magic,
      protocol.getCommand(message),
      payloadByteCount,
      checksum(payload),
      payload
    )
  }

  fun verify(envelope : BitcoinMessageEnvelope) : Unit {
    assert(envelope.payload.length % 8 == 0)

    if ( envelope.magic != BitcoinConfiguration.config.magic)
      throw ProtocolCodecException( ErrorCode.IncorrectMagicValue )

    if ( envelope.length != (envelope.payload.length / 8) )
      throw ProtocolCodecException( ErrorCode.PayloadLengthMismatch)

    if ( envelope.checksum != checksum( envelope.payload) )
      throw ProtocolCodecException( ErrorCode.PayloadChecksumMismatch)
  }

  fun encode(msg: BitcoinMessageEnvelope) : scodec.Attempt<scodec.bits.BitVector>{
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
}
**/
