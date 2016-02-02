package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.{ErrorCode, ProtocolCodecException}
import io.scalechain.blockchain.proto.{ ProtocolMessage}
import scodec.{DecodeResult, Attempt}
import scodec.bits.{BitVector}

class BitcoinProtocolCodec( protocol : NetworkProtocol ) {
  def encode(message : ProtocolMessage): Array[Byte] = {
    val envelope = BitcoinMessageEnvelope.build(protocol, message)
    BitcoinMessageEnvelope.codec.encode(envelope) match {
      case Attempt.Successful(bitVector) => {
        bitVector.toByteArray
      }
      case Attempt.Failure(err) => {
        throw new ProtocolCodecException(ErrorCode.EncodeFailure, err.toString)
      }
    }
  }
  def decode(bytes : Array[Byte]) : ProtocolMessage = {
    val bitVector: BitVector = BitVector.view(bytes)

    val envelope = BitcoinMessageEnvelope.codec.decode(bitVector) match {
      case Attempt.Successful(DecodeResult(decoded, remainder)) => {
        if ( remainder.isEmpty ) {
          decoded
        } else {
          throw new ProtocolCodecException(ErrorCode.RemainingNotEmptyAfterDecoding)
        }
      }
      case Attempt.Failure(err) => {
        throw new ProtocolCodecException(ErrorCode.DecodeFailure, err.toString)
      }
    }
    BitcoinMessageEnvelope.verify(envelope)
    protocol.decode( envelope.command, envelope.payload )
  }
}


