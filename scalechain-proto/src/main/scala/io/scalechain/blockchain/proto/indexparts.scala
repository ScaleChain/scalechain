package io.scalechain.blockchain.proto

import io.scalechain.util.ByteArray

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

/** To get a record locator of each transaction we write while we write a block,
  * We need to write (1) block header (2) transaction count (3) each transaction.
  *
  * This is why we need a separate case class for the transaction count.
  * If we write a block as a whole, we can't get record locators for each transaction in a block.
  *
  * @param count The number of transactions.
  */
case class TransactionCount( count : Int) extends ProtocolMessage

/**
  * by mijeong
  *
  * TODO : Implement full WalletInfo
  *
  * WalletInfo Prototype
  */
case class WalletInfo(
  status : Int,
  walletHeader : WalletHeader
) extends ProtocolMessage

/**
  * by mijeong
  *
  * @param account account name
  * @param purpose purpose of address (received or unknown)
  * @param publicKey public key
  * @param privateKeyLocator private key locator in record
  */
case class AddressInfo(
  account : String,
  purpose : Int,
  publicKey : ByteArray,
  privateKeyLocator : Option[FileRecordLocator]
) extends ProtocolMessage

/**
  * by mijeong
  *
  * @param address address
  */
case class AddressKey(
  address : String
) extends ProtocolMessage

/**
  * by mijeong
  *
  * @param account account name
  */
case class AccountInfo(
  account : String
) extends ProtocolMessage


