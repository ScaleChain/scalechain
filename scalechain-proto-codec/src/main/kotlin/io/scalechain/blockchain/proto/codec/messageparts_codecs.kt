package io.scalechain.blockchain.proto.codec

import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.*
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.primitive.*
import io.scalechain.io.InputOutputStream

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/*
trait SerializeParseUtil<T> {
  val codec : Codec<T>

  fun serialize(obj : T) : Array<Byte> {

    val bitVector = codec.encode(obj).require

    val len : Int = bitVector.length.toInt
    // Make sure we have bit length aligned to bytes.
    assert((len & 0x00000007) == 0)
    val byteLen = len >> 3
    //
    val serializedBytes = Array<Byte>(byteLen)

    var i = 0
    while (i < byteLen) {
      serializedBytes(i) = bitVector.getByte(i)
      i += 1
    }
    serializedBytes

    //    codec.encode(obj).require.toByteArray
    /*
    codec.encode(obj) match {
      case Attempt.Successful(bitVector) => {
        bitVector.toByteArray
      }
      case Attempt.Failure(err) => {
        //println(s"error : ${err.toString}")
        throw ProtocolCodecException(ErrorCode.EncodeFailure, err.toString)
      }
    }*/
  }

  fun parse(data: Array<Byte>) : T {
    val bitVector: BitVector = BitVector.view(data)

    codec.decode(bitVector) match {
      case Attempt.Successful(DecodeResult(decoded, remainder)) => {
        if ( remainder.isEmpty ) {
          decoded
        } else {
          throw ProtocolCodecException(ErrorCode.RemainingNotEmptyAfterDecoding)
        }
      }
      case Attempt.Failure(err) => {
        throw ProtocolCodecException(ErrorCode.DecodeFailure, err.toString)
      }
    }
  }

  @tailrec
  final fun parseManyInternal(bitVector:BitVector, decodedItems : ListBuffer<T>) : Unit {
    codec.decode(bitVector) match {
      case Attempt.Successful(DecodeResult(decoded, remainder)) => {
        decodedItems.append(decoded)
        if ( !remainder.isEmpty ) {
          parseManyInternal(remainder, decodedItems)
          // throw ProtocolCodecException(ErrorCode.RemainingNotEmptyAfterDecoding)
        }
      }
      case Attempt.Failure(err) => {
        throw ProtocolCodecException(ErrorCode.DecodeFailure, err.toString)
      }
    }
  }

  fun parseMany(data: Array<Byte>) : List<T> {
    val decodedItems = ListBuffer<T>()
    val bitVector: BitVector = BitVector.view(data)
    parseManyInternal(bitVector, decodedItems)
    decodedItems.toList
  }
}

trait MessagePartCodec<T <: ProtocolMessage> : SerializeParseUtil<T> {
}
*/

object HashCodec : Codec<Hash> {
  private val HashValueCodec = Codecs.fixedByteBuf(32)
  override fun transcode( io : CodecInputOutputStream, obj : Hash? ) : Hash? {
    val value = io.transcode( HashValueCodec, obj?.value )

    if (io.isInput) {
      return Hash(
          value!!
      )
    } else {
      return null
    }
  }
}

object LockingScriptCodec : Codec<LockingScript> {
  override fun transcode(io : CodecInputOutputStream, obj : LockingScript? ) : LockingScript? {
    val data = Codecs.VariableByteBuf.transcode(io, obj?.data)

    if (io.isInput) {
      return LockingScript(
        data!!
      )
    }
    return null
  }
}

object UnlockingScriptCodec : Codec<UnlockingScript> {
  override fun transcode(io : CodecInputOutputStream, obj : UnlockingScript? ) : UnlockingScript? {
    val data = Codecs.VariableByteBuf.transcode(io, obj?.data)

    if (io.isInput) {
      return UnlockingScript(
        data!!
      )
    }
    return null
  }
}


/**
  * <Description>
  * Each non-coinbase input spends an outpoint from a previous transaction.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#txin
  *
  * <Protocol>
  *
  *  7b1eabe0209b1fe794124575ef807057
  *  c77ada2138ae4fa8d6c4de0398a14f3f ......... Outpoint TXID
  *  00000000 ................................. Outpoint index number
  *
  *  49 ....................................... Bytes in sig. script: 73
  *  | 48 ..................................... Push 72 bytes as data
  *  | | 30450221008949f0cb400094ad2b5eb3
  *  | | 99d59d01c14d73d8fe6e96df1a7150de
  *  | | b388ab8935022079656090d7f6bac4c9
  *  | | a94e0aad311a4268e082a725f8aeae05
  *  | | 73fb12ff866a5f01 ..................... Secp256k1 signature
  *
  *  ffffffff ................................. Sequence number: UINT32_MAX
  */
object NormalTransactionInputCodec : Codec<NormalTransactionInput> {
  override fun transcode(io : CodecInputOutputStream, obj : NormalTransactionInput? ) : NormalTransactionInput? {
    val outputTransactionHash = HashCodec.transcode(io, obj?.outputTransactionHash)
    val outputIndex           = Codecs.UInt32L.transcode(io, obj?.outputIndex)
    val unlockingScript       = UnlockingScriptCodec.transcode(io, obj?.unlockingScript)
    val sequenceNumber        = Codecs.UInt32L.transcode(io, obj?.sequenceNumber)

    if (io.isInput) {
      return NormalTransactionInput(
        outputTransactionHash!!,
        outputIndex!!,
        unlockingScript!!,
        sequenceNumber!!
      )
    }
    return null
  }
}

/** Convert the normal transaction input to generation transaction input
  * only if the hash and index matches to the generation transaction.
  *
  * The format is same to the normal transaction input, but the values differ.
  * So, we will create a codec based on the normal transaction input.
  *
  * Generation transaction's UTXO hash has all bits set to zero,
  * and its UTXO index has all bits set to one.
  *
  * <Description>
  * The first transaction in a block, called the coinbase transaction,
  * must have exactly one input, called a coinbase.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#coinbase
  *
  * <Protocol>
  *
  * 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00
  * 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 .... The first field is Tx Hash(32 bytes). All bits are zero
  *
  * ff ff ff ff                                      .... Output Index ( 4 bytes) . All bits are ones: 0xFFFFFFFF
  *
  * 4d                                               .... coinbase data size. (4d => 77 bytes)
  *                                                       Length of the coinbase data, from 2 to 100 bytes
  *
  * 04 ff ff 00 1d 01 04 45  54 68 65 20 54 69 6d 65
  * 73 20 30 33 2f 4a 61 6e  2f 32 30 30 39 20 43 68
  * 61 6e 63 65 6c 6c 6f 72  20 6f 6e 20 62 72 69 6e
  * 6b 20 6f 66 20 73 65 63  6f 6e 64 20 62 61 69 6c
  * 6f 75 74 20 66 6f 72 20  62 61 6e 6b 73          .... coinbase data
  *                                                  .... ^D��<GS>^A^DEThe Times 03/Jan/2009 Chancellor on brink of
  *                                                  .... second bailout for banks
  *
  * ff ff ff ff                                      .... Sequence Number ( 4 bytes. Set to 0xFFFFFFFF )
  */
object TransactionInputCodec : Codec<TransactionInput> {
  override fun transcode(io : CodecInputOutputStream, obj : TransactionInput? ) : TransactionInput? {

    if (io.isInput) {
      val readNormalTxInput = NormalTransactionInputCodec.transcode(io, null)
      return normalTxToGenerationOrNormal(readNormalTxInput!!)
    } else {
      val normalTxInput = generationOrNormalToNormalTx(obj!!)
      return NormalTransactionInputCodec.transcode(io, normalTxInput)
    }
  }

  private fun normalTxToGenerationOrNormal(normalTxInput: NormalTransactionInput) : TransactionInput {
    if (normalTxInput.isCoinBaseInput()) {
      // Generation Transaction
      // Convert to GenerationTransactionInput
      return GenerationTransactionInput(
        normalTxInput.outputTransactionHash,
        normalTxInput.outputIndex,
        CoinbaseData(normalTxInput.unlockingScript.data),
        normalTxInput.sequenceNumber
      )
    } else {
      return normalTxInput
    }
  }

