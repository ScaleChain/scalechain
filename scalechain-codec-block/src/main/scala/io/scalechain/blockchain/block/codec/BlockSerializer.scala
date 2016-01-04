package io.scalechain.blockchain.block.codec

import io.scalechain.blockchain.block._
import io.scalechain.io.BlockDataOutputStream

/**
 * Created by kangmo on 11/15/15.
 */
class BlockSerializer(val stream : BlockDataOutputStream) {
  def serialize(block : Block) : Unit = {
    val magic = 0xD9B4BEF9

    stream.writeLittleEndianInt(magic)

    stream.writeLittleEndianInt(block.size.toInt)
    writeBlockHeader(block.header)
    writeTransactions(block.transactions)
  }

  def writeBlockHeader(blockHeader : BlockHeader) : Unit = {
    stream.writeLittleEndianInt(blockHeader.version)
    stream.writeBytes(blockHeader.hashPrevBlock.hash)
    stream.writeBytes(blockHeader.hashMerkleRoot.hash)
    stream.writeLittleEndianInt(blockHeader.time.unixTimestamp)
    stream.writeLittleEndianInt(blockHeader.target)
    stream.writeLittleEndianInt(blockHeader.nonce)
  }

  def writeTransactions(transactions : Array[Transaction]) : Unit = {
    stream.writeVarInt(transactions.length)
    for ( tx : Transaction <- transactions ) {
      writeTransaction(tx)
    }
  }


  def writeTransaction(transaction : Transaction) : Unit = {
    stream.writeLittleEndianInt(transaction.version)
    writeTransactionInputs(transaction.inputs)
    writeTransactionOutputs(transaction.outputs)
    stream.writeLittleEndianInt(transaction.lockTime)
  }

  def writeTransactionInputs(transactionInputs : Array[TransactionInput]) : Unit = {
    stream.writeVarInt(transactionInputs.length)

    for( txInput : TransactionInput <- transactionInputs) {
      writeTransactionInput(txInput)
    }
  }

  def writeTransactionOutputs(transactionOutputs : Array[TransactionOutput]) : Unit = {
    val outputCount = stream.writeVarInt(transactionOutputs.length)
    for( txOutput : TransactionOutput <- transactionOutputs ) {
      writeTransactionOutput(txOutput)
    }
  }

  def writeTransactionInput(transactionInput : TransactionInput) : Unit = {
    transactionInput match {
      case tx : GenerationTransactionInput => {
        assert( tx.outputTransactionHash.isAllZero() )
        writeGenerationTranasctionInput(tx)
      }
      case tx : NormalTransactionInput => {
        writeNormalTransactionInput(tx)
      }
      case _ => {
        assert(false);
      }
    }
  }

  def writeGenerationTranasctionInput(transactionInput : GenerationTransactionInput) : Unit = {
    stream.writeBytes(transactionInput.outputTransactionHash.hash)

    stream.writeLittleEndianInt(transactionInput.outputIndex)
    stream.writeVarInt(transactionInput.coinbaseData.data.length)
    stream.writeBytes(transactionInput.coinbaseData.data)
    stream.writeLittleEndianInt(transactionInput.sequenceNumber)
  }

  def writeNormalTransactionInput(transactionInput : NormalTransactionInput) : Unit = {
    stream.writeBytes(transactionInput.outputTransactionHash.hash)

    stream.writeLittleEndianInt(transactionInput.outputIndex)
    writeUnlockingScript(transactionInput.unlockingScript)
    stream.writeLittleEndianInt(transactionInput.sequenceNumber)
  }

  def writeTransactionOutput(transactionOutput : TransactionOutput) : Unit = {
    stream.writeLittleEndianLong(transactionOutput.value)
    writeLockingScript(transactionOutput.lockingScript)
  }

  def writeLockingScript(lockingScript : LockingScript ) : Unit = {
    stream.writeVarInt(lockingScript.data.length)
    stream.writeBytes(lockingScript.data)
  }

  def writeUnlockingScript(unlockingScript : UnlockingScript ) : Unit = {
    stream.writeVarInt(unlockingScript.data.length)
    stream.writeBytes(unlockingScript.data)
  }

}
