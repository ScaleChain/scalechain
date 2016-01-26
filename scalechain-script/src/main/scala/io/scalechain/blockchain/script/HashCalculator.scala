package io.scalechain.blockchain.script

import java.io.ByteArrayOutputStream
import io.scalechain.blockchain.block.codec.BlockSerializer
import io.scalechain.blockchain.proto.Transaction
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

    hash.value
  }
}


