package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive.{FixedByteArray, VarStr, VarInt}
import scodec.Codec
import scodec.codecs._

object RecordLocatorCodec extends MessagePartCodec[RecordLocator] {
  val codec : Codec[RecordLocator] = {
    ("offset" | int64L) ::
    ("size"   | int32L)
  }.as[RecordLocator]
}

object FileRecordLocatorCodec extends MessagePartCodec[FileRecordLocator]{
  val codec : Codec[FileRecordLocator] = {
    ("fileIndex" | int32L ) ::
    ("recordLocator" | RecordLocatorCodec.codec)
  }.as[FileRecordLocator]
}

object BlockFileInfoCodec extends MessagePartCodec[BlockFileInfo]{
  val codec : Codec[BlockFileInfo] = {
    ("blockCount" | int32L) ::
    ("fileSize" | int64L) ::
    ("fistBlockHeight" | int64L) ::
    ("lastBlockHeight" | int64L) ::
    ("firstBlockTimestamp" | int64L) ::
    ("lastBlockTimestamp" | int64L)
  }.as[BlockFileInfo]
}

object BlockInfoCodec extends MessagePartCodec[BlockInfo]{
  val codec : Codec[BlockInfo] = {
    ("height" | int32L) ::
    ("transactionCount" | int32L) ::
    ("status" | int32L) ::
    ("blockHeader" | BlockHeaderCodec.codec) ::
    ("blockLocatorOption" | optional(bool(8), FileRecordLocatorCodec.codec) )
  }.as[BlockInfo]
}

object FileNumberCodec extends MessagePartCodec[FileNumber] {
  val codec : Codec[FileNumber] = {
    ("file_number" | int32L)
  }.as[FileNumber]
}

/** Writes only one byte, to test the case where a record file has a remaining space.
  */
object OneByteCodec extends MessagePartCodec[OneByte]{
  val codec : Codec[OneByte] = {
    ("value" | byte)
  }.as[OneByte]
}

object TransactionCountCodec extends MessagePartCodec[TransactionCount] {
  val codec : Codec[TransactionCount] = {
    ("transaction_count" | VarInt.countCodec )
  }.as[TransactionCount]
}

/**
  * by mijeong
  *
  * TODO : Implement full WalletInfoCodec
  *
  * WalletInfoCodec Prototype
  */
object WalletInfoCodec extends MessagePartCodec[WalletInfo]{
  val codec : Codec[WalletInfo] = {
      ("status" | int32L) ::
      ("walletHeader" | WalletHeaderCodec.codec)
  }.as[WalletInfo]
}

/**
  * by mijeong
  */
object AddressKeyCodec extends MessagePartCodec[AddressKey]{
  val codec : Codec[AddressKey] = {
      ("address"            | VarStr.codec                 )
  }.as[AddressKey]
}

/**
  * by mijeong
  */
object AddressInfoCodec extends MessagePartCodec[AddressInfo]{
  val codec : Codec[AddressInfo] = {
      ("account"            | VarStr.codec                                    ) ::
      ("purpose"            | int32L                                          ) ::
      ("publicKey"          | FixedByteArray.codec(33)                        ) ::
      ("privateKeyLocator"  | optional(bool(8),FileRecordLocatorCodec.codec)  )
  }.as[AddressInfo]
}

/**
  * by mijeong
  */
object AccountInfoCodec extends MessagePartCodec[AccountInfo]{
  val codec : Codec[AccountInfo] = {
    ("account"            | VarStr.codec                 )
  }.as[AccountInfo]
}

object WalletTransactionInfoCodec extends MessagePartCodec[WalletTransactionInfo] {
  val codec : Codec[WalletTransactionInfo] = {
    ("category"            | int32L                                          ) ::
    ("txLocator"           | optional(bool(8),FileRecordLocatorCodec.codec)  )
  }.as[WalletTransactionInfo]
}
