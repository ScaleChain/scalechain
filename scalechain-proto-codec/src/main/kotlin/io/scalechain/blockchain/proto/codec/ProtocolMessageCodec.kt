package io.scalechain.blockchain.proto.codec

import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.ProtocolMessage

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
  private val codecs = listOf(
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
    SendHeadersCodec
    /*,
    FilterLoadCodec,
    FilterAddCodec,
    FilterClearCodec,
    MerkleBlockCodec,
    AlertCodec */
  )

  private val codecMapByCommand = (codecs.map{it.command} zip codecs).toMap()
  private val codecMapByClass   = (codecs.map{it.clazz} zip codecs).toMap()


  override fun getCommand(message : ProtocolMessage) : String {
    val codec = codecMapByClass[message.javaClass]
    return codec!!.command
  }

  override fun encode(writeBuf : ByteBuf, message : ProtocolMessage) {

    // Force to type case the codec to transcode ProtocolMessage.
    // **b0c1** provided this code.
    val codec = codecMapByClass[message.javaClass]!! as ProtocolMessageCodec<ProtocolMessage>
    codec.transcode(CodecInputOutputStream(writeBuf, isInput = false), message)
  }

  override fun decode(readBuf: ByteBuf, command:String) : ProtocolMessage {
    val codec = codecMapByCommand[command]
    val message = codec!!.transcode(CodecInputOutputStream(readBuf, isInput = true), null)
    return message!!
  }
}
