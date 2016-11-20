package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{FileRecordLocator, BlockInfo, Hash, BlockHeader}
import io.scalechain.crypto.HashEstimation

protected[storage] object BlockInfoFactory {
  /** Create a block information.
    *
    * @param prevBlockInfoOption The block information of the previous block. Pass None for the genesis block.
    * @param blockHash The hash of the current block.
    * @param blockHeader The block header.
    * @param transactionCount the number of transactions in the block.
    * @return The created block descriptor.
    */
  def create(prevBlockInfoOption : Option[BlockInfo], blockHeader : BlockHeader, blockHash : Hash, transactionCount : Int, blockLocatorOption : Option[FileRecordLocator]) : BlockInfo = {

    val prevBlockHeight = prevBlockInfoOption.map(_.height).getOrElse(-1L)
    val prevBlockChainWork = prevBlockInfoOption.map(_.chainWork).getOrElse(0L)

    io.scalechain.blockchain.proto.BlockInfo(
      height = prevBlockHeight + 1L,
      transactionCount = transactionCount,
      chainWork = prevBlockChainWork + HashEstimation.getHashCalculations(blockHash.value.array),
      nextBlockHash = None,
      // BUGBUG : Need to use enumeration
      status = 0,
      blockHeader = blockHeader,
      blockLocatorOption = blockLocatorOption
    )
  }
}
