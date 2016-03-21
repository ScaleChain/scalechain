package io.scalechain.blockchain.net

import akka.util.ByteString
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.{BitcoinProtocol, BitcoinProtocolCodec}
import scodec.bits.{BitVector}

/** ProtocolMessage decoder, which decodes protocol messages from byte strings.
  */
class ProtocolDecoder {
  private var codec: BitcoinProtocolCodec = new BitcoinProtocolCodec(new BitcoinProtocol)

  /**
    * An incomplete message, which needs to receive more data to construct a complete message.
    *
    * This happens situations as follow.
    * (1) when the data received is less than 24 bytes, which is the length for the header of bitcoin message.
    * (2) when we received less than the length specified at the payload length( BitcoinMessageEnvelope.length ).
    */
  private var incompleteMessage: BitVector = null

  /** Decode a list of protocol messages from a byte string.
    * If we have a remaining (incomplete) byte string, keep it to use it when remaining part of byte string comes again.
    * A byte string may contain multiple protocol messages, so we are returning a list of protocol messages.
    *
    * @param buffer The byte string which contains encoded protocol messages.
    * @return The list of (decoded) protocol messages.
    */
  def decode(buffer:ByteString) : List[ProtocolMessage] = {
    val bytes: Array[Byte] = buffer.toArray
    var inputMessage: BitVector = BitVector.view(bytes)

    val messages = new java.util.Vector[ProtocolMessage]()

    if (incompleteMessage != null) {
      inputMessage = incompleteMessage ++ inputMessage
    }

    // In case there is any remaining bits, we keep it in incompleteMessage
    incompleteMessage = codec.decode(inputMessage, messages)

    // BUGBUG : Optimize the code to use List in the first place, instead of converting a Vector to a List.
    import collection.JavaConverters._
    val decodedMessages = messages.asScala.toList

    decodedMessages foreach {
      message =>
        //println("decoded : " + message)
    }

    decodedMessages
  }
}
