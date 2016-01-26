package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.{ErrorCode, ProtocolParseException}
import io.scalechain.blockchain.proto._
import io.scalechain.io.{BlockDataOutputStream, InputOutputStream, BlockDataInputStream}


object VersionCodec extends ProtocolMessageCodec[Version] {
  val command = "version"
  val prototype = Version()

  def processImpl(stream: InputOutputStream, message: Version): Version = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object VerackCodec extends ProtocolMessageCodec[Verack] {
  val command = "verack"
  val prototype = Verack()
  def processImpl(stream : InputOutputStream, message : Verack) : Verack = {
    //message.copy( x =  )
    assert(false);
    null
  }
}


object AddrCodec extends ProtocolMessageCodec[Addr] {
  val command = "addr"
  val prototype = Addr()
  def processImpl(stream : InputOutputStream, message : Addr) : Addr = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object InvCodec extends ProtocolMessageCodec[Inv] {
  val command = "inv"
  val prototype = Inv()
  def processImpl(stream : InputOutputStream, message : Inv) : Inv = {
    //message.copy( x =  )
    assert(false);
    null
  }
}


object GetDataCodec extends ProtocolMessageCodec[GetData] {
  val command = "getdata"
  val prototype = GetData()
  def processImpl(stream : InputOutputStream, message : GetData) : GetData = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object NotFoundCodec extends ProtocolMessageCodec[NotFound] {
  val command = "notfound"
  val prototype = NotFound()
  def processImpl(stream : InputOutputStream, message : NotFound) : NotFound = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object GetBlocksCodec extends ProtocolMessageCodec[GetBlocks] {
  val command = "getblocks"
  val prototype = GetBlocks()
  def processImpl(stream : InputOutputStream, message : GetBlocks) : GetBlocks = {
    //message.copy( x =  )
    assert(false);
    null
  }
}


object GetHeadersCodec extends ProtocolMessageCodec[GetHeaders] {
  val command = "getheaders"
  val prototype = GetHeaders()
  def processImpl(stream : InputOutputStream, message : GetHeaders) : GetHeaders = {
    //message.copy( x =  )
    assert(false);
    null
  }
}


object TxCodec extends ProtocolMessageCodec[Tx] {
  val command = "tx"
  val prototype = Tx()
  def processImpl(stream : InputOutputStream, message : Tx) : Tx = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object HeadersCodec extends ProtocolMessageCodec[Headers] {
  val command = "headers"
  val prototype = Headers()
  def processImpl(stream : InputOutputStream, message : Headers) : Headers = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object GetAddrCodec extends ProtocolMessageCodec[GetAddr] {
  val command = "getaddr"
  val prototype = GetAddr()
  def processImpl(stream : InputOutputStream, message : GetAddr) : GetAddr = {
    //message.copy( x =  )
    assert(false);
    null
  }
}


object MempoolCodec extends ProtocolMessageCodec[Mempool] {
  val command = "mempool"
  val prototype = Mempool()
  def processImpl(stream : InputOutputStream, message : Mempool) : Mempool = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object CheckOrderCodec extends ProtocolMessageCodec[CheckOrder] {
  val command = "checkorder"
  val prototype = CheckOrder()
  def processImpl(stream : InputOutputStream, message : CheckOrder) : CheckOrder = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object SubmitOrderCodec extends ProtocolMessageCodec[SubmitOrder] {
  val command = "submitorder"
  val prototype = SubmitOrder()
  def processImpl(stream : InputOutputStream, message : SubmitOrder) : SubmitOrder = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object ReplyCodec extends ProtocolMessageCodec[Reply] {
  val command = "reply"
  val prototype = Reply()
  def processImpl(stream : InputOutputStream, message : Reply) : Reply = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object PingCodec extends ProtocolMessageCodec[Ping] {
  val command = "ping"
  val prototype = Ping()

  def processImpl(stream: InputOutputStream, message: Ping): Ping = {
    message.copy(
      nonce = stream.littleEndianLong(message.nonce)
    )
  }
}

object PongCodec extends ProtocolMessageCodec[Pong] {
  val command = "pong"
  val prototype = Pong()
  def processImpl(stream : InputOutputStream, message : Pong) : Pong = {
    message.copy(
      nonce = stream.littleEndianLong(message.nonce)
    )
  }
}

object RejectCodec extends ProtocolMessageCodec[Reject] {
  val command = "reject"
  val prototype = Reject()
  def processImpl(stream : InputOutputStream, message : Reject) : Reject = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object FilterLoadCodec extends ProtocolMessageCodec[FilterLoad] {
  val command = "filterload"
  val prototype = FilterLoad()
  def processImpl(stream : InputOutputStream, message : FilterLoad) : FilterLoad = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object FilterAddCodec extends ProtocolMessageCodec[FilterAdd] {
  val command = "filteradd"
  val prototype = FilterAdd()
  def processImpl(stream : InputOutputStream, message : FilterAdd) : FilterAdd = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object FilterClearCodec extends ProtocolMessageCodec[FilterClear] {
  val command = "filterclear"
  val prototype = FilterClear()
  def processImpl(stream : InputOutputStream, message : FilterClear) : FilterClear = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object MerkleBlockCodec extends ProtocolMessageCodec[MerkleBlock] {
  val command = "merkleblock"
  val prototype = MerkleBlock()
  def processImpl(stream : InputOutputStream, message : MerkleBlock) : MerkleBlock = {
    //message.copy( x =  )
    assert(false);
    null
  }
}

object AlertCodec extends ProtocolMessageCodec[Alert] {
  val command = "alert"
  val prototype = Alert()
  def processImpl(stream : InputOutputStream, message : Alert) : Alert = {
    //message.copy( x =  )
    assert(false);
    null
  }
}