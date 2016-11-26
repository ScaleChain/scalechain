package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.FileRecordLocator
import io.scalechain.blockchain.proto.BlockInfo
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.BlockHeader

import io.scalechain.crypto.HashEstimation

internal object BlockInfoFactory {
  /** Create a block information.
    *
    * @param prevBlockInfoOption The block information of the previous block. Pass None for the genesis block.
    * @param blockHash The hash of the current block.
    * @param blockHeader The block header.
    * @param transactionCount the number of transactions in the block.
    * @return The created block descriptor.
    */
  fun create(prevBlockInfoOption : BlockInfo?, blockHeader : BlockHeader, blockHash : Hash, transactionCount : Int, blockLocatorOption : FileRecordLocator?) : BlockInfo {

    val prevBlockHeight = prevBlockInfoOption?.height ?: -1L
    val prevBlockChainWork = prevBlockInfoOption?.chainWork ?: 0L

    return BlockInfo(
      height = prevBlockHeight + 1L,
      transactionCount = transactionCount,
      chainWork = prevBlockChainWork + HashEstimation.getHashCalculations(blockHash.value),
      nextBlockHash = null,
      // BUGBUG : Need to use enumeration
      status = 0,
      blockHeader = blockHeader,
      blockLocatorOption = blockLocatorOption
    )
  }
}
