package io.scalechain.blockchain.block

import java.io.ByteArrayOutputStream

import io.scalechain.util.Hash

object HashCalculator {
  def transactionHash(transaction : Transaction ) : Array[Byte] = {
    val bout = new ByteArrayOutputStream()
    val dout = new BlockDataOutputStream(bout)
    val serializer = new BlockSerializer(dout)
    serializer.writeTransaction(transaction)

    // Run SHA256 twice and reverse bytes.
    val hash = Hash.hash256( bout.toByteArray )
    hash.value.reverse
  }
}


