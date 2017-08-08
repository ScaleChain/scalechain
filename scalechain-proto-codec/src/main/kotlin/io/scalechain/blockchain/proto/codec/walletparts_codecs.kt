package io.scalechain.blockchain.proto.codec

import java.math.BigInteger

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.primitive.*
import io.scalechain.util.Option

object AccountCodec : Codec<Account> {
  override fun transcode(io : CodecInputOutputStream, obj : Account? ) : Account? {
    // As the account is used as a key in KeyValueDatabase,
    // we should not use codecs such as utf8_32 which prefixes the encoded data with the length of the data.
    // If any length of data is encoded, we can't compare string values on a KeyValueDatabase.

    // BUGBUG : Make sure it is ok to have utf8 for multi-byte languages such as Chinese or Korean.
    // If we are not doing any range search over the account name, it should be fine.
    val name = Codecs.CString.transcode(io, obj?.name)

    if (io.isInput) {
      return Account(
          name!!
      )
    }
    return null
  }
}


object OutPointCodec : Codec<OutPoint> {
  override fun transcode(io : CodecInputOutputStream, obj : OutPoint? ) : OutPoint? {
    // Note that we are not using the reverseCodec here.
    // OutPointCodec is for writing keys and values on the wallet database, not for communicating with peers.
    val transactionHash = HashCodec.transcode(io, obj?.transactionHash)
    val outputIndex = Codecs.Int32L.transcode(io, obj?.outputIndex)

    if (io.isInput) {
      return OutPoint(
        transactionHash!!,
        outputIndex!!
      )
    }
    return null
  }
}

object InPointCodec : Codec<InPoint> {
  override fun transcode(io : CodecInputOutputStream, obj : InPoint? ) : InPoint? {
    // Note that we are not using the reverseCodec here.
    // InPointCodec is for writing keys and values on the wallet database, not for communicating with peers.
    val transactionHash = HashCodec.transcode(io, obj?.transactionHash)
    val inputIndex      = Codecs.Int32L.transcode(io, obj?.inputIndex)

    if (io.isInput) {
      return InPoint(
        transactionHash!!,
        inputIndex!!
      )
    }
    return null
  }
}


object WalletTransactionCodec : Codec<WalletTransaction> {
  private val OptionalHashCodec  = Codecs.optional(HashCodec)
  private val OptionalInt64Codec = Codecs.optional(Codecs.Int64)
  private val OptionalInt32Codec = Codecs.optional(Codecs.Int32)

  override fun transcode(io : CodecInputOutputStream, obj : WalletTransaction? ) : WalletTransaction? {
    val blockHash = OptionalHashCodec.transcode(io, Option.from(obj?.blockHash))
    val blockIndex = OptionalInt64Codec.transcode(io, Option.from(obj?.blockIndex))
    val blockTime = OptionalInt64Codec.transcode(io, Option.from(obj?.blockTime))
    val transactionId = OptionalHashCodec.transcode(io, Option.from(obj?.transactionId))
    val addedTime = Codecs.Int64.transcode(io, obj?.addedTime)
    val transactionIndex = OptionalInt32Codec.transcode(io, Option.from(obj?.transactionIndex))
    val transaction = TransactionCodec.transcode(io, obj?.transaction)

    if (io.isInput) {
      return WalletTransaction(
        blockHash!!.toNullable(),
        blockIndex!!.toNullable(),
        blockTime!!.toNullable(),
        transactionId!!.toNullable(),
        addedTime!!,
        transactionIndex!!.toNullable(),
        transaction!!
      )
    }
    return null
  }

}

object OwnershipDescriptorCodec : Codec<OwnershipDescriptor> {
  private val StringListCodec = Codecs.variableListOf( Codecs.VariableString )
  override fun transcode(io : CodecInputOutputStream, obj : OwnershipDescriptor? ) : OwnershipDescriptor? {
    val account = Codecs.VariableString.transcode(io, obj?.account)
    val privateKeys = StringListCodec.transcode(io, obj?.privateKeys)

    if (io.isInput) {
      return OwnershipDescriptor(
        account!!,
        privateKeys!!
      )
    }
    return null
  }
}


object WalletOutputCodec : Codec<WalletOutput> {
  val OptionalInt64Codec = Codecs.optional( Codecs.Int64 )

  override fun transcode(io : CodecInputOutputStream, obj : WalletOutput? ) : WalletOutput? {

    val blockindex        = OptionalInt64Codec.transcode(io, Option.from(obj?.blockindex))
    val coinbase          = Codecs.Boolean.transcode(io, obj?.coinbase)
    val spent             = Codecs.Boolean.transcode(io, obj?.spent)
    val transactionOutput = TransactionOutputCodec.transcode(io, obj?.transactionOutput)

    if (io.isInput) {
      return WalletOutput(
        blockindex!!.toNullable(),
        coinbase!!,
        spent!!,
        transactionOutput!!
      )
    }
    return null
  }
}

