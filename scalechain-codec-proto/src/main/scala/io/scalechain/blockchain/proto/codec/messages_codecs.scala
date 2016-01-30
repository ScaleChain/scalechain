package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive.UInt64
import UInt64.bigIntCodec
import scodec.Codec

object VersionCodec extends ProtocolMessageCodec[Version] {
  val command = "version"
  val clazz = classOf[Version]

  // TODO : Implement
  val codec : Codec[Version] = null
}

object VerackCodec extends ProtocolMessageCodec[Verack] {
  val command = "verack"
  val clazz = classOf[Verack]

  // TODO : Implement
  val codec : Codec[Verack] = null
}


object AddrCodec extends ProtocolMessageCodec[Addr] {
  val command = "addr"
  val clazz = classOf[Addr]

  // TODO : Implement
  val codec : Codec[Addr] = null
}

object InvCodec extends ProtocolMessageCodec[Inv] {
  val command = "inv"
  val clazz = classOf[Inv]

  // TODO : Implement
  val codec : Codec[Inv] = null
}


object GetDataCodec extends ProtocolMessageCodec[GetData] {
  val command = "getdata"
  val clazz = classOf[GetData]

  // TODO : Implement
  val codec : Codec[GetData] = null
}

object NotFoundCodec extends ProtocolMessageCodec[NotFound] {
  val command = "notfound"
  val clazz = classOf[NotFound]

  // TODO : Implement
  val codec : Codec[NotFound] = null
}

object GetBlocksCodec extends ProtocolMessageCodec[GetBlocks] {
  val command = "getblocks"
  val clazz = classOf[GetBlocks]

  // TODO : Implement
  val codec : Codec[GetBlocks] = null
}

object BlockCodec extends ProtocolMessageCodec[Block] {
  val command = "block"
  val clazz = classOf[Block]

  // TODO : Implement
  val codec : Codec[Block] = null
}

object GetHeadersCodec extends ProtocolMessageCodec[GetHeaders] {
  val command = "getheaders"
  val clazz = classOf[GetHeaders]

  // TODO : Implement
  val codec : Codec[GetHeaders] = null
}


object TransactionCodec extends ProtocolMessageCodec[Transaction] {
  val command = "tx"
  val clazz = classOf[Transaction]

  // TODO : Implement
  val codec : Codec[Transaction] = null
}

object HeadersCodec extends ProtocolMessageCodec[Headers] {
  val command = "headers"
  val clazz = classOf[Headers]

  // TODO : Implement
  val codec : Codec[Headers] = null
}

object GetAddrCodec extends ProtocolMessageCodec[GetAddr] {
  val command = "getaddr"
  val clazz = classOf[GetAddr]

  // TODO : Implement
  val codec : Codec[GetAddr] = null
}


object MempoolCodec extends ProtocolMessageCodec[Mempool] {
  val command = "mempool"
  val clazz = classOf[Mempool]

  // TODO : Implement
  val codec : Codec[Mempool] = null
}

object CheckOrderCodec extends ProtocolMessageCodec[CheckOrder] {
  val command = "checkorder"
  val clazz = classOf[CheckOrder]

  // TODO : Implement
  val codec : Codec[CheckOrder] = null
}

object SubmitOrderCodec extends ProtocolMessageCodec[SubmitOrder] {
  val command = "submitorder"
  val clazz = classOf[SubmitOrder]

  // TODO : Implement
  val codec : Codec[SubmitOrder] = null
}

object ReplyCodec extends ProtocolMessageCodec[Reply] {
  val command = "reply"
  val clazz = classOf[Reply]

  // TODO : Implement
  val codec : Codec[Reply] = null
}


object PingCodec extends ProtocolMessageCodec[Ping] {
  val command = "ping"
  val clazz = classOf[Ping]

  val codec : Codec[Ping] = Codec[BigInt].xmap(Ping.apply, _.nonce)
}

object PongCodec extends ProtocolMessageCodec[Pong] {
  val command = "pong"
  val clazz = classOf[Pong]

  val codec : Codec[Pong] = Codec[BigInt].xmap(Pong.apply, _.nonce)
}

object RejectCodec extends ProtocolMessageCodec[Reject] {
  val command = "reject"
  val clazz = classOf[Reject]

  // TODO : Implement
  val codec : Codec[Reject] = null
}

object FilterLoadCodec extends ProtocolMessageCodec[FilterLoad] {
  val command = "filterload"
  val clazz = classOf[FilterLoad]

  // TODO : Implement
  val codec : Codec[FilterLoad] = null
}

object FilterAddCodec extends ProtocolMessageCodec[FilterAdd] {
  val command = "filteradd"
  val clazz = classOf[FilterAdd]

  // TODO : Implement
  val codec : Codec[FilterAdd] = null
}

object FilterClearCodec extends ProtocolMessageCodec[FilterClear] {
  val command = "filterclear"
  val clazz = classOf[FilterClear]

  // TODO : Implement
  val codec : Codec[FilterClear] = null
}

object MerkleBlockCodec extends ProtocolMessageCodec[MerkleBlock] {
  val command = "merkleblock"
  val clazz = classOf[MerkleBlock]

  // TODO : Implement
  val codec : Codec[MerkleBlock] = null
}

object AlertCodec extends ProtocolMessageCodec[Alert] {
  val command = "alert"
  val clazz = classOf[Alert]

  // TODO : Implement
  val codec : Codec[Alert] = null
}