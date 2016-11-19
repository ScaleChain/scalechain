package io.scalechain.blockchain.proto

/** data classes that are used for keys or values of the block storage index.
  */

data class RecordLocator(val offset : Long, val size : Int) : ProtocolMessage

data class FileRecordLocator(val fileIndex : Int, val recordLocator : RecordLocator) : ProtocolMessage

data class BlockFileInfo(
  val blockCount : Int,
  val fileSize : Long,
  val firstBlockHeight : Long,
  val lastBlockHeight : Long,
  val firstBlockTimestamp : Long,
  val lastBlockTimestamp : Long
) : ProtocolMessage

data class BlockInfo(
  val height : Long,
  // The total (estimated) number of hash calculations from the genesis block.
  val chainWork : Long, // TODO : BUGBUG : Make sure that the 64 bit integer value is enough for the chainwork.
  val nextBlockHash : Hash?,
  val transactionCount : Int,
  val status : Int,
  val blockHeader : BlockHeader,
  val blockLocatorOption : FileRecordLocator?
) : ProtocolMessage

data class FileNumber(
  val fileNumber : Int
) : ProtocolMessage

/** For testing purpose. For testing record files, we need to create a record file which has remaining space.
  * Ex> The file size limit is 12 bytes, but we need to be able to write only 11 bytes.
  */
data class OneByte( val value : Byte ) : ProtocolMessage


/** For converting transation time(long) to a byte array, which is finally converted to a base58 encoded string
  */
data class LongValue( val value : Long ) : ProtocolMessage

/** To get a record locator of each transaction we write while we write a block,
  * We need to write (1) block header (2) transaction count (3) each transaction.
  *
  * This is why we need a separate data class for the transaction count.
  * If we write a block as a whole, we can't get record locators for each transaction in a block.
  *
  * @param count The number of transactions.
  */
data class TransactionCount( val count : Int) : ProtocolMessage

/** To search a block on the best blockchain by height, we need the BlockHeight data class.
  *
  * @param height The height of a block.
  */
data class BlockHeight( val height : Long ) : ProtocolMessage

/** The transaction descriptor kept for each transaction stored in a block.
  * Transactions in the transaction pool don't store transaction descriptors, but they are kept in the transaction pool.
  *
  * @param transactionLocator The file record locator pointing to an on-disk serialized transaction
  * @param blockHeight The height of the block where the transaction belongs to. Whenever blocks are reorganized, transaction descriptors are completely removed and reconstructed, so we can depend on the block height(8 bytes) instead of the block hash(32 bytes).
  * @param outputsSpentBy List of transaction inputs that spends outputs of the transaction.
  *                       For each element of the list, it is Some(inPoint) if an output was spent, None otherwise.
  */
data class TransactionDescriptor( val transactionLocator : FileRecordLocator, val blockHeight : Long, val outputsSpentBy : List<InPoint?> ) : ProtocolMessage


/** A descriptor for an orphan block. Used as the value of the (key:block hash, value:orphan block) index.
  *
  * @param block The orphan block.
  */
data class OrphanBlockDescriptor( val block : Block ) : ProtocolMessage

/** a descriptor for an orphan transaction. Used as the value of the (key:transaction hash, value:orphan transaction) index.
  *
  * @param transaction
  */
data class OrphanTransactionDescriptor(
  val transaction : Transaction
) : ProtocolMessage


/**
  * An entry in the transaction pool. Transactions that are not kept in any block on the best blockchain are kept in the transaction pool.
  *
  * @param transaction The transaction in the transaction pool.
  * @param outputsSpentBy List of transaction inputs that spends outputs of the transaction.
  *                       For each element of the list, it is Some(inPoint) if an output was spent, None otherwise.
  */
data class TransactionPoolEntry(
  val transaction : Transaction,
  val outputsSpentBy : List<InPoint?>,
  val createdAtNanos : Long
) : ProtocolMessage