package io.scalechain.blockchain.script

import io.scalechain.blockchain.proto.{BlockHeader, Block, Transaction}
import io.scalechain.blockchain.proto.codec.{BlockHeaderCodec, BlockCodec, TransactionCodec}

object HashCalculator {
  def transactionHash(transaction : Transaction ) : Array[Byte] = {
    val serializedBytes = TransactionCodec.serialize(transaction)

    // Run SHA256 twice and reverse bytes.
    val hash = io.scalechain.crypto.HashFunctions.hash256( serializedBytes )

    hash.value
  }

  def blockHeaderHash(blockheader:BlockHeader) : Array[Byte] = {
    val serializedBlockHeader = BlockHeaderCodec.serialize(blockheader)
    val blockHeaderHash = io.scalechain.crypto.HashFunctions.hash256( serializedBlockHeader )

    blockHeaderHash.value
  }
}


