package io.scalechain.blockchain.chain

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.{ErrorCode, ChainException}
import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.storage.{TransactionLocator, BlockWriter, BlockStorage}
import io.scalechain.blockchain.transaction.ChainBlock
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import io.scalechain.blockchain.script.HashSupported._

/**
  * Created by kangmo on 6/9/16.
  */
class BlockMagnet(storage : BlockStorage, txPool : TransactionPool, txMagnet : TransactionMagnet) {
  private val logger = Logger( LoggerFactory.getLogger(classOf[BlockMagnet]) )

  /**
    * Detach a block from the best blockchain.
    *
    * @param blockInfo The BlockInfo of the block to detach.
    * @param block The block to detach.
    */
  protected[chain] def detachBlock(blockInfo: BlockInfo, block : Block)(implicit db : KeyValueDatabase) : Unit = {
    // Detach each transaction in reverse order.
    // TODO : Optimize : Iterate a list in reverse order without reversing it.
    block.transactions.reverse foreach { transaction : Transaction =>
      txMagnet.detachTransaction(transaction)
    }

    storage.delBlockHashByHeight(blockInfo.height)

    val prevBlockHash = blockInfo.blockHeader.hashPrevBlock
    assert(!prevBlockHash.isAllZero()) // The genesis block can't be detached
    // Unlink the next block hash.
    storage.updateNextBlockHash(prevBlockHash, None)
  }

  /**
    * Detach blocks from the best blockchain. Recent blocks are detached first. (Detach order : Newest -> Oldest )
    *
    * @param beforeFirstHeader Blocks after this block are detached from the best blockchain.
    * @param last The last block in the best blockchain.
    */
  @tailrec
  protected[chain] final def detachBlocksAfter(beforeFirstHeader : BlockHeader, last : BlockInfo, lastBlock : Block, detachedBlocks : ListBuffer[Block])(implicit db : KeyValueDatabase) : Unit = {
    if (beforeFirstHeader == last.blockHeader) {
      // The base case.
    } else {
      // Construct the detachedBlocks so that it has detached blocks. Order : from oldest to newest
      detachedBlocks.prepend(lastBlock)
      detachBlock(last, lastBlock)

      // Get the block before the last one, continue iteration.
      val Some((beforeLastInfo, beforeLastBlock)) = storage.getBlock(last.blockHeader.hashPrevBlock)
      detachBlocksAfter(beforeFirstHeader, beforeLastInfo, beforeLastBlock, detachedBlocks)
    }
  }

  /**
    * Attach a block to the best blockchain.
    *
    * @param block The block to attach
    */
  protected[chain] def attachBlock(blockInfo: BlockInfo, block : Block)(implicit db : KeyValueDatabase) : Unit = {
    val blockHash = block.header.hash
    // Check if the block is valid.
    BlockProcessor.get.validateBlock(block)

    assert(blockInfo.blockHeader == block.header)

    val txLocators : List[TransactionLocator] = BlockWriter.getTxLocators(blockInfo.blockLocatorOption.get, block)

    // TODO : BUGBUG : Optimize : length on List is slow.
    assert(txLocators.length == block.transactions.length)

    // TODO : BUGBUG : P0 : Need to check with a temporary transaction pool.
/*
    // Before attaching a block, check if we can attach each transaction first without affecting the transaction database.
    // If any error such as double spending is detected, an exception is raised.
    for ( ( txLocator : TransactionLocator, transaction: Transaction) <- (txLocators zip block.transactions)) {
      val transactionHash = transaction.hash
      txMagnet.attachTransaction(transactionHash, transaction, Some(txLocator.txLocator), checkOnly = true)
    }
*/
    val chainBlockOption = Some(ChainBlock(blockInfo.height, block))
    val blockHeightOption = Some(blockInfo.height)
    var transactionIndex = -1
    for ( ( txLocator : TransactionLocator, transaction: Transaction) <- (txLocators zip block.transactions)) {
      transactionIndex += 1

      val transactionHash = transaction.hash

      // Step 5 : Remove the transaction from the disk pool.
      txPool.removeTransactionFromPool(transactionHash)
      //logger.trace(s"[Attach Block] Removed transaction from pool : ${transactionHash}")

      txMagnet.attachTransaction(transactionHash, transaction, checkOnly = false, Some(txLocator.txLocator), chainBlockOption, Some(transactionIndex) )
    }

    // TODO : Check if the generation transaction's output amount is less than or equal to the reward + sum of fees for all transactions in the block.

    // Put the index for block hash by height
    storage.putBlockHashByHeight(blockInfo.height, blockHash)

    val prevBlockHash = blockInfo.blockHeader.hashPrevBlock
    if (prevBlockHash.isAllZero()) {
      // the genesis block. do nothing. genesis blocks are not reorganized, but can be attached when the block is put for the first time.
    } else {
      // Link the next block hash.
      storage.updateNextBlockHash(prevBlockHash, Some(blockHash))
    }
  }

