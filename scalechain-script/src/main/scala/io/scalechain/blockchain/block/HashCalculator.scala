package io.scalechain.blockchain.block

import java.io.ByteArrayOutputStream
import io.scalechain.io.BlockDataOutputStream

object HashCalculator {
  def transactionHash(transaction : Transaction ) : Array[Byte] = {
    val bout = new ByteArrayOutputStream()
    val dout = new BlockDataOutputStream(bout)

    try {
      val serializer = new BlockSerializer(dout)
      serializer.writeTransaction(transaction)
    } finally {
      dout.close()
    }

    // Run SHA256 twice and reverse bytes.
    val hash = io.scalechain.crypto.HashFunctions.hash256( bout.toByteArray )
    //hash.value.reverse
    // TODO : Check if don't need to reverse hash value
    hash.value
  }
}


