package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.storage.BlockIndex

/**
  * Created by kangmo on 3/15/16.
  */
class BlockVerifier(block : Block) {
  def verify(blockIndex : BlockIndex) : Unit = {
    // (1) verify the hash of the block is within the difficulty level
    // TODO : Implement

    // (2) verify each transaction in the block
    block.transactions.map { transaction =>
      new TransactionVerifier(transaction).verify(blockIndex)
    }

    // throw new BlockVerificationException
  }
}
