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
    * @param messages The list of protocol messages to encode.
    * @return The encoded byte string.
    */
  def encode(messages : List[ProtocolMessage]) : ByteString = {
    // Step 1 : Map protocol messages to byte strings.
    val byteStrings = messages map {
      message => {
        println("encoding : " + message)
        ByteString( codec.encode(message) )
      }
    }

    // Step 2 : Merge all ByteString(s) into a ByteString.
    byteStrings.foldLeft(ByteString("")) { (left, right) =>
      left ++ right
    }
  }
}
