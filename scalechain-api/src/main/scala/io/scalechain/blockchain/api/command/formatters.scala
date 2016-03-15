package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.command.blockchain.GetBlockResult
import io.scalechain.blockchain.api.command.rawtx._
import io.scalechain.blockchain.api.command.wallet.{TransactionItem}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{TransactionCodec, BlockCodec}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.util.{ByteArray, HexUtil}


// [API layer] Convert a block to a specific block format.
object BlockFormatter {
  /** Get the GetBlockResult case class instance from a block.
    *
    * Used by : getblock RPC.
    * @param block The block to format.
    * @return The GetBlockResult instance.
    */
  def getBlockResult(block : Block) : GetBlockResult = {
    val serializedBlock = BlockCodec.serialize(block)

    val blockHash = Hash( HashCalculator.blockHeaderHash(block.header) )

    val txHashes = block.transactions.map { tx =>
      Hash( HashCalculator.transactionHash(tx) )
    }

    GetBlockResult(
      hash = blockHash,
      size = serializedBlock.length,
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
    * @return The converted RawTransaction instance.
    */
  def getRawTransaction(transaction : Transaction) : RawTransaction = {
    val txHash = Hash( HashCalculator.transactionHash(transaction) )
    val serializedTransaction = getSerializedTranasction(transaction)

    RawTransaction(
      hex      = serializedTransaction,
      txid     = txHash,
      version  = transaction.version,
      locktime = transaction.lockTime,
      vin      = convertTransactionInputs(transaction.inputs),
      vout     = convertTransactionOutputs(transaction.outputs)
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
    val txHash = Hash( HashCalculator.transactionHash(transaction) )

    DecodedRawTransaction(
      txid     = txHash,
      version  = transaction.version,
      locktime = transaction.lockTime,
      vin      = convertTransactionInputs(transaction.inputs),
      vout     = convertTransactionOutputs(transaction.outputs)
    )
  }



  /** Convert a transaction to a TransactionItem, which is an element of array to respond for listtransactions RPC.
    *
    * Used by : listtransactions RPC.
    *
    * @param transaction The transaction to convert.
    * @return The converted transaction item.
    */
  def getTransactionItem( transaction : Transaction ) : TransactionItem =  {
    // TODO : Implement
    assert(false)
    null
  }
}
