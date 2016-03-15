package io.scalechain.blockchain.script

import io.scalechain.blockchain.proto.{WalletHeader, BlockHeader, Block, Transaction}
import io.scalechain.blockchain.proto.codec.{WalletHeaderCodec, BlockHeaderCodec, BlockCodec, TransactionCodec}

object HashCalculator {
  def transactionHash(transaction : Transaction ) : Array[Byte] = {
    val serializedBytes = TransactionCodec.serialize(transaction)

    // Run SHA256 twice and reverse bytes.
    val hash = io.scalechain.crypto.HashFunctions.hash256( serializedBytes )

    hash.value.reverse
  }

  def blockHeaderHash(blockheader:BlockHeader) : Array[Byte] = {
    val serializedBlockHeader = BlockHeaderCodec.serialize(blockheader)

    // Run SHA256 twice and reverse bytes.
    val blockHeaderHash = io.scalechain.crypto.HashFunctions.hash256( serializedBlockHeader )

    blockHeaderHash.value.reverse
  }

  /**
    * by mijeong
    *
    * walletHeaderHash Prototype
    */
  def walletHeaderHash(walletHeader:WalletHeader) : Array[Byte] = {
    val serializedWalletHeader = WalletHeaderCodec.serialize(walletHeader)

    // Run SHA256 twice and reverse bytes.
    val walletHeaderHash = io.scalechain.crypto.HashFunctions.hash256( serializedWalletHeader )

    walletHeaderHash.value.reverse
  }
}


