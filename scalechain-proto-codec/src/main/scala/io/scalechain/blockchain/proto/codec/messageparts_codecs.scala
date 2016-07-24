package io.scalechain.blockchain.proto.codec

import java.nio.ByteBuffer

import io.scalechain.blockchain.{ErrorCode, ProtocolCodecException}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive._
import io.scalechain.io.InputOutputStream
import io.scalechain.util.{ArrayUtil, ByteArray, ByteArrayAndVectorConverter}
import org.apache.commons.collections4.map.LRUMap
import scodec.{DecodeResult, Attempt, Codec}
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs._

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

trait SerializeParseBase[T] {
  val codec : Codec[T]

  def serialize(obj : T) : Array[Byte] = {

    val bitVector = codec.encode(obj).require

    val len : Int = bitVector.length.toInt
    // Make sure we have bit length aligned to bytes.
    assert((len & 0x00000007) == 0)
    val byteLen = len >> 3
    //
    val serializedBytes = new Array[Byte](byteLen)

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
        throw new ProtocolCodecException(ErrorCode.EncodeFailure, err.toString)
      }
    }*/
  }

  def parse(data: Array[Byte]) : T = {
    val bitVector: BitVector = BitVector.view(data)

    codec.decode(bitVector) match {
      case Attempt.Successful(DecodeResult(decoded, remainder)) => {
        if ( remainder.isEmpty ) {
          decoded
        } else {
          throw new ProtocolCodecException(ErrorCode.RemainingNotEmptyAfterDecoding)
        }
      }
      case Attempt.Failure(err) => {
        throw new ProtocolCodecException(ErrorCode.DecodeFailure, err.toString)
      }
    }
  }

  @tailrec
  final def parseManyInternal(bitVector:BitVector, decodedItems : ListBuffer[T]) : Unit = {
    codec.decode(bitVector) match {
      case Attempt.Successful(DecodeResult(decoded, remainder)) => {
        decodedItems.append(decoded)
        if ( !remainder.isEmpty ) {
          parseManyInternal(remainder, decodedItems)
          // throw new ProtocolCodecException(ErrorCode.RemainingNotEmptyAfterDecoding)
        }
      }
      case Attempt.Failure(err) => {
        throw new ProtocolCodecException(ErrorCode.DecodeFailure, err.toString)
      }
    }
  }

  def parseMany(data: Array[Byte]) : List[T] = {
    val decodedItems = new ListBuffer[T]()
    val bitVector: BitVector = BitVector.view(data)
    parseManyInternal(bitVector, decodedItems)
    decodedItems.toList
  }
}

case class ParseCacheEntry[T](obj : T, serialized : Array[Byte])

class Cache[T](count : Int) {
  type SerializeCache = LRUMap[T, Array[Byte]]
  type ParseCache = LRUMap[Int, ParseCacheEntry[T]]
  var hitCount : Int = 0
  var missCount : Int = 0
  def parseCacheKey(serializedData : Array[Byte]): Int = {
    (if (serializedData.length >= 1) (serializedData(0).toInt) else 0) +
    (if (serializedData.length >= 2) (serializedData(1).toInt << 8) else 0) +
    (if (serializedData.length >= 3) (serializedData(2).toInt << 16) else 0) +
    (if (serializedData.length >= 3) (serializedData(3).toInt << 24) else 0)
  }
  val serializeCache = new SerializeCache(count)
  val parseCache = new ParseCache(count)

  def put(obj : T, serializedData : Array[Byte]) : Unit = {
    serializeCache.put(obj, serializedData)
    val key = parseCacheKey(serializedData)
    parseCache.put(key, ParseCacheEntry(obj, serializedData))
  }

  /**
    * Get the serialized array of an object from the cache.
    *
    * @param obj The input object.
    * @return A non-null byte array if there is an entry in the cache. Null otherwise.
    */
  def get(obj : T) : Array[Byte] = {
    val cached = serializeCache.get(obj)
    if (cached == null) {
      missCount += 1
    } else {
      hitCount += 1
    }
    cached
  }

  /**
    * Get the deserialized object from a serialized array from the cache.
    *
    * @param serializedData The serialized data.
    * @return A non-null object reference if there is an entry in the cache. Null otherwise.
    */
  def get(serializedData : Array[Byte]) : Option[T] = {
    val key = parseCacheKey(serializedData)
    val candidate = parseCache.get(key)
    if (candidate == null) {
      missCount += 1
      None
    } else {
      if (ArrayUtil.isEqual(serializedData, candidate.serialized)) {
        hitCount += 1
        Some(candidate.obj)
      } else {
        missCount += 1
        None
      }
    }
  }
  override def toString = s"Cache(missCount=${missCount}, hitCount=${hitCount})"
}

object SerializeParseUtil {
  val CacheSize = 1024
}

trait SerializeParseUtil[T] extends SerializeParseBase[T] {
  import SerializeParseUtil._
  val cache = new Cache[T](CacheSize)

  override def serialize(obj : T) : Array[Byte] = {
//    println(s"${obj.getClass.getCanonicalName}"+cache)
    val cached = cache.get(obj)
    if (cached == null) {
      val serialized = super.serialize(obj)
      cache.put(obj, serialized)
      serialized
    } else {
      cached
    }
  }

  override def parse(data: Array[Byte]) : T = {
    val cached = cache.get(data)
    if (cached.isEmpty) {
      val parsed = super.parse(data)
      cache.put(parsed, data)
      parsed
    } else {
      cached.get
    }
  }
}

trait MessagePartCodec[T <: ProtocolMessage] extends SerializeParseUtil[T] {
}

