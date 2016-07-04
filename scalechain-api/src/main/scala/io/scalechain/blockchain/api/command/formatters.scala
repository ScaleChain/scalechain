package io.scalechain.blockchain.api.command

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.api.command.blockchain.GetBlockResult
import io.scalechain.blockchain.api.command.rawtx._
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{TransactionCodec, BlockCodec}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.util.{ByteArray, HexUtil}
import org.slf4j.LoggerFactory


// [API layer] Convert a block to a specific block format.
object BlockFormatter {
  /** Get the GetBlockResult case class instance from a block.
    *
    * Used by : getblock RPC.
    *
    * @param block The block to format.
    * @return The GetBlockResult instance.
    */
  def getBlockResult(blockInfo : BlockInfo, block : Block) : GetBlockResult = {
    val serializedBlock = BlockCodec.serialize(block)

    val blockHash = block.header.hash

    val txHashes = block.transactions.map { tx => tx.hash }

    GetBlockResult(
      hash = blockHash,
      size = serializedBlock.length,
      height = blockInfo.height,
      version = block.header.version,
      merkleroot = Hash(block.header.hashMerkleRoot.value),
      tx = txHashes,
      time = block.header.timestamp,
      nonce = block.header.nonce,
      previousblockhash = Some(Hash(block.header.hashPrevBlock.value))
    )
  }

  /** Get a serialized block data.
    *
    * Used by : getblock RPC.
    *
    * @param block The block to format.
    * @return The serialized string value.
    */
  def getSerializedBlock(block : Block) : String = {
    val rawBlockData : Array[Byte] = BlockCodec.serialize(block)
    HexUtil.hex(rawBlockData)
  }
}

// [API Layer] decode a transaction.
object TransactionDecoder {
  /** Decodes a serialized transaction hex string into a Transaction.
    *
    * Used by : decoderawtransaction RPC.
    * Used by : sendrawtransaction RPC.
    *
    * @param serializedTransaction The serialized transaction.
    * @return The decoded transaction, Transaction instance.
    */
  def decodeTransaction(serializedTransaction : String) : Transaction = {
    val rawTransaction = HexUtil.bytes(serializedTransaction)
    TransactionCodec.parse(rawTransaction)
  }

  /** Decodes multiple transactions from a hex string.
    *
    * Note : This method uses parseMany, which is
    *
    * @param serializedTransactions The hex string that has multiple(or single) transactions to decode.
    * @return A list of transactions.
    */
  def decodeTransactions(serializedTransactions : String) : List[Transaction] = {
    val rawTransactions = HexUtil.bytes(serializedTransactions)
    TransactionCodec.parseMany(rawTransactions)
  }

}

// [API Layer] encode a transaction
object TransactionEncoder {
  /** Encodes a transaction into a serialized transaction hex string.
    *
    * Used by : signrawtransaction RPC.
    *
    * @param transaction The transaction to encode.
    * @return The serialized transaction.
    */
  def encodeTransaction(transaction : Transaction) : Array[Byte] = {
    TransactionCodec.serialize(transaction)
  }
}


// [API Layer] decode a transaction.
object BlockDecoder {
  /** Decodes a serialized block hex string into a Block.
    *
    * Used by : submitblock RPC.
    *
    * @param serializedBlock The serialized block.
    * @return The decoded block, Block instance.
    */
  def decodeBlock(serializedBlock : String) : Block = {
    val rawBlock = HexUtil.bytes(serializedBlock)
    BlockCodec.parse(rawBlock)
  }
}


// [API layer] Convert a transaction into a specific transaction format.
object TransactionFormatter {
  private lazy val logger = Logger( LoggerFactory.getLogger(TransactionFormatter.getClass) )
  /** Get a serialized version of a transaction.
    *
    * Used by : sign raw transaction
    */
  def getSerializedTranasction(transaction : Transaction) : String = {
    val rawTransactionData : Array[Byte] = TransactionCodec.serialize(transaction)
    HexUtil.hex(rawTransactionData)
  }


  protected[command] def convertTransactionInputs( inputs : List[TransactionInput] ) : List[RawTransactionInput] = {
    inputs.map { input =>
      input match {
        case in : NormalTransactionInput => {
          assert( in.outputIndex < Integer.MAX_VALUE)

          RawNormalTransactionInput(
            txid      = Hash( in.outputTransactionHash.value ),
            vout      = in.outputIndex.toInt,
            scriptSig = RawScriptSig( ByteArray.byteArrayToString(in.unlockingScript.data) ),
            sequence  = in.sequenceNumber
          )

        }
        case in : GenerationTransactionInput => {
          RawGenerationTransactionInput(
            coinbase  = ByteArray.byteArrayToString(in.coinbaseData.data),
            sequence  = in.sequenceNumber
          )
        }
      }
    }
  }

  protected[command] def convertTransactionOutputs(outputs : List[TransactionOutput]) : List[RawTransactionOutput] = {
    var outputIndex = -1 // Because we add 1 to the outputIndex, we set it to -1 to start from 0.
    val rawTxOutputs = outputs.map { output =>
        outputIndex += 1
        RawTransactionOutput(
          value        = output.value,
          n            = outputIndex,
          scriptPubKey = RawScriptPubKey(
            ByteArray.byteArrayToString(output.lockingScript.data)
          )
        )
      }
    rawTxOutputs
  }

  /** Convert a transaction to a RawTransaction instance.
    *
    * Used by getrawtransaction RPC.
    *
    * @param transaction The transaction to convert.
    * @param bestBlockHeight The height of the best block. Used for calculating block confirmations.
    * @param blockInfoOption Some(blockInfo) if the transaction is included in a block; None otherwise.
    * @return The converted RawTransaction instance.
    */
  def getRawTransaction(transaction : Transaction, bestBlockHeight : Long, blockInfoOption : Option[BlockInfo]) : RawTransaction = {
    val serializedTransaction = getSerializedTranasction(transaction)

    val confirmations =
      if (blockInfoOption.isDefined) {
        if (bestBlockHeight >= blockInfoOption.get.height) {
          // If the block is the best block, we can say, 1 confirmation.
          bestBlockHeight - blockInfoOption.get.height + 1L
        } else {
          logger.error(s"The best block height(${bestBlockHeight}) is less than the block height${blockInfoOption.get.height} which has a transaction.")
          assert(false)
          0L
        }
      } else {
        0L
      }

    RawTransaction(
      hex      = serializedTransaction,
      txid     = transaction.hash,
      version  = transaction.version,
      locktime = transaction.lockTime,
      vin      = convertTransactionInputs(transaction.inputs),
      vout     = convertTransactionOutputs(transaction.outputs),
      blockhash  = blockInfoOption.map(_.blockHeader.hash),
      confirmations = confirmations,
      time     = blockInfoOption.map(_.blockHeader.timestamp),
      blocktime     = blockInfoOption.map(_.blockHeader.timestamp)
    )
  }

  /** Convert a transaction to DecodedRawTransaction.
    *
    * Used by : decoderawtransaction RPC.
    *
    * @param transaction The transaction to convert.
    * @return The converted DecodedRawTransaction instance.
    */
  def getDecodedRawTransaction(transaction : Transaction) : DecodedRawTransaction = {
    DecodedRawTransaction(
      txid     = transaction.hash,
      version  = transaction.version,
      locktime = transaction.lockTime,
      vin      = convertTransactionInputs(transaction.inputs),
      vout     = convertTransactionOutputs(transaction.outputs)
    )
  }
}
