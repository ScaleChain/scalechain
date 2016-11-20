package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.WalletOutput
import io.scalechain.blockchain.proto.codec.primitive.{UInt64, VarByteArray}
import io.scalechain.blockchain.proto.codec.{LockingScriptCodec, TransactionOutputCodec, MessagePartCodec}
import io.scalechain.util.ByteArray
import scodec.Codec
import scodec.codecs._

/*
object ByteArrayCodec {
  val codec : Codec<ByteArray> {
    ("array" | VarByteArray.codec)
  }.as<ByteArray>
}
*/

object CoinAddressCodec : MessagePartCodec<CoinAddress> {
  val codec: Codec<CoinAddress> = utf8_32.xmap(
    addressString => CoinAddress.from(addressString),
    coinAddress => coinAddress.base58)
}


object ParsedPubKeyScriptCodec : MessagePartCodec<ParsedPubKeyScript> {
  val codec: Codec<ParsedPubKeyScript> = LockingScriptCodec.codec.xmap(
    lockingScript => ParsedPubKeyScript.from(lockingScript),
    parsedPubKeyScript => parsedPubKeyScript.lockingScript)
}

// From sample code of scodec.
/*
    "support building a codec for an ADT" in {
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

object OutputOwnershipCodec : MessagePartCodec<OutputOwnership> {
  val codec : Codec<OutputOwnership> =
    discriminated<OutputOwnership>.by(uint8).
      typecase(1, CoinAddressCodec.codec).
      typecase(2, ParsedPubKeyScriptCodec.codec)
}


