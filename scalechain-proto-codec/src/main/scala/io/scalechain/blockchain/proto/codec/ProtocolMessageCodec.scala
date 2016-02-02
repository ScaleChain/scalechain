package io.scalechain.blockchain.proto.codec

import java.nio.charset.StandardCharsets


import java.io.EOFException

import io.scalechain.blockchain.{ErrorCode, ProtocolCodecException}
import io.scalechain.blockchain.proto._
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{DecodeResult, Attempt, Codec}


/** Write/read a protocol message to/from a byte array stream.
  *
  * Source : https://en.bitcoin.it/wiki/Protocol_documentation
  */
trait ProtocolMessageCodec[T <: ProtocolMessage] {

  val command : String
  val clazz : Class[T]
  val codec : Codec[T]

  def encode( message : ProtocolMessage ) = {
    val castedMessage = clazz.cast(message)
    codec.encode(castedMessage)
  }


  def serialize(obj : T) : Array[Byte] = {
    codec.encode(obj) match {
      case Attempt.Successful(bitVector) => {
        bitVector.toByteArray
      }
      case Attempt.Failure(err) => {
        throw new ProtocolCodecException(ErrorCode.EncodeFailure, err.toString)
      }
    }

  }

  def parse(data: Array[Byte]) : T = {
    val bitVector: BitVector = BitVector.view(data)

    codec.decode(bitVector) match {
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
  }

}

trait NetworkProtocol {
  def getCommand(message : ProtocolMessage) : String
  def encode(message : ProtocolMessage) : BitVector
  def decode(command:String, bitVector:BitVector) : ProtocolMessage
}



/** Encode or decode bitcoin protocol messages to/from on-the-wire format.
  * Specification of Protocol Messages : https://en.bitcoin.it/wiki/Protocol_documentation
  *
  * Design decisions made to have only minimal amount of maintenance cost.
  * 1. Implement protocol in only one method.
  * No need to implement both encoder and decoder for each message even though we have only one on-the-wire format for a message.
  * 2. No need to change codes here and there to modify the protocol or add a new protocol message.
  * Just add a case class for the message and add an entry on codecs array of ProtocolMessageCodecs class.
  */
class BitcoinProtocol extends NetworkProtocol {
  val codecs = Seq(
    VersionCodec,
    VerackCodec,
    AddrCodec,
    InvCodec,
    GetDataCodec,
    NotFoundCodec,
    GetBlocksCodec,
    GetHeadersCodec,
    TransactionCodec,
    BlockCodec,
    HeadersCodec,
    GetAddrCodec,
    MempoolCodec,
    PingCodec,
    PongCodec,
    RejectCodec,
    FilterLoadCodec,
    FilterAddCodec,
    FilterClearCodec,
    MerkleBlockCodec,
    AlertCodec )

  val codecMapByCommand = (codecs.map(_.command) zip codecs).toMap
  val codecMapByClass   = (codecs.map(_.clazz) zip codecs).toMap[Class[_ <:ProtocolMessage], ProtocolMessageCodec[_ <: ProtocolMessage]]

  def getCommand(message : ProtocolMessage) : String = {
    val codec = codecMapByClass(message.getClass)
    codec.command
  }

  def encode(message : ProtocolMessage) : BitVector = {
    val codec = codecMapByClass(message.getClass)

    codec.encode(message) match {
      case Attempt.Successful(bitVector) => {
        bitVector
      }
      case Attempt.Failure(err) => {
        throw new ProtocolCodecException(ErrorCode.EncodeFailure, err.toString)
      }
    }
  }

  def decode(command:String, bitVector: BitVector) : ProtocolMessage = {
    val codec = codecMapByCommand(command).codec
    val message = codec.decode(bitVector) match {
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
    message
  }
}