  /**
    * collect BlockInfos from the last to the next block of the beforeFirst.
    * The collected BlockInfos are kept in blockInfos List in ascending order. (Order : Oldest -> Newest)
    *
    * @param blockInfos The list buffer to keep the block info.
    * @param beforeFirstHeader Blocks after this block are collected.
    * @param last The last block to collect.
    */
  @tailrec
  protected[chain] final def collectBlockInfos(blockInfos : ListBuffer[BlockInfo], beforeFirstHeader : BlockHeader, last : BlockInfo)(implicit db : KeyValueDatabase) : Unit = {
    // TODO : Need to check if our memory is enough by checking the gap of the height between beforeFirst and last
    if (beforeFirstHeader == last.blockHeader) {
      // The base case. Nothing to do.
    } else {
      // Note that we are constructing the blockInfos so that the order of the blocks in the blockInfos is from the oldest to the newest.
      blockInfos.prepend(last)
      if (last.blockHeader.hashPrevBlock.isAllZero()) {
        logger.error(s"collectBlockInfos : The last block is the genesis block. beforeFirst:${beforeFirstHeader}, last:${last}")
        assert(false)
      }
      val beforeLastOption : Option[BlockInfo] = storage.getBlockInfo(last.blockHeader.hashPrevBlock)
      assert(beforeLastOption.isDefined)
      collectBlockInfos(blockInfos, beforeFirstHeader, beforeLastOption.get)
    }
  }

  /**
    * Attach blocks to the best blockchain. Oldest blocks are attached first. (Attach order : Oldest -> Newest )
    *
    * @param beforeFirstHeader Blocks after this block are attached to the best blockchain.
    * @param last The last block in the new best blockchain.
    */
  protected[chain] def attachBlocksAfter(beforeFirstHeader : BlockHeader, last : BlockInfo)(implicit db : KeyValueDatabase) : Unit = {
    // Blocks NOT in the best blockchain does not have the BlockInfo.nextBlockHash.
    // We need to track from the last back to beforeFirst, and reverse the list.
    val blockInfos = ListBuffer[BlockInfo]()
    collectBlockInfos(blockInfos, beforeFirstHeader, last)

    var prevBlockHash = beforeFirstHeader.hash
    // The previous block of the first block in the list buffer should be the beforeFirst block.
    assert(prevBlockHash == blockInfos.head.blockHeader.hashPrevBlock)
    // The last block info in the list buffer should match the last block info passed to this method.
    assert(last == blockInfos.last)

    blockInfos foreach { blockInfo : BlockInfo =>
      val blockHash = blockInfo.blockHeader.hash
      // Get the block
      val Some((readBlockInfo, readBlock)) = storage.getBlock(blockHash)
      assert(readBlockInfo == blockInfo)

      attachBlock( readBlockInfo, readBlock )

      prevBlockHash = blockHash
    }

    // The last block should not have any next block.
    assert( last.nextBlockHash.isEmpty )
  }


