package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto._
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