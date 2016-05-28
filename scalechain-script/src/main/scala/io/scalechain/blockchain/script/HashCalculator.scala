package io.scalechain.blockchain.script

import io.scalechain.blockchain.proto.{Hash, BlockHeader, Block, Transaction}
import io.scalechain.blockchain.proto.codec.{BlockHeaderCodec, BlockCodec, TransactionCodec}

object HashCalculator {
  def transactionHash(transaction : Transaction ) : Hash = {
    val serializedBytes = TransactionCodec.serialize(transaction)

    // Run SHA256 twice and reverse bytes.
    val hash = io.scalechain.crypto.HashFunctions.hash256( serializedBytes )

    Hash( hash.value.reverse )
  }

  def blockHeaderHash(blockheader:BlockHeader) : Hash = {
    val serializedBlockHeader = BlockHeaderCodec.serialize(blockheader)

    // Run SHA256 twice and reverse bytes.
    val blockHeaderHash = io.scalechain.crypto.HashFunctions.hash256( serializedBlockHeader )

    Hash( blockHeaderHash.value.reverse )
  }
}


