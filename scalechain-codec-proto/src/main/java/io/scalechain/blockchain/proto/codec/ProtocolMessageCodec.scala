package io.scalechain.blockchain.proto.codec


import java.nio.charset.StandardCharsets


import java.io.EOFException

import io.scalechain.blockchain.{ErrorCode, ProtocolParseException}
import io.scalechain.blockchain.proto._
import io.scalechain.io.{InputOutputStream, BlockDataOutputStream, BlockDataInputStream}
import scala.reflect.{ClassTag, classTag}

trait ProtocolCodec {
  def process(stream : InputOutputStream, message : ProtocolMessage) : ProtocolMessage
}

/** Write/read a protocol message to/from a byte array stream.
  *
  * Source : https://en.bitcoin.it/wiki/Protocol_documentation
  */
abstract class ProtocolMessageCodec[T <:ProtocolMessage :ClassTag ] extends ProtocolCodec {
  val command : String
  val prototype : ProtocolMessage

  def process(stream : InputOutputStream,
              message : ProtocolMessage) : ProtocolMessage = {
    if (classTag[T].runtimeClass.isInstance(message)) {
      processImpl(stream, message.asInstanceOf[T])
    } else {
      throw new IllegalArgumentException()
    }
  }

  def processImpl(stream : InputOutputStream, message : T) : T
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
class ProtocolMessageCodecs {

  /** Parse the byte array stream to get a protocol message.
    *
    * @param command The command in the message envelope. See https://en.bitcoin.it/wiki/Protocol_documentation#Common_structures.
    * @param stream The stream where we read a protocol message.
    * @return ProtocolMessage if we successfully decoded a protocol message. None otherwise.
    */
  def decode(command : String, stream : BlockDataInputStream): ProtocolMessage = {
    try {
      val codec = codecMapByCommand(command)
      codec.process( new InputOutputStream(Left(stream)), codec.prototype )
    } catch {
      case e : java.util.NoSuchElementException => {
        throw new ProtocolParseException(ErrorCode.SerializerNotRegistered)
      }
    }
  }

  /** Write a protocol message to an output stream.
    *
    * @param stream The stream where we write the protocol message.
    * @param message The message we want to write to the output stream.
    */
  def encode(stream : BlockDataOutputStream, message : ProtocolMessage) : Unit = {
    try {
      val codec = codecMapByClass(message.getClass)
      codec.process( new InputOutputStream(Right(stream)), message )
    } catch {
      case e : java.util.NoSuchElementException => {
        throw new ProtocolParseException(ErrorCode.SerializerNotRegistered)
      }
    }
  }

  val codecs = Seq(
    VersionCodec,
    VerackCodec,
    AddrCodec,
    InvCodec,
    GetDataCodec,
    NotFoundCodec,
    GetBlocksCodec,
    GetHeadersCodec,
    TxCodec,
    BlockCodec,
    HeadersCodec,
    GetAddrCodec,
    MempoolCodec,
    CheckOrderCodec,
    SubmitOrderCodec,
    ReplyCodec,
    PingCodec,
    PongCodec,
    RejectCodec,
    FilterLoadCodec,
    FilterAddCodec,
    FilterClearCodec,
    MerkleBlockCodec,
    AlertCodec )

  val codecMapByCommand = (codecs.map(_.command) zip codecs).toMap
  val codecMapByClass   = (codecs.map(_.prototype.getClass) zip codecs).toMap
}