  /** Reorganize blocks.
    * This method is called when the new best block is not based on the original best block.
    *
    * @param originalBestBlock The original best block before the new best one was found.
    * @param newBestBlock The new best block, which has greater chain work than the original best block.
    */
  def reorganize(originalBestBlock : BlockInfo, newBestBlock : BlockInfo)(implicit db : KeyValueDatabase) : Unit = {
    // TODO : BUGBUG : Need to think about RocksDB transactions.

    assert( originalBestBlock.chainWork < newBestBlock.chainWork)

    val detachedBlocks = new ListBuffer[Block]

    // Step 1 : Find the common block(pfork) between the current blockchain(pindexBest) and the new longer blockchain.
    val commonBlockHeader : BlockHeader = findCommonBlock(originalBestBlock, newBestBlock)

    // TODO : Call chainEventListener : onNewBlock, onRemoveBlock

    // Step 2 : Detach blocks after the common block to originalBestBlock, which is the tip of the current best blockchain.
    // TODO : BUGBUG : First need to get the list of detached blocks first, and then add transactions in the detached blocks into the transaction pool.
    val Some((bestBlockInfo, bestBlock )) = storage.getBlock(originalBestBlock.blockHeader.hash)
    assert(bestBlockInfo == originalBestBlock)
    detachBlocksAfter(commonBlockHeader, bestBlockInfo, bestBlock, detachedBlocks)


    // Step 3 : Attach blocks after the common block to the newBestBlock.
    attachBlocksAfter(commonBlockHeader, newBestBlock)

    // Step 4 : Move the transaction from the detached blocks into the transaction pool.
    // Note 1 : Some transactions might not be able to put into the transaction pool, because of double spending the UTXO spent by transactions on newly attached blocks.
    // Note 2 : Some transactions might not be able to put into the transaction pool, because it depends on the above double spending transactions.
    // TODO : Do we really need to reset the transaction record locator? How do we recover it when we attach the transaction again?
    detachedBlocks foreach { block : Block =>
      block.transactions foreach { transaction : Transaction =>
        val transactionHash = transaction.hash
        if (transaction.inputs(0).isCoinBaseInput()) {
          // Do nothing
        } else {
          try {
            if (storage.hasTransaction(transactionHash)) {
              // The transaction already exists.
              // This can happen, as the newly attached block might have the same transaction. Do nothing.
            } else {
              txPool.addTransactionToPool(transactionHash, transaction)
            }
          } catch {
            case e : ChainException => {
              e.code match {
                case ErrorCode.TransactionOutputAlreadySpent => // do nothing. a double spending transaction
                case ErrorCode.ParentTransactionNotFound => // do nothing. these transactions may depend on double spending transactions.
                case _ => throw e
              }
            }
          }
        }
      }
    }

    /*



      // Step 2 : Get the list of blocks to disconnect from the common block to the tip of the current blockchain

      // Step 3 : Get the list of blocks to connect from the common block to the longer blockchain.

      // Step 4 : Reorder the list of blocks to connect so that the blocks with lower height come first.

      // Step 5 : Disconnect blocks from the current (shorter) blockchain. (order : newest to oldest)
      LOOP block := For each block to disconnect
          // Step 5.1 : Read each block, and disconnect each block.
          block.ReadFromDisk(pindex)
          block.DisconnectBlock(txdb, pindex)
              1. Mark all outputs spent by all inputs of all transactions in the block as unspent.
              LOOP tx := For each transaction in the block
                  1.1. Mark outputs pointed by the inputs of the transaction unspent.
                  tx.DisconnectInputs
                      - LOOP input := For each input in the transaction
                          - Get the transaction pointed by the input
                          - On disk, Mark the output point by the input as spent
              2. On disk, disconnect from the previous block
                  (previous block.next = null)

          // Step 5.2 : Prepare transactions to add back to the mempool.
      }

      // Step 6 : Connect blocks from the longer blockchain to the current blockchain. (order : oldest to newest)
      LOOP block := For each block to connect
          // kangmo : comment - Step 6.1 : Read block, connect the block to the current blockchain, which does not have the disconnected blocks.
          block.ReadFromDisk(pindex)
          block.ConnectBlock(txdb, pindex)
              - 1. Do preliminary checks for a block
              pblock->CheckBlock()

              - 2. Prepare a queue for database changes marking outputs spent by all inputs of all transactions in the block.
              map<uint256, CTxIndex> mapQueuedChanges;

              - 3. Populate mapQueuedChanges with transaction outputs marking which transactions are spending each of the outputs.
              LOOP tx := For each transaction in the block
                  - 3.1 Mark transaction outputs pointed by inputs of this transaction spent.
                  IF not coinbase transaction
                      CTransaction::ConnectInputs( .. & mapQueuedChanges .. )
                          LOOP input := for each input in the transaction
                              // 1. read CTxIndex from disk if not read yet.
                              // 2. read the transaction that the outpoint points from disk if not read yet.
                              // 3. Increase DoS score if an invalid output index was found in a transaction input.
                              // 4. check coinbase maturity for outpoints spent by a transaction.
                              // 5. Skip ECDSA signature verification when connecting blocks (fBlock=true) during initial download
                              // 6. check double spends for each OutPoint of each transaction input in a transaction.
                              // 7. check value range of each input and sum of inputs.
                              // 8. for the transaction output pointed by the input, mark this transaction as the spending transaction of the output.

                          // check if the sum of input values is greater than or equal to the sum of outputs.
                          // make sure if the fee is not negative.
                          // check the minimum transaction fee for each transaction.
                  // Add UTXO : set all outputs are unspent for the newly connected transaction.

              - 4. For each items in mapQueuedChanges, write to disk.
              - 5. Check if the generation transaction's output amount is less than or equal to the reward + sum of fees for all transactions in the block.
              - 6. On disk, connect the block from the previous block. (previous block.next = this block)

              - 7. For each transaction, sync with wallet.
              LOOP tx := For each transaction in the block
                  SyncWithWallets
                      - For each registered wallet
                           pwallet->AddToWalletIfInvolvingMe
          // Step 6.2 : Prepare transactions to remove from the mempool
      }

      // Step 7 : Write the hash of the tip block on the best blockchain, commit the db transaction.

      // Step 8 : Set the next block pointer for each connected block. Also, set next block pointer to null for each disconnected block.
      // Note : next pointers of in-memory block index nodes are modified after the on-disk transaction commits the on-disk version of the next pointers.

      // Step 9 : add transactions in the disconnected blocks to the mempool.

      // Step 10 : Remove transactions in the connected blocks from the mempool.
*/
  }

