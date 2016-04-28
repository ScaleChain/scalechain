package io.scalechain.blockchain.proto.codec

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

  def fromHex(hexString : String) = Checksum(HexUtil.bytes(hexString))

  val codec: Codec[Checksum] = bytes(VALUE_SIZE).xmap(
    b => Checksum.apply(b.toArray), // internal byte order, should not reverse bytes.
    c => ByteVector(c.value.array)) // internal byte order, should not reverse bytes.
}

case class Checksum(value : ByteArray) {
  assert(value.length == Checksum.VALUE_SIZE)

  override def toString = s"Checksum($value)"
}

object Magic {
  val VALUE_SIZE = 4

  val MAIN     = fromHex("D9B4BEF9")
  val TESTNET  = fromHex("DAB5BFFA")
  val TESTNET3 = fromHex("0709110B")
  val NAMECOIN = fromHex("FEB4BEF9")

  def fromHex(hexString : String) = Magic(HexUtil.bytes(hexString))

  val codec: Codec[Magic] = bytes(VALUE_SIZE).xmap(
    b => Magic.apply(b.reverse.toArray),
    m => ByteVector(m.value.array.reverse))
}

case class Magic(value : ByteArray) {
  assert(value.length == Magic.VALUE_SIZE )
  override def toString = s"Magic($value)"
}

case class BitcoinMessageEnvelope(
  magic    : Magic,
  command  : String,
  length   : Int,
  checksum : Checksum,
  payload  : BitVector) {
  //   override def toString = s"Ping(BigInt(${scalaHex(nonce.toByteArray)}))"

  override def toString() = {
    s"""BitcoinMessageEnvelope($magic, \"${command}\", $length, $checksum, $payload)"""
  }
}

case class BitcoinConfiguration( magic : Magic )

object BitcoinConfiguration {
//  val config = BitcoinConfiguration(Magic.MAIN)
  val config = BitcoinConfiguration(Magic.TESTNET3)
}

object BitcoinMessageEnvelope {
  val MIN_DATA_BITS = 24 * 8

  val COMMAND_SIZE = 12

  private def decodeCommand(zeroPaddedCommand : Array[Byte]): String = {
    assert(zeroPaddedCommand.length == COMMAND_SIZE)

    // command is a 0 padded string. Get rid of trailing 0 values.
    val command: Array[Byte] = ArrayUtil.unpad(zeroPaddedCommand, 0)
    new String(command, StandardCharsets.US_ASCII)
  }

  private def encodeCommand(command: String) : Array[Byte] = {
    assert(command.length <= COMMAND_SIZE)

    val bytes = command.getBytes(StandardCharsets.US_ASCII)
    // Pad the array with 0, to make the size to 12 bytes.
    ArrayUtil.pad(bytes, COMMAND_SIZE /* targetLength */ , 0 /* value */)
  }

  def checksum(payload : BitVector) : Checksum = {
    // OPTIMIZE : Directly calculate hash from the BitVector
    val hash = HashFunctions.hash256(payload.toByteArray)
    Checksum(hash.value.slice(0,Checksum.VALUE_SIZE))
  }

  def build(protocol:NetworkProtocol, message:ProtocolMessage) : BitcoinMessageEnvelope = {
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

  def verify(envelope : BitcoinMessageEnvelope) : Unit = {
    assert(envelope.payload.length % 8 == 0)

    if ( envelope.magic != BitcoinConfiguration.config.magic)
      throw new ProtocolCodecException( ErrorCode.IncorrectMagicValue )

    if ( envelope.length != (envelope.payload.length / 8) )
      throw new ProtocolCodecException( ErrorCode.PayloadLengthMismatch)

    if ( envelope.checksum != checksum( envelope.payload) )
      throw new ProtocolCodecException( ErrorCode.PayloadChecksumMismatch)
  }

  def encode(msg: BitcoinMessageEnvelope) : scodec.Attempt[scodec.bits.BitVector]= {
    for {
      magic <- Magic.codec.encode(BitcoinConfiguration.config.magic)
      command <- bytes(12).encode(ByteVector(encodeCommand(msg.command)))
      length <- uint32L.encode(msg.length)
      checksum <- Checksum.codec.encode(msg.checksum)
    } yield magic ++ command ++ length ++ checksum ++ msg.payload
  }

  def decode(bits: BitVector) : scodec.Attempt[scodec.DecodeResult[BitcoinMessageEnvelope]] = {
    for {
      magic <- Magic.codec.decode(bits)
      command <- bytes(12).decode(magic.remainder)
      length <- uint32L.decode(command.remainder)
      checksum <- Checksum.codec.decode(length.remainder)
      (payload, rest) = checksum.remainder.splitAt(length.value * 8)
    } yield scodec.DecodeResult(
              BitcoinMessageEnvelope(
                magic.value,
                decodeCommand(command.value.toArray),
                length.value.toInt,
                checksum.value,
                payload),
              rest)
  }

  val codec = Codec[BitcoinMessageEnvelope](encode _, decode _)
}

