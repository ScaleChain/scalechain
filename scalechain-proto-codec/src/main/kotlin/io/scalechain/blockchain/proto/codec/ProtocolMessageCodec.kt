package io.scalechain.blockchain.proto.codec

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets


import java.io.EOFException

import io.scalechain.blockchain.*
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.*
import io.scalechain.io.InputOutputStream

/** Write/read a protocol message to/from a byte array stream.
  *
  * Source : https://en.bitcoin.it/wiki/Protocol_documentation
  */
/*
trait ProtocolMessageCodec<T <: ProtocolMessage> : MessagePartCodec<T> {

  val command : String
  val clazz : Class<T>

  // BUGBUG : Simply use T?
  fun encode( message : ProtocolMessage ) {
    val castedMessage = clazz.cast(message)
    codec.encode(castedMessage)
  }
}
*/


interface NetworkProtocol {
  fun getCommand(message : ProtocolMessage) : String
  // BUGBUG : Interface change  encode(message : ProtocolMessage) : ByteBuf -> encode(writeBuf : ByteBuf, message : ProtocolMessage)
  fun encode(writeBuf : ByteBuf, message : ProtocolMessage)
  // BUGBUG : Interface change  decode(command:String, byteBuf:ByteBuf) : ProtocolMessage -> decode(readBuf: ByteBuf, command:String) : ProtocolMessage
  fun decode(readBuf: ByteBuf, command:String) : ProtocolMessage
}



/** Encode or decode bitcoin protocol messages to/from on-the-wire format.
  * Specification of Protocol Messages : https://en.bitcoin.it/wiki/Protocol_documentation
  *
  * Design decisions made to have only minimal amount of maintenance cost.
  * 1. Implement protocol in only one method.
  * No need to implement both encoder and decoder for each message even though we have only one on-the-wire format for a message.
  * 2. No need to change codes here and there to modify the protocol or add a protocol message.
  * Just add a data class for the message and add an entry on codecs array of ProtocolMessageCodecs class.
  */
class BitcoinProtocol : NetworkProtocol {
  val codecs = listOf(
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

  val codecMapByCommand = (codecs.map{it.command} zip codecs).toMap()
  val codecMapByClass   = (codecs.map{it.clazz} zip codecs).toMap()


  override fun getCommand(message : ProtocolMessage) : String {
    val codec = codecMapByClass[message.javaClass]
    return codec!!.command
  }

  override fun encode(writeBuf : ByteBuf, message : ProtocolMessage) {

    // Force to type case the codec to transcode ProtocolMessage.
    // **b0c1** provided this code.
    val codec = codecMapByClass[message.javaClass]!! as ProtocolMessageCodec<ProtocolMessage>
    codec.transcode(CodecInputOutputStream(writeBuf, isInput = false), message)

    /*
    val io = CodecInputOutputStream(writeBuf, isInput = false)
    when(message) {
      is Version      -> VersionCodec.transcode(io, message)
      is Verack       -> VerackCodec.transcode(io, message)
      is Addr         -> AddrCodec.transcode(io, message)
      is Inv          -> InvCodec.transcode(io, message)
      is GetData      -> GetDataCodec.transcode(io, message)
      is NotFound     -> NotFoundCodec.transcode(io, message)
      is GetBlocks    -> GetBlocksCodec.transcode(io, message)
      is GetHeaders   -> GetHeadersCodec.transcode(io, message)
      is Transaction  -> TransactionCodec.transcode(io, message)
      is Block        -> BlockCodec.transcode(io, message)
      is Headers      -> HeadersCodec.transcode(io, message)
      is GetAddr      -> GetAddrCodec.transcode(io, message)
      is Mempool      -> MempoolCodec.transcode(io, message)
      is Ping         -> PingCodec.transcode(io, message)
      is Pong         -> PongCodec.transcode(io, message)
      is Reject       -> RejectCodec.transcode(io, message)
      is FilterLoad   -> FilterLoadCodec.transcode(io, message)
      is FilterAdd    -> FilterAddCodec.transcode(io, message)
      is FilterClear  -> FilterClearCodec.transcode(io, message)
      is MerkleBlock  -> MerkleBlockCodec.transcode(io, message)
      is Alert        -> AlertCodec.transcode(io, message)
      else            -> throw AssertionError()
    }
*/
  }

  override fun decode(readBuf: ByteBuf, command:String) : ProtocolMessage {
    val codec = codecMapByCommand[command]
    val message = codec!!.transcode(CodecInputOutputStream(readBuf, isInput = true), null)
    return message!!
  }
}