  /** Get the descriptor of the common ancestor of the two given blocks.
    * Because all blocks are on top of the genesis block, this method should at least return the BlockInfo for the genesis block.
    *
    * @param block1 The first given block.
    * @param block2 The second given block.
    */
  @tailrec
  final protected def findCommonBlock(block1 : BlockInfo, block2 : BlockInfo)(implicit db : KeyValueDatabase) : BlockHeader = {
    if ( block1.height < block2.height ) {
      // back track for block2
      assert(!block2.blockHeader.hashPrevBlock.isAllZero())
      val prevBlock2 : BlockInfo = storage.getBlockInfo(block2.blockHeader.hashPrevBlock).get
      findCommonBlock(block1, prevBlock2)
    } else if (block1.height > block2.height) {
      // back track for block1
      assert(!block1.blockHeader.hashPrevBlock.isAllZero())
      val prevBlock1 : BlockInfo = storage.getBlockInfo(block1.blockHeader.hashPrevBlock).get
      findCommonBlock(prevBlock1, block2)
    } else { // block1.height == block2.height
      if (block1.blockHeader == block2.blockHeader) { // the base case : The common block was found
        block1.blockHeader
      } else {
        assert(!block1.blockHeader.hashPrevBlock.isAllZero())
        assert(!block2.blockHeader.hashPrevBlock.isAllZero())
        // the block height is same, but the block header does not match.
        // back track for block1 and block2
        val prevBlock1 : BlockInfo = storage.getBlockInfo(block1.blockHeader.hashPrevBlock).get
        val prevBlock2 : BlockInfo = storage.getBlockInfo(block2.blockHeader.hashPrevBlock).get
        findCommonBlock(prevBlock1, prevBlock2)
      }
    }
  }
}
