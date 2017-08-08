package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.WalletOutput
import io.scalechain.blockchain.proto.codec.primitive.Codecs
import io.scalechain.blockchain.proto.codec.LockingScriptCodec
import io.scalechain.blockchain.proto.codec.TransactionOutputCodec
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream

object CoinAddressCodec : Codec<CoinAddress> {
  override fun transcode(io: CodecInputOutputStream, obj: CoinAddress?): CoinAddress? {
    if (io.isInput) {
      val address = Codecs.CString.transcode(io, null)
      return CoinAddress.from(address!!)
    } else {
      val base58Address = obj!!.base58()
      Codecs.CString.transcode(io, base58Address)
      return null
    }
  }
}


object ParsedPubKeyScriptCodec : Codec<ParsedPubKeyScript> {
  override fun transcode(io: CodecInputOutputStream, obj: ParsedPubKeyScript?): ParsedPubKeyScript? {
    if (io.isInput) {
      val lockingScript = LockingScriptCodec.transcode(io, null)
      return ParsedPubKeyScript.from(lockingScript!!)
    } else {
      val lockingScript = obj!!.lockingScript()
      LockingScriptCodec.transcode(io, lockingScript)
      return null
    }
  }
}

// From sample code of scodec.
/*
    "support building a codec for an ADT" {
      sealed trait Direction
      case object Stay : Direction
      data class Go(units: Int) : Direction

      val stayCodec = provide(Stay)
      val goCodec = int32.widenOpt<Go>(Go.apply, Go.unapply)

      val codec =
        discriminated<Direction>.by(uint8).
          typecase(0, stayCodec).
          typecase(1, goCodec)

      roundtrip(codec, Stay)
      roundtrip(codec, Go(42))
    }
*/

object OutputOwnershipCodec : Codec<OutputOwnership> {
  val Codec = Codecs.polymorphicCodec<Byte, OutputOwnership>(
    typeIndicatorCodec = Codecs.Byte,
    typeClassNameToTypeIndicatorMap = mapOf<String, Byte>(
      "CoinAddress" to 1.toByte(),
      "ParsedPubKeyScript" to 2.toByte()
    ),
    typeIndicatorToCodecMap = mapOf<Byte, Codec<OutputOwnership>> (
      1.toByte() to CoinAddressCodec as Codec<OutputOwnership> ,
      2.toByte() to ParsedPubKeyScriptCodec as Codec<OutputOwnership>
    )
  )
  override fun transcode(io: CodecInputOutputStream, obj: OutputOwnership?): OutputOwnership? = Codec.transcode(io, obj)
}


