package io.scalechain.blockchain.proto.codec

import io.scalechain.blockchain.proto._
import io.scalechain.io.InputOutputStream
import scodec.Codec


trait MessagePartCodec[T] {
  val codec : Codec[T]
}

object BlockHashCodec extends MessagePartCodec[BlockHash]{
  // TODO : Implement
  val codec : Codec[BlockHash] = null
}

object CoinbaseDataCodec extends MessagePartCodec[CoinbaseData] {
  // TODO : Implement
  val codec : Codec[CoinbaseData] = null
}

object LockingScriptCodec extends MessagePartCodec[LockingScript] {
  // TODO : Implement
  val codec : Codec[LockingScript] = null
}

object MerkleRootHashCodec extends MessagePartCodec[MerkleRootHash] {
  // TODO : Implement
  val codec : Codec[MerkleRootHash] = null
}


object TransactionHashCodec extends MessagePartCodec[TransactionHash] {
  // TODO : Implement
  val codec : Codec[TransactionHash] = null
}


object UnlockingScriptCodec extends MessagePartCodec[UnlockingScript] {
  // TODO : Implement
  val codec : Codec[UnlockingScript] = null
}

/**
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
object GenerationTransactionInputCodec extends MessagePartCodec[GenerationTransactionInput] {
  // TODO : Implement
  val codec : Codec[GenerationTransactionInput] = null
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
  // TODO : Implement
  val codec : Codec[NormalTransactionInput] = null
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
  // TODO : Implement
  val codec : Codec[TransactionOutput] = null
}