object HashCodec extends MessagePartCodec[Hash] {
  val codec : Codec[Hash] = {
    ("value" | FixedByteArray.reverseCodec(32))
  }.as[Hash]
}

// No need to create a codec for CoinbaseData, as it is converted from/to UnlockingScript by TransactionInputCodec.
/*
object CoinbaseDataCodec extends MessagePartCodec[CoinbaseData] {
  val codec : Codec[CoinbaseData] = {
    ("coinbase_data" | VarByteArray.codec)
  }.as[CoinbaseData]
}
*/

object LockingScriptCodec extends MessagePartCodec[LockingScript] {
  val codec : Codec[LockingScript] = {
    ("locking_script" | VarByteArray.codec)
  }.as[LockingScript]
}

object UnlockingScriptCodec extends MessagePartCodec[UnlockingScript] {
  val codec : Codec[UnlockingScript] = {
    ("unlocking_script" | VarByteArray.codec)
  }.as[UnlockingScript]
}


/**
  * [Description]
  * Each non-coinbase input spends an outpoint from a previous transaction.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#txin
  *
  * [Protocol]
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
object NormalTransactionInputCodec extends MessagePartCodec[NormalTransactionInput] {
  val codec : Codec[NormalTransactionInput] = {
    ( "outpoint_transaction_hash"  | HashCodec.codec            ) ::
    ( "outpoint_transaction_index" | uint32L                    ) ::
    ( "unlocking_script"           | UnlockingScriptCodec.codec ) ::
    ( "sequence_number"            | uint32L                    )
  }.as[NormalTransactionInput]
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
  * [Description]
  * The first transaction in a block, called the coinbase transaction,
  * must have exactly one input, called a coinbase.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#coinbase
  *
  * [Protocol]
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
object TransactionInputCodec extends MessagePartCodec[TransactionInput] {
  /** See if the transaction input data represents the generation transaction input.
    *
    * Generation transaction's UTXO hash has all bits set to zero,
    * and its UTXO index has all bits set to one.
    *
    * @param txInput The transaction input to investigate.
    * @return true if the give transaction input is the generation transaction. false otherwise.
    */
  private def isGenerationTransaction(txInput : TransactionInput) = {
    // BUGBUG : Need to check if outputIndex is 0xFFFFFFFF.
    //println(s"${txInput.outputIndex}")
    txInput.outputTransactionHash.isAllZero() //&& (txInput.outputIndex == -1L)
  }

  private def normalTxToGenerationOrNormal(normalTxInput: NormalTransactionInput) : TransactionInput = {
    if (isGenerationTransaction(normalTxInput)) {
      // Generation Transaction
      // Convert to GenerationTransactionInput
      GenerationTransactionInput(
        normalTxInput.outputTransactionHash,
        normalTxInput.outputIndex,
        CoinbaseData(normalTxInput.unlockingScript.data),
        normalTxInput.sequenceNumber
      )
    } else {
      normalTxInput
    }
  }

  private def generationOrNormalToNormalTx(txInput : TransactionInput) : NormalTransactionInput = {
    txInput match {
      case generationTxInput : GenerationTransactionInput => {
        //assert(isGenerationTransaction(txInput))
        // Covert back to NormalTransactionInput
        NormalTransactionInput(
          generationTxInput.outputTransactionHash,
          generationTxInput.outputIndex,
          UnlockingScript(generationTxInput.coinbaseData.data),
          generationTxInput.sequenceNumber)
      }
      case normalTxInput : NormalTransactionInput => {
        //assert(!isGenerationTransaction(txInput))
        normalTxInput
      }
    }
  }

  val codec : Codec[TransactionInput] = NormalTransactionInputCodec.codec.xmap(
    normalTxToGenerationOrNormal _, generationOrNormalToNormalTx _
  );
}


/**
  * [Description]
  * Each output spends a certain number of satoshis,
  * placing them under control of anyone who can satisfy the provided pubkey script.
  *
  * [Reference]
  * https://bitcoin.org/en/developer-reference#txout
  *
  * [Protocol]
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
object TransactionOutputCodec extends MessagePartCodec[TransactionOutput] {
  val codec : Codec[TransactionOutput] = {
    ("value"          | int64L                   ) ::
    ("locking_script" | LockingScriptCodec.codec )
  }.as[TransactionOutput]
}

// TODO : Add a test case
object BlockHeaderCodec extends MessagePartCodec[BlockHeader]{
  val codec : Codec[BlockHeader] = {
    ("version"               | int32L                                 ) ::
    ("previous_block_hash"   | HashCodec.codec                        ) ::
    ("merkle_root_hash"      | HashCodec.codec                        ) ::
    ("timestamp"             | uint32L                                ) ::
    ("diffculty_target_bits" | uint32L                                ) ::
    ("nonce"                 | uint32L                                )
  }.as[BlockHeader]
}

// TODO : Add a test case
object IPv6AddressCodec extends MessagePartCodec[IPv6Address]{
  val codec : Codec[IPv6Address] = {
    ("address" | FixedByteArray.codec(16) )
  }.as[IPv6Address]
}

object NetworkAddressCodec extends MessagePartCodec[NetworkAddress]{
  val codec : Codec[NetworkAddress] = {
    ("services" | BigIntForLongCodec.codec) ::
    ("ipv6" | IPv6AddressCodec.codec) ::
    ("port" | uint16) // Note, port is encoded with big endian, not little endian
  }.as[NetworkAddress]
}


object NetworkAddressWithTimestampCodec extends MessagePartCodec[NetworkAddressWithTimestamp]{
  val codec : Codec[NetworkAddressWithTimestamp] = {
    ("timestamp" | uint32L) ::
    ("network_address" | NetworkAddressCodec.codec)
  }.as[NetworkAddressWithTimestamp]
}
