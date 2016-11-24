package io.scalechain.blockchain.proto.codec

import java.math.BigInteger

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.primitive.*



object AccountCodec : Codec<Account> {
  override fun transcode(io : CodecInputOutputStream, obj : Account? ) : Account? {
    // As the account is used as a key in KeyValueDatabase,
    // we should not use codecs such as utf8_32 which prefixes the encoded data with the length of the data.
    // If any length of data is encoded, we can't compare string values on a KeyValueDatabase.

    val name = .transcode(io, obj.?)

    if (io.isInput) {
      return Account(
          name
      )
    }
    return null
  }
/*
  val codec : Codec<Account> {
    ("name" | utf8 )
  }.as<Account>
*/
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
    val blockHash = OptionalHashCodec.transcode(io, obj?.blockHash)
    val blockIndex = OptionalInt64Codec.transcode(io, obj?.blockIndex)
    val blockTime = OptionalInt64Codec.transcode(io, obj?.blockTime)
    val transactionId = OptionalHashCodec.transcode(io, obj?.transactionId)
    val addedTime = Codecs.Int64.transcode(io, obj?.addedTime)
    val transactionIndex = OptionalInt32Codec.transcode(io, obj?.transactionIndex)
    val transaction = TransactionCodec.transcode(io, obj?.transaction)

    if (io.isInput) {
      return WalletTransaction(
        blockHash!!,
        blockIndex!!,
        blockTime!!,
        transactionId!!,
        addedTime!!,
        transactionIndex!!,
        transaction!!
      )
    }
    return null
  }

}

object OwnershipDescriptorCodec : Codec<OwnershipDescriptor> {
  private val StringListCodec = Codecs.variableList( Codecs.VariableString )
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

    val blockindex        = OptionalInt64Codec.transcode(io, obj?.blockindex)
    val coinbase          = Codecs.Boolean.transcode(io, obj?.coinbase)
    val spent             = Codecs.Boolean.transcode(io, obj?.spent)
    val transactionOutput = TransactionOutputCodec.transcode(io, obj?.transactionOutput)

    if (io.isInput) {
      return WalletOutput(
        blockindex!!,
        coinbase!!,
        spent!!,
        transactionOutput!!
      )
    }
    return null
  }
}

