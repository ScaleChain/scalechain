package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto.walletparts.{WalletTransactionDetail, WalletTransactionHeader, Address, AccountHeader}
import io.scalechain.blockchain.{ErrorCode, ProtocolCodecException}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive._
import scodec.{DecodeResult, Attempt, Codec}
import scodec.bits.{BitVector}
import scodec.codecs._

trait MessagePartCodec[T <: ProtocolMessage] {
  val codec : Codec[T]

  def serialize(obj : T) : Array[Byte] = {
    codec.encode(obj) match {
      case Attempt.Successful(bitVector) => {
        bitVector.toByteArray
      }
      case Attempt.Failure(err) => {
        throw new ProtocolCodecException(ErrorCode.EncodeFailure, err.toString)
      }
    }

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
}

object HashCodec extends MessagePartCodec[Hash] {
  val codec : Codec[Hash] = {
    ("hash" | FixedByteArray.reverseCodec(32))
  }.as[Hash]
}

/**
  * by mijeong
  *
  * WalletHashCodec Prototype
  */
object WalletHashCodec extends MessagePartCodec[Hash] {
  val codec : Codec[Hash] = {
    ("hash" | FixedByteArray.codec(33))
  }.as[Hash]
}

object BlockHashCodec extends MessagePartCodec[BlockHash]{
  val codec : Codec[BlockHash] = {
    ("block_hash" | FixedByteArray.reverseCodec(32))
  }.as[BlockHash]
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

object MerkleRootHashCodec extends MessagePartCodec[MerkleRootHash] {
  val codec : Codec[MerkleRootHash] = {
    ("merkle_root_hash" | FixedByteArray.reverseCodec(32))
  }.as[MerkleRootHash]
}


object TransactionHashCodec extends MessagePartCodec[TransactionHash] {
  val codec: Codec[TransactionHash] = {
    ("transaction_hash" | FixedByteArray.reverseCodec(32))
  }.as[TransactionHash]
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
    ( "outpoint_transaction_hash"  | TransactionHashCodec.codec ) ::
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
        assert(isGenerationTransaction(txInput))
        // Covert back to NormalTransactionInput
        NormalTransactionInput(
          generationTxInput.outputTransactionHash,
          generationTxInput.outputIndex,
          UnlockingScript(generationTxInput.coinbaseData.data),
          generationTxInput.sequenceNumber)
      }
      case normalTxInput : NormalTransactionInput => {
        assert(!isGenerationTransaction(txInput))
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
    ("previous_block_hash"   | BlockHashCodec.codec                   ) ::
    ("merkle_root_hash"      | MerkleRootHashCodec.codec              ) ::
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
    ("services" | BigIntCodec.codec) ::
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


/**
  * by mijeong
  *
  * TODO: Implement full WalletHeaderCodec
  *
  * WalletHeaderCodec Prototype
  */
object WalletHeaderCodec extends MessagePartCodec[WalletHeader]{
  val codec : Codec[WalletHeader] = {
      ("lsn"                   | int64L                                 ) ::
      ("pgno"                  | int32L                                 ) ::
      ("magic"                 | int32L                                 ) ::
      ("version"               | int32L                                 ) ::
      ("pagesize"              | int32L                                 ) ::
      ("unused1"               | int8L                                  ) ::
      ("type"                  | int8L                                  ) ::
      ("unused2"               | int8L                                  ) ::
      ("free"                  | int32L                                 ) ::
      ("alloc_lsn"             | int64L                                 ) ::
      ("key_count"             | int32L                                 ) ::
      ("record_count"          | int32L                                 ) ::
      ("flags"                 | int32L                                 ) ::
      ("uid"                   | int8L                                  )
  }.as[WalletHeader]
}

/**
  * by mijeong
  *
  * KeyPaireCodec Prototype
  */
object KeyPairCodec extends MessagePartCodec[KeyPair]{
  val codec : Codec[KeyPair] = {
      ("version"                 | int32L                                 ) ::
      ("publickey"               | WalletHashCodec.codec                  ) ::
      ("privatekey"              | HashCodec.codec                        )
  }.as[KeyPair]
}

/**
  * by mijeong
  *
  * AccountHeaderCodec
  *
  * [Protocol]
  *
  *  01 00 00 00 ................................. version: 1
  *  12 33 54 02 ................................. timestamp
  */
object AccountHeaderCodec extends MessagePartCodec[AccountHeader]{
  val codec : Codec[AccountHeader] = {
    ("version"                 | int32L                                 ) ::
    ("timestamp"               | uint32L                                )
  }.as[AccountHeader]
}

/**
  * by mijeong
  *
  * AddressCodec
  *
  * [Protocol]
  *
  *  d8 b7 ee 13 19 be 14 a5 e2 b0 b8 9d 5c 3b a9 d3
  *  df c8 20 e5 94 4a e7 30 e9 f1 87 5f a2 33 55 f9 .... private key
  *  02 10 a8 16 7c 75 7b 3f 62 77 50 6e d0 16 eb db
  *  cc 10 03 eb 62 f7 2b 37 8e 05 77 62 87 86 3d b2
  *  d7 ................................................. public key
  *  00 00 00 01 ........................................ purpose
  *  22 ................................................. address number of bytes: 34
  *  31 32 6e 39 50 52 71 64 59 51 70 39 44 50 47 56
  *  39 79 67 68 62 4a 48 73 6f 4d 5a 63 64 35 78 63
  *  75 6d .............................................. address
  */
object AddressCodec extends MessagePartCodec[Address]{
  val codec : Codec[Address] = {
    ("privatekey"              | FixedByteArray.codec(32)                     ) ::
    ("publickey"               | FixedByteArray.codec(33)                     ) ::
    ("purpose"                 | int32L                                       ) ::
    ("address"                 | VarStr.codec                                 )
  }.as[Address]
}

object WalletTransactionHeaderCodec extends MessagePartCodec[WalletTransactionHeader] {
  val codec : Codec[WalletTransactionHeader] = {
    ("version"             | int32L                     ) ::
    ("height"              | int32L                     )
  }.as[WalletTransactionHeader]
}

object WalletTransactionDetailCodec extends MessagePartCodec[WalletTransactionDetail] {
  val codec : Codec[WalletTransactionDetail] = {
    ("account"                      | VarStr.codec                           ) ::
    ("address"                      | VarStr.codec                           ) ::
    ("toAddress"                    | VarStr.codec                           ) ::
    ("category"                     | int32L                                 ) ::
    ("amount"                       | int64L                                 ) ::
    ("vout"                         | int8                                   ) ::
    ("fee"                          | int64L                                 ) ::
    ("confirmations"                | int32                                  ) ::
    ("generated"                    | int8                                   ) ::
    ("blockHash"                    | FixedByteArray.reverseCodec(32)        ) ::
    ("blockIndex"                   | int32L                                 ) ::
    ("blockTime"                    | int32L                                 ) ::
    ("time"                         | int32L                                 ) ::
    ("comment"                      | VarStr.codec                           ) ::
    ("toComment"                    | VarStr.codec                           )
  }.as[WalletTransactionDetail]
}