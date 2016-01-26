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


  val codecs = Seq[ProtocolMessageCodec[_<:ProtocolMessage]](
    new ProtocolMessageCodec[VersionMessage] {
      val command = "version"
      val prototype = VersionMessage()

      def processImpl(stream: InputOutputStream, message: VersionMessage): VersionMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[VerackMessage] {
      val command = "verack"
      val prototype = VerackMessage()
      def processImpl(stream : InputOutputStream, message : VerackMessage) : VerackMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[AddrMessage] {
      val command = "addr"
      val prototype = AddrMessage()
      def processImpl(stream : InputOutputStream, message : AddrMessage) : AddrMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[InvMessage] {
      val command = "inv"
      val prototype = InvMessage()
      def processImpl(stream : InputOutputStream, message : InvMessage) : InvMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[GetDataMessage] {
      val command = "getdata"
      val prototype = GetDataMessage()
      def processImpl(stream : InputOutputStream, message : GetDataMessage) : GetDataMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[NotFoundMessage] {
      val command = "notfound"
      val prototype = NotFoundMessage()
      def processImpl(stream : InputOutputStream, message : NotFoundMessage) : NotFoundMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[GetBlocksMessage] {
      val command = "getblocks"
      val prototype = GetBlocksMessage()
      def processImpl(stream : InputOutputStream, message : GetBlocksMessage) : GetBlocksMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[GetHeadersMessage] {
      val command = "getheaders"
      val prototype = GetHeadersMessage()
      def processImpl(stream : InputOutputStream, message : GetHeadersMessage) : GetHeadersMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[TxMessage] {
      val command = "tx"
      val prototype = TxMessage()
      def processImpl(stream : InputOutputStream, message : TxMessage) : TxMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[BlockMessage] {
      val command = "block"
      val prototype = BlockMessage()
      def processImpl(stream : InputOutputStream, message : BlockMessage) : BlockMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[HeadersMessage] {
      val command = "headers"
      val prototype = HeadersMessage()
      def processImpl(stream : InputOutputStream, message : HeadersMessage) : HeadersMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[GetAddrMessage] {
      val command = "getaddr"
      val prototype = GetAddrMessage()
      def processImpl(stream : InputOutputStream, message : GetAddrMessage) : GetAddrMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[MempoolMessage] {
      val command = "mempool"
      val prototype = MempoolMessage()
      def processImpl(stream : InputOutputStream, message : MempoolMessage) : MempoolMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[CheckOrderMessage] {
      val command = "checkorder"
      val prototype = CheckOrderMessage()
      def processImpl(stream : InputOutputStream, message : CheckOrderMessage) : CheckOrderMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[SubmitOrderMessage] {
      val command = "submitorder"
      val prototype = SubmitOrderMessage()
      def processImpl(stream : InputOutputStream, message : SubmitOrderMessage) : SubmitOrderMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[ReplyMessage] {
      val command = "reply"
      val prototype = ReplyMessage()
      def processImpl(stream : InputOutputStream, message : ReplyMessage) : ReplyMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[PingMessage] {
      val command = "ping"
      val prototype = PingMessage()

      def processImpl(stream: InputOutputStream, message: PingMessage): PingMessage = {
        message.copy(
          nonce = stream.littleEndianLong(message.nonce)
        )
      }
    },
    new ProtocolMessageCodec[PongMessage] {
      val command = "pong"
      val prototype = PongMessage()
      def processImpl(stream : InputOutputStream, message : PongMessage) : PongMessage = {
        message.copy(
          nonce = stream.littleEndianLong(message.nonce)
        )
      }
    },
    new ProtocolMessageCodec[RejectMessage] {
      val command = "reject"
      val prototype = RejectMessage()
      def processImpl(stream : InputOutputStream, message : RejectMessage) : RejectMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[FilterLoadMessage] {
      val command = "filterload"
      val prototype = FilterLoadMessage()
      def processImpl(stream : InputOutputStream, message : FilterLoadMessage) : FilterLoadMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[FilterAddMessage] {
      val command = "filteradd"
      val prototype = FilterAddMessage()
      def processImpl(stream : InputOutputStream, message : FilterAddMessage) : FilterAddMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[FilterClearMessage] {
      val command = "filterclear"
      val prototype = FilterClearMessage()
      def processImpl(stream : InputOutputStream, message : FilterClearMessage) : FilterClearMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[MerkleBlockMessage] {
      val command = "merkleblock"
      val prototype = MerkleBlockMessage()
      def processImpl(stream : InputOutputStream, message : MerkleBlockMessage) : MerkleBlockMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    },
    new ProtocolMessageCodec[AlertMessage] {
      val command = "alert"
      val prototype = AlertMessage()
      def processImpl(stream : InputOutputStream, message : AlertMessage) : AlertMessage = {
        //message.copy( x =  )
        assert(false);
        null
      }
    }
  )

  val codecMapByCommand = (codecs.map(_.command) zip codecs).toMap
  val codecMapByClass   = (codecs.map(_.prototype.getClass) zip codecs).toMap
}
