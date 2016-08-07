package io.scalechain.blockchain.proto

/** case classes that are used for keys or values of the block storage index.
  */

case class RecordLocator(offset : Long, size : Int) extends ProtocolMessage

case class FileRecordLocator(fileIndex : Int, recordLocator : RecordLocator) extends ProtocolMessage

case class BlockFileInfo(
  blockCount : Int,
  fileSize : Long,
  firstBlockHeight : Long,
  lastBlockHeight : Long,
  firstBlockTimestamp : Long,
  lastBlockTimestamp : Long
) extends ProtocolMessage

case class BlockInfo(
  height : Long,
  // The total (estimated) number of hash calculations from the genesis block.
  chainWork : Long, // TODO : BUGBUG : Make sure that the 64 bit integer value is enough for the chainwork.
  nextBlockHash : Option[Hash],
  transactionCount : Int,
  status : Int,
  blockHeader : BlockHeader,
  blockLocatorOption : Option[FileRecordLocator]
) extends ProtocolMessage

case class FileNumber(
  fileNumber : Int
) extends ProtocolMessage

/** For testing purpose. For testing record files, we need to create a record file which has remaining space.
  * Ex> The file size limit is 12 bytes, but we need to be able to write only 11 bytes.
  */
case class OneByte( value : Byte ) extends ProtocolMessage


/** For converting transation time(long) to a byte array, which is finally converted to a base58 encoded string
  */
case class LongValue( value : Long ) extends ProtocolMessage

/** To get a record locator of each transaction we write while we write a block,
  * We need to write (1) block header (2) transaction count (3) each transaction.
  *
  * This is why we need a separate case class for the transaction count.
  * If we write a block as a whole, we can't get record locators for each transaction in a block.
  *
  * @param count The number of transactions.
  */
case class TransactionCount( count : Int) extends ProtocolMessage

/** To search a block on the best blockchain by height, we need the BlockHeight case class.
  *
  * @param height The height of a block.
  */
case class BlockHeight( height : Long ) extends ProtocolMessage

/** The transaction descriptor kept for each transaction stored in a block.
  * Transactions in the transaction pool don't store transaction descriptors, but they are kept in the transaction pool.
  *
  * @param transactionLocator The file record locator pointing to an on-disk serialized transaction
  * @param blockHeight The height of the block where the transaction belongs to. Whenever blocks are reorganized, transaction descriptors are completely removed and reconstructed, so we can depend on the block height(8 bytes) instead of the block hash(32 bytes).
  * @param outputsSpentBy List of transaction inputs that spends outputs of the transaction.
  *                       For each element of the list, it is Some(inPoint) if an output was spent, None otherwise.
  */
case class TransactionDescriptor( transactionLocator : FileRecordLocator, blockHeight : Long, outputsSpentBy : List[Option[InPoint]] ) extends ProtocolMessage


/** A descriptor for an orphan block. Used as the value of the (key:block hash, value:orphan block) index.
  *
  * @param block The orphan block.
  */
case class OrphanBlockDescriptor( block : Block ) extends ProtocolMessage

/** a descriptor for an orphan transaction. Used as the value of the (key:transaction hash, value:orphan transaction) index.
  *
  * @param transaction
  */
case class OrphanTransactionDescriptor(
  transaction : Transaction
) extends ProtocolMessage


/**
  * An entry in the transaction pool. Transactions that are not kept in any block on the best blockchain are kept in the transaction pool.
  *
  * @param transaction The transaction in the transaction pool.
  * @param outputsSpentBy List of transaction inputs that spends outputs of the transaction.
  *                       For each element of the list, it is Some(inPoint) if an output was spent, None otherwise.
  */
case class TransactionPoolEntry(
  transaction : Transaction,
  outputsSpentBy : List[Option[InPoint]]
) extends ProtocolMessage