package io.scalechain.blockchain.proto.codec.primitive

/*

import io.scalechain.blockchain.proto.{ProtocolMessage}
import io.scalechain.blockchain.proto.codec.{SerializeParseUtil, MessagePartCodec}
import scodec.Attempt.{Successful, Failure}
import scodec.*
import scodec.bits.*
import scodec.codecs.*


data class StringMessage(value:String) : ProtocolMessage
/**
  * String codec that uses the `US-ASCII` charset that encodes strings with a trailing `NUL` termination byte
  * and decodes a string up to the next `NUL` termination byte.
  * It fails to decode if the bit vector ends before a `NUL` termination byte can be found.
  *
  * Code copied from : https://gitter.im/scodec/scodec/archives/2015/06/17
  */

object CString : SerializeParseUtil<String> {
  val codec :Codec<String> = filtered(ascii, Codec<BitVector> {

    val nul = ByteVector.fromByte(0)

    override fun sizeBound: SizeBound = SizeBound.unknown
    override fun encode(bits: BitVector): Attempt<BitVector> {
      val bytes = bits.bytes
      if (bytes.containsSlice(nul)) {
        Failure(Err("cstring cannot encode character 'NUL'"))
      } else {
        Successful(bits ++ nul.bits)
      }
    }
    override fun decode(bits: BitVector): Attempt<DecodeResult<BitVector>> {
      val bytes = bits.bytes
      bytes.indexOfSlice(nul) match {
        case -1 => Failure(Err("Does not contain a 'NUL' termination byte."))
        case i => Successful(DecodeResult(bytes.take(i).bits, bytes.drop(i+1).bits))
      }
    }
  }).withToString("cstring")
}

data class CStringPrefixed<T>(prefix : String, data : T) : ProtocolMessage

/** A codec that prefixes data with a null terminated string.
  * This codec is used for creating prefixed keys in the storage layer.
  *
  * @param codecT The codec that decodes and encodes data right after the null terminated prefix string.
  * @tparam T The type of the data right after the null terminated prefix string.
  */
class CStringPrefixedCodec<T <: ProtocolMessage>(codecT : MessagePartCodec<T>) : MessagePartCodec<CStringPrefixed<T>> {
  val codec : Codec<CStringPrefixed<T>> {
    ("prefix" | CString.codec ) ::
    ("data"   | codecT.codec )
  }.as<CStringPrefixed<T>>
}

*/