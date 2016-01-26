package io.scalechain.blockchain.proto.codec

import java.nio.charset.StandardCharsets

import io.scalechain.util.ArrayUtil
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.io.{BlockDataOutputStream, BlockDataInputStream}

/** The envelope message that wraps actual payload.
 * Field Size,  Description,  Data type,  Comments
 * ================================================
 *           4,       magic,   uint32_t,  Magic value indicating message origin network, and used to seek to next message when stream state is unknown
 *          12,     command,   char[12],  ASCII string identifying the packet content, NULL padded (non-NULL padding results in packet rejected)
 *           4,      length,   uint32_t,  Length of payload in number of bytes
 *           4,    checksum,   uint32_t,  First 4 bytes of sha256(sha256(payload))
 *           ?,     payload,    uchar[],  The actual data
 */
case class BitcoinMessageEnvelope(
  magic : Int,
  command : String,
  length : Int,
  checksum : Int,
  payload : ProtocolMessage)

object BitcoinMessageEnvelope {
  object MAGIC {
    val MAIN = 0xD9B4BEF9
    val TESTNET = 0xDAB5BFFA
    val TESTNET3 = 0x0709110B
    val NAMECOIN = 0xFEB4BEF9
  }

  def build(message : ProtocolMessage) : BitcoinMessageEnvelope = {
    // TODO : Implement
    assert(false);
    null
  }

  def verify(envelope : BitcoinMessageEnvelope) : Unit = {
    // TODO : Implement
    assert(false);
  }


  def parse(stream: BlockDataInputStream): BitcoinMessageEnvelope = {
    val codecs = new ProtocolMessageCodecs()

    // TODO : check magic value.
    assert(false);
    val magic = stream.readLittleEndianInt()
    val command = parseCommand(stream)
    val length = stream.readLittleEndianInt()
    // TODO : verify checksum
    assert(false);
    val checksum = stream.readLittleEndianInt()
    val protocolMessage = codecs.decode(command, stream)

    BitcoinMessageEnvelope(magic, command, length, checksum, protocolMessage)
  }

  private def parseCommand(stream: BlockDataInputStream): String = {
    // Read 12 bytes. The command of a bitcoin envelope is always 12 bytes.
    val zeroPaddedCommand = stream.readBytes(12)
    // command is a 0 padded string. Get rid of trailing 0 values.
    val command: Array[Byte] = ArrayUtil.unpad(zeroPaddedCommand, 0)
    new String(command, StandardCharsets.US_ASCII)
  }


  def serialize(envelope: BitcoinMessageEnvelope, stream: BlockDataOutputStream): Unit = {
    val codecs = new ProtocolMessageCodecs()

    stream.writeLittleEndianInt(envelope.magic)
    serializeCommand(envelope.command, stream)
    stream.writeLittleEndianInt(envelope.length)
    // TODO : calculate checksum
    assert(false);
    stream.writeLittleEndianInt(envelope.checksum)
    codecs.encode(stream, envelope.payload)
  }

  private def serializeCommand(command: String, stream: BlockDataOutputStream): Unit = {
    assert(command.length <= 12)

    val bytes = command.getBytes(StandardCharsets.US_ASCII)
    // Pad the array with 0, to make the size to 12 bytes.
    ArrayUtil.pad(bytes, 12 /* targetLength */ , 0 /* value */)
  }
}

