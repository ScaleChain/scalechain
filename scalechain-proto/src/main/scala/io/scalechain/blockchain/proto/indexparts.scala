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


