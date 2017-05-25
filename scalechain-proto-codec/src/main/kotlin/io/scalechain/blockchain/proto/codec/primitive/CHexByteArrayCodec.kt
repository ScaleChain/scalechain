package io.scalechain.blockchain.proto.codec.primitive

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import java.nio.charset.Charset
import io.scalechain.util.HexUtil

// Convert the byte array to a hex string, and write it as CString (null terminiated string)
class CHexByteArrayCodec : Codec<ByteArray> {
  override fun transcode(io : CodecInputOutputStream, obj : ByteArray? ) : ByteArray? {
    val hexStringToWrite = if (obj != null) {
      HexUtil.hex(obj)
    } else {
      null
    }
    val hexString = CStringCodec(Charset.forName("UTF-8")).transcode(io, hexStringToWrite);
    if (io.isInput) {
      return HexUtil.bytes(hexString!!)
    }
    return null
  }
}
