package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto.{Hash, BlockSignature, PrivateVersion, Version}
import io.scalechain.blockchain.proto.codec.primitive.{FixedByteArray, Bool, VarStr, BigIntForLongCodec}
import scodec.Codec
import scodec.codecs._

object PrivateVersionCodec extends ProtocolMessageCodec[PrivateVersion] {
  val command = "privversion"
  val clazz = classOf[PrivateVersion]
  val codec : Codec[PrivateVersion] = {
      ("blockSigningAddress" | VarStr.codec )
  }.as[PrivateVersion]
}

object BlockSignatureCodec extends MessagePartCodec[BlockSignature] {
  val codec : Codec[BlockSignature] = {
    ("magic" | FixedByteArray.codec(2)) ::
    ("blockHash" | HashCodec.codec )
  }.as[BlockSignature]
}
