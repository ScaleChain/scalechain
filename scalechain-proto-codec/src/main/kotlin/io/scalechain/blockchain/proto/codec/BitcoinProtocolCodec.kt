package io.scalechain.blockchain.proto.codec


import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ProtocolCodecException
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.util.HexUtil
import io.scalechain.util.readableByteCount

class BitcoinProtocolCodec( private val protocol : NetworkProtocol ) {
  fun encode(message : ProtocolMessage, byteBuf : ByteBuf) {
    val envelope = BitcoinMessageEnvelope.build(protocol, message)

    val io = CodecInputOutputStream(byteBuf, isInput = false)
    BitcoinMessageEnvelopeCodec.transcode(io, envelope)
  }

  /** Decode bits and add decoded messages to the given vector.
    *
    * @param bitVector The data to decode.
    * @param messages The messages decoded from the given BitVector. The BitVector may have multiple messages, with or without an incomplete message. However, the BitVector itself may not have enough data to construct a message.
    * @return BitVector If we do not have enough data to construct a message, return the data as BitVector instead of constructing a message.
    */
  tailrec fun decode(encodedByteBuf : ByteBuf, messages : java.util.List<Any>) : Unit {
    if ( BitcoinMessageEnvelopeCodec.decodable(encodedByteBuf) ) {
//      val io = CodecInputOutputStream(encodedByteBuf, isInput = false)
      val envelope = BitcoinMessageEnvelopeCodec.decode(encodedByteBuf)!!

      BitcoinMessageEnvelope.verify(envelope)
      val protocolMessage = protocol.decode( envelope.payload, envelope.command )

      messages.add( protocolMessage )

      if ( encodedByteBuf.readableByteCount() >= BitcoinMessageEnvelopeCodec.MIN_ENVELOPE_BYTES) {
        decode(encodedByteBuf, messages )
      }
    }
  }
}


