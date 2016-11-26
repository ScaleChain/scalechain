package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.primitive.Codecs
import io.scalechain.util.Option

object RecordLocatorCodec : Codec<RecordLocator> {
  override fun transcode(io : CodecInputOutputStream, obj : RecordLocator? ) : RecordLocator? {
    val offset = Codecs.Int64L.transcode(io, obj?.offset)
    val size   = Codecs.Int32L.transcode(io, obj?.size)

    if (io.isInput) {
      return RecordLocator(
        offset!!,
        size!!
      )
    }
    return null
  }
}

object FileRecordLocatorCodec : Codec<FileRecordLocator>{
  override fun transcode(io : CodecInputOutputStream, obj : FileRecordLocator? ) : FileRecordLocator? {
    val fileIndex     = Codecs.Int32L.transcode(io, obj?.fileIndex)
    val recordLocator = RecordLocatorCodec.transcode(io, obj?.recordLocator)

    if (io.isInput) {
      return FileRecordLocator(
        fileIndex!!,
        recordLocator!!
      )
    }
    return null
  }
}

object BlockFileInfoCodec : Codec<BlockFileInfo>{

  override fun transcode(io : CodecInputOutputStream, obj : BlockFileInfo? ) : BlockFileInfo? {
    val blockCount          = Codecs.Int32L.transcode(io, obj?.blockCount)
    val fileSize            = Codecs.Int64L.transcode(io, obj?.fileSize)
    val firstBlockHeight    = Codecs.Int64L.transcode(io, obj?.firstBlockHeight)
    val lastBlockHeight     = Codecs.Int64L.transcode(io, obj?.lastBlockHeight)
    val firstBlockTimestamp = Codecs.Int64L.transcode(io, obj?.firstBlockTimestamp)
    val lastBlockTimestamp  = Codecs.Int64L.transcode(io, obj?.lastBlockTimestamp)

    if (io.isInput) {
      return BlockFileInfo(
        blockCount!!,
        fileSize!!,
        firstBlockHeight!!,
        lastBlockHeight!!,
        firstBlockTimestamp!!,
        lastBlockTimestamp!!
      )
    }
    return null
  }
}

object BlockInfoCodec : Codec<BlockInfo>{
  val optionalHashCodec              = Codecs.optional(valueCodec = HashCodec)
  val optionalFileRecordLocatorCodec = Codecs.optional(valueCodec = FileRecordLocatorCodec)
  override fun transcode(io : CodecInputOutputStream, obj : BlockInfo? ) : BlockInfo? {
    val height             = Codecs.Int64L.transcode(io, obj?.height)
    val chainWork          = Codecs.Int64L.transcode(io, obj?.chainWork)
    val nextBlockHash      = optionalHashCodec.transcode(io, Option.from(obj?.nextBlockHash))
    val transactionCount   = Codecs.Int32L.transcode(io, obj?.transactionCount)
    val status             = Codecs.Int32L.transcode(io, obj?.status)
    val blockHeader        = BlockHeaderCodec.transcode(io, obj?.blockHeader)
    val blockLocatorOption = optionalFileRecordLocatorCodec.transcode(io, Option.from(obj?.blockLocatorOption))

    if (io.isInput) {
      return BlockInfo(
        height!!,
        chainWork!!,
        nextBlockHash!!.toNullable(),
        transactionCount!!,
        status!!,
        blockHeader!!,
        blockLocatorOption!!.toNullable()
      )
    }
    return null
  }
}

object FileNumberCodec : Codec<FileNumber> {
  override fun transcode(io : CodecInputOutputStream, obj : FileNumber? ) : FileNumber? {
    val fileNumber = Codecs.Int32L.transcode(io, obj?.fileNumber)

    if (io.isInput) {
      return FileNumber(
        fileNumber!!
      )
    }
    return null
  }
}

/** Writes only one byte, to test the case where a record file has a remaining space.
  */
object OneByteCodec : Codec<OneByte>{
  override fun transcode(io : CodecInputOutputStream, obj : OneByte? ) : OneByte? {
    val value = Codecs.Byte.transcode(io, obj?.value)
    if (io.isInput) {
      return OneByte(
        value!!
      )
    }
    return null
  }
}


object LongValueCodec : Codec<LongValue>{
  override fun transcode(io : CodecInputOutputStream, obj : LongValue? ) : LongValue? {
    val value = Codecs.Int64.transcode( io, obj?.value )
    if (io.isInput) {
      return LongValue(
        value!!
      )
    }
    return null
  }
}

/** The codec for TransactionCount.
  *
  */
object TransactionCountCodec : Codec<TransactionCount> {
  override fun transcode(io : CodecInputOutputStream, obj : TransactionCount? ) : TransactionCount? {
    val count = Codecs.VariableInt.transcode( io, obj?.count )
    if (io.isInput) {
      return TransactionCount(
        count!!
      )
    }
    return null
  }
}

object BlockHeightCodec : Codec<BlockHeight> {
  override fun transcode(io : CodecInputOutputStream, obj : BlockHeight? ) : BlockHeight? {
    val height = Codecs.Int64.transcode(io, obj?.height)
    if (io.isInput) {
      return BlockHeight(
        height!!
      )
    }
    return null
  }
}

internal val OptionalInPointListCodec =
  Codecs.variableList(
    valueCodec = Codecs.optional(valueCodec = InPointCodec)
  )

object TransactionDescriptorCodec : Codec<TransactionDescriptor> {
  override fun transcode(io : CodecInputOutputStream, obj : TransactionDescriptor? ) : TransactionDescriptor? {
    val transactionLocator = FileRecordLocatorCodec.transcode( io, obj?.transactionLocator )
    val blockHeight        = Codecs.Int64.transcode( io, obj?.blockHeight )
    val outputsSpentBy     = OptionalInPointListCodec.transcode( io, obj?.outputsSpentBy?.map{ Option.from(it) } )

    if (io.isInput) {
      return TransactionDescriptor(
        transactionLocator!!,
        blockHeight!!,
        outputsSpentBy!!.map{ it.toNullable() }
      )
    }
    return null
  }
}

object OrphanBlockDescriptorCodec : Codec<OrphanBlockDescriptor> {
  override fun transcode(io : CodecInputOutputStream, obj : OrphanBlockDescriptor? ) : OrphanBlockDescriptor? {
    val block = BlockCodec.transcode(io, obj?.block)
    if (io.isInput) {
      return OrphanBlockDescriptor(
        block!!
      )
    }
    return null
  }
}

object OrphanTransactionDescriptorCodec : Codec<OrphanTransactionDescriptor> {
  override fun transcode(io : CodecInputOutputStream, obj : OrphanTransactionDescriptor? ) : OrphanTransactionDescriptor? {
    val transaction = TransactionCodec.transcode(io, obj?.transaction)
    if (io.isInput) {
      return OrphanTransactionDescriptor(
        transaction!!
      )
    }
    return null
  }
}

object TransactionPoolEntryCodec : Codec<TransactionPoolEntry> {

  override fun transcode(io : CodecInputOutputStream, obj : TransactionPoolEntry? ) : TransactionPoolEntry? {
    val transaction    = TransactionCodec.transcode(io, obj?.transaction)
    val outputsSpentBy = OptionalInPointListCodec.transcode(io, obj?.outputsSpentBy?.map{ Option.from(it) })
    val createdAtNanos = Codecs.Int64.transcode(io, obj?.createdAtNanos)

    if (io.isInput) {
      return TransactionPoolEntry(
        transaction!!,
        outputsSpentBy!!.map{ it.toNullable() },
        createdAtNanos!!
      )
    }
    return null
  }
}
