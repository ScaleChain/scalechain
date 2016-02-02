package io.scalechain.blockchain.script

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.codec.TransactionCodec

object HashCalculator {
  def transactionHash(transaction : Transaction ) : Array[Byte] = {
    val serializedBytes = TransactionCodec.serialize(transaction)

    // Run SHA256 twice and reverse bytes.
    val hash = io.scalechain.crypto.HashFunctions.hash256( serializedBytes )

    hash.value
  }
}