  private fun generationOrNormalToNormalTx(txInput : TransactionInput) : NormalTransactionInput {
    when {
      txInput is GenerationTransactionInput -> {
        //assert(isGenerationTransaction(txInput))
        // Covert back to NormalTransactionInput
        return NormalTransactionInput(
            txInput.outputTransactionHash,
            txInput.outputIndex,
            UnlockingScript(txInput.coinbaseData.data),
            txInput.sequenceNumber)
      }
      txInput is NormalTransactionInput -> {
        //assert(!isGenerationTransaction(txInput))
        return txInput
      }
      else -> {
        // A transaction input should be either a generation transaction input or a normal transaction input.
        throw AssertionError()
      }
    }
  }
}


/**
  * <Description>
  * Each output spends a certain number of satoshis,
  * placing them under control of anyone who can satisfy the provided pubkey script.
  *
  * <Reference>
  * https://bitcoin.org/en/developer-reference#txout
  *
  * <Protocol>
  *
  *  f0ca052a01000000 ......................... Satoshis (49.99990000 BTC)
  *
  *  19 ....................................... Bytes in pubkey script: 25
  *  | 76 ..................................... OP_DUP
  *  | a9 ..................................... OP_HASH160
  *  | 14 ..................................... Push 20 bytes as data
  *  | | cbc20a7664f2f69e5355aa427045bc15
  *  | | e7c6c772 ............................. PubKey hash
  *  | 88 ..................................... OP_EQUALVERIFY
  *  | ac ..................................... OP_CHECKSIG
  */
object TransactionOutputCodec : Codec<TransactionOutput> {
  override fun transcode(io : CodecInputOutputStream, obj : TransactionOutput? ) : TransactionOutput? {
    val value = Codecs.Int64L.transcode(io, obj?.value)
    val lockingScript = LockingScriptCodec.transcode(io, obj?.lockingScript)

    if (io.isInput) {
      return TransactionOutput(
        value!!,
        lockingScript!!
      )
    }
    return null
  }
}

// TODO : Add a test case
object BlockHeaderCodec : Codec<BlockHeader>{
  override fun transcode(io : CodecInputOutputStream, obj : BlockHeader? ) : BlockHeader? {
    val version        = Codecs.Int32L.transcode(io, obj?.version)
    val hashPrevBlock  = HashCodec.transcode(io, obj?.hashPrevBlock)
    val hashMerkleRoot = HashCodec.transcode(io, obj?.hashMerkleRoot)
    val timestamp      = Codecs.UInt32L.transcode(io, obj?.timestamp)
    val target         = Codecs.UInt32L.transcode(io, obj?.target)
    val nonce          = Codecs.UInt32L.transcode(io, obj?.nonce)

    if (io.isInput) {
      return BlockHeader(
        version!!,
        hashPrevBlock!!,
        hashMerkleRoot!!,
        timestamp!!,
        target!!,
        nonce!!
      )
    }
    return null
  }
}

// TODO : Add a test case
object IPv6AddressCodec : Codec<IPv6Address>{
  val byteArrayLength16 = Codecs.fixedByteBuf(16)
  override fun transcode(io : CodecInputOutputStream, obj : IPv6Address? ) : IPv6Address? {
    val address = byteArrayLength16.transcode(io, obj?.address)

    if (io.isInput) {
      return IPv6Address(
        address!!
      )
    }
    return null
  }
}

object NetworkAddressCodec : Codec<NetworkAddress>{
  override fun transcode(io : CodecInputOutputStream, obj : NetworkAddress? ) : NetworkAddress? {
    val services = Codecs.UInt64L.transcode(io, obj?.services)
    val ipv6     = IPv6AddressCodec.transcode(io, obj?.ipv6)
    // Note, port is encoded with big endian, not little endian
    val port     = Codecs.UInt16.transcode(io, obj?.port)

    if (io.isInput) {
      return NetworkAddress(
        services!!,
        ipv6!!,
        port!!
      )
    }
    return null
  }
}


object NetworkAddressWithTimestampCodec : Codec<NetworkAddressWithTimestamp>{
  override fun transcode(io : CodecInputOutputStream, obj : NetworkAddressWithTimestamp? ) : NetworkAddressWithTimestamp? {
    val timestamp = Codecs.UInt32L.transcode(io, obj?.timestamp)
    val address   = NetworkAddressCodec.transcode(io, obj?.address)

    if (io.isInput) {
      return NetworkAddressWithTimestamp(
        timestamp!!,
        address!!
      )
    }
    return null
  }
}
