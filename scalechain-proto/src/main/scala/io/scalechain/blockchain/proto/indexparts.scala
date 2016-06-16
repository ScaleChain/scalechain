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
  height : Int,
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

/** The transaction descriptor kept for each transaction.
  * ScaleChain does not use mempool, but keeps transactions on rocksdb.
  * So, if a transaction was put input a block on the best blockchain, we keep the file record locator of the transaction stored on disk.
  * If a transaction was not put into a block on the best blockchain, we keep the serialized transaction as part of a value in rocksdb.
  *
  * @param transactionLocatorOption Some(file record locator) pointing to an on-disk serialized transaction in a block if the transaction is stored in a block. None if it is stored in the on-disk transaction pool.
  * @param outputsSpentBy List of transaction inputs that spends outputs of the transaction.
  *                       For each element of the list, it is Some(inPoint) if an output was spent, None otherwise.
  */
case class TransactionDescriptor( transactionLocatorOption : Option[FileRecordLocator], outputsSpentBy : List[Option[InPoint]] ) extends ProtocolMessage


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