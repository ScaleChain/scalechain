package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.{ErrorCode, ProtocolCodecException}
import io.scalechain.blockchain.proto.{Hash, Block, ProtocolMessage}
import io.scalechain.util.HexUtil
import scodec.{DecodeResult, Attempt}
import scodec.bits.{BitVector}

class BitcoinProtocolCodec( protocol : NetworkProtocol ) {
  fun encode(message : ProtocolMessage): Array<Byte> {
    val envelope = BitcoinMessageEnvelope.build(protocol, message)
    BitcoinMessageEnvelope.codec.encode(envelope) match {
      case Attempt.Successful(bitVector) => {
        bitVector.toByteArray
      }
      case Attempt.Failure(err) => {
        throw ProtocolCodecException(ErrorCode.EncodeFailure, err.toString)
      }
    }
  }

  /** Decode bits and add decoded messages to the given vector.
    *
    * @param bitVector The data to decode.
    * @param messages The messages decoded from the given BitVector. The BitVector may have multiple messages, with or without an incomplete message. However, the BitVector itself may not have enough data to construct a message.
    * @return BitVector If we do not have enough data to construct a message, return the data as BitVector instead of constructing a message.
    */
  fun decode(bitVector : BitVector, messages : java.util.Vector<ProtocolMessage>) : BitVector {
    if ( bitVector.length < BitcoinMessageEnvelope.MIN_DATA_BITS) {
      bitVector
    } else {
      val (envelope, remainder) = BitcoinMessageEnvelope.codec.decode(bitVector) match {
        case Attempt.Successful(DecodeResult(decoded, remainder)) => {

          (decoded, remainder)
        }
        case Attempt.Failure(err) => {
          throw ProtocolCodecException(ErrorCode.DecodeFailure, err.toString)
        }
      }


      if ( (envelope.payload.length / 8) < envelope.length  ) { // Not enough data received for an envelope.
        bitVector
      } else {
        BitcoinMessageEnvelope.verify(envelope)
        val protocolMessage = protocol.decode( envelope.command, envelope.payload )
/*
        if (envelope.command == "block") {
          val block = protocolMessage.asInstanceOf<Block>
          if (block.header.hashPrevBlock == Hash("000000006f6709b76bed31001b32309167757007aa4fb899f8168c8e9c084b1a")) {
            println(s"decoded:\n$protocolMessage\nprotocol data:\n${HexUtil.hex(bitVector.bytes.toArray)}\n")
            System.exit(-1)
          }
        }
*/
        messages.add( protocolMessage )

        if ( remainder.isEmpty ) {
          null
        } else {
          decode(remainder, messages )
        }
      }

      /*
      println("received : " + envelope)

      try {
        if ( (envelope.payload.length / 8) < envelope.length  ) { // Not enough data received for an envelope.
          println("Not enough data.")
          bitVector
        } else {
          BitcoinMessageEnvelope.verify(envelope)
          val protocolMessage = protocol.decode( envelope.command, envelope.payload )

          messages.add( protocolMessage )

          println("adding : " + protocolMessage)

          if ( remainder.isEmpty ) {
            null
          } else {
            println("remaining : " + remainder.length)

            decode(remainder, messages )
          }
        }
      } catch {
        case e : Exception => {
          println("Exception : " + e)
          e.printStackTrace()
          throw e
        }
      }
      */
    }
  }
}


