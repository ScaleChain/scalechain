package io.scalechain.blockchain.script

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.BlockHeader
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.codec.BlockHeaderCodec
import io.scalechain.blockchain.proto.codec.BlockCodec
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.crypto.HashFunctions
import io.scalechain.util.toByteArray
import io.scalechain.util.Bytes

fun Transaction.hash() : Hash {
  val io = CodecInputOutputStream(Unpooled.buffer(), isInput = false)
  TransactionCodec.transcode(io, this)

  // Run SHA256 twice and reverse bytes.
  assert(io.byteBuf.hasArray())
  val hash = HashFunctions.hash256( io.byteBuf.toByteArray() )

  // BUGBUG : Rethink if using ByteBuf in Hash is a correct apporach.
  return Hash(Bytes(hash.value.array.reversed().toByteArray()))
}

fun BlockHeader.hash() : Hash {
  val io = CodecInputOutputStream(Unpooled.buffer(), isInput = false)
  BlockHeaderCodec.transcode(io, this)

  // Run SHA256 twice and reverse bytes.
  assert(io.byteBuf.hasArray())
  val hash = HashFunctions.hash256( io.byteBuf.toByteArray() )

  return Hash(Bytes(hash.value.array.reversed().toByteArray()))
}

/*
object HashSupported {
  implicit fun toHashSupportedBlockHeader(blockHeader: BlockHeader) = HashSupportedBlockHeader(blockHeader)
  implicit fun toHashSupportedTransaction(transaction : Transaction) = HashSupportedTransaction(transaction)
}

data class HashSupportedTransaction(transaction:Transaction)  {
  fun hash() {
    HashCalculator.transactionHash(transaction)
  }
}

data class HashSupportedBlockHeader(blockHeader:BlockHeader)  {
  fun hash() {
    HashCalculator.blockHeaderHash(blockHeader)
  }
}
*/
