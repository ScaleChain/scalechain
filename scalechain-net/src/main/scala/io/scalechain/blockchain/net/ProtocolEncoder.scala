package io.scalechain.blockchain.net

import akka.util.ByteString
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.{BitcoinProtocol, BitcoinProtocolCodec}

/** ProtocolMessage encoder, which encodes a list of protocol messages to a byte string.
  */
object ProtocolEncoder {
  private var codec: BitcoinProtocolCodec = new BitcoinProtocolCodec(new BitcoinProtocol)

  /** Encode a list of protcol messages to a byte string.
    *
    * @param message A protocol messages to encode.
    * @return The encoded byte string.
    */
  def encode(message : ProtocolMessage) : ByteString = {
    ByteString( codec.encode(message) )
  }
}
