package io.scalechain.blockchain.api

import io.scalechain.blockchain.RpcException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.net.Node
import io.scalechain.blockchain.net.PeerInfo
import io.scalechain.blockchain.net.PeerCommunicator
import io.scalechain.blockchain.net.PeerSet
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.DiskBlockStorage
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionVerifier
import com.google.gson.Gson

class RpcSubSystem(private val db : KeyValueDatabase, private val chain : Blockchain, private val peerCommunicator: PeerCommunicator) {

  /** Get the hash of a block specified by the block height on the best blockchain.
    *
    * Used by : getblockhash RPC.
    *
    * @param blockHeight The height of the block.
    * @return The hash of the block header.
    */
  fun getBlockHash(blockHeight : Long) : Hash {
    return chain.getBlockHash(db, blockHeight)
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  fun getBlock(blockHash: Hash): Pair<BlockInfo, Block>? {
    return chain.getBlock(db, blockHash)
  }

  /** Get the header hash of the most recent block on the best block chain.
    *
    * Used by : getbestblockhash RPC.
    *
    * @return The header hash of the most recent block.
    */
  fun getBestBlockHash(): Hash? {
    return chain.getBestBlockHash(db)
  }


  /**
    * Return the block height of the best block.
    *
    * Used by getrawtransaction RPC to get the confirmation of the block which has a transaction.
    *
    * @return The height of the best block.
    */
  fun getBestBlockHeight() : Long {
    return chain.getBestBlockHeight()
  }

  /**
    * Get the block info of the block which has the given transaction.
    *
    * @param txHash The hash of the transaction to get the block info of the block which has the transaction.
    * @return Some(block info) if the transaction is included in a block; None otherwise.
    */
  fun getTransactionBlockInfo(txHash : Hash) : BlockInfo? {
    return chain.getTransactionBlockInfo(db, txHash)
  }

  /** Get a transaction searching by the transaction hash.
    *
    * Used by : gettransaction RPC.
    *
    * @param txHash The header hash of the transaction to search.
    * @return The searched block.
    */
  fun getTransaction(txHash : Hash): Transaction? {
    return chain.getTransaction(db, txHash)
  }


  /** List of responses for submitblock RPC.
    */
  enum class SubmitBlockResult {
    DUPLICATE, DUPLICATE_INVALID, INCONCLUSIVE, REJECTED
/*
    val DUPLICATE         = Val(nextId, "duplicate")
    val DUPLICATE_INVALID = Val(nextId, "duplicate-invalid")
    val INCONCLUSIVE      = Val(nextId, "inconclusive")
    val REJECTED          = Val(nextId, "rejected")
*/
  }
/*
  /** Accepts a block, verifies it is a valid addition to the block chain, and broadcasts it to the network.
    *
    * Used by : submitblock RPC.
    *
    * @param block The block we are going to submit.
    * @param parameters The JsObject we got from the second parameter of submitblock RPC. A common parameter is a workid string.
    * @return Some(SubmitBlockResult) if any error happend; None otherwise.
    */
  fun submitBlock(block : Block, parameters : Any?) : SubmitBlockResult? {
    // TODO : BUGBUG : parameters is not used.
    val blockHash = block.header.hash()
    if (chain.hasBlock(db, blockHash)) {
      return SubmitBlockResult.DUPLICATE
    } else {
      peerCommunicator.propagateBlock(block)
      chain.withTransaction { transactingDB ->
        chain.putBlock(transactingDB, Hash(blockHash.value), block)
      }
      return null
    }
  }
*/
  /** Validates a transaction and broadcasts it to the peer-to-peer network.
    *
    * Used by : sendrawtransaction RPC.
    *
    * @param transaction The serialized transaction.
    * @param allowHighFees Whether to allow the transaction to pay a high transaction fee.
    * @return
    */
  fun sendRawTransaction(transaction : Transaction, allowHighFees : Boolean) {
    // Do not process the send raw transaction RPC during initial block download.
    if ( Node.get().isInitialBlockDownload() ) {
      throw RpcException( ErrorCode.BusyWithInitialBlockDownload, "Unable to send raw transactions while the initial block download is in progress.")
    } else {
      TransactionProcessor.putTransaction(Blockchain.get().db, transaction.hash(), transaction)

      peerCommunicator.propagateTransaction(transaction)
    }
  }

  /** Get the list of information on each peer.
    *
    * Used by : getpeerinfo RPC.
    *
    * @return The list of peer information.
    */
  fun getPeerInfos() : List<PeerInfo> {
    return peerCommunicator.getPeerInfos()
  }

  fun verifyTransaction( transaction : Transaction ) : Unit {
    val db : KeyValueDatabase = Blockchain.get().db

    TransactionVerifier(db, transaction).verify(Blockchain.get())
  }

  companion object {
    private var theRpcSubSystem : RpcSubSystem? = null

    fun create(chain : Blockchain, peerCommunicator: PeerCommunicator) : RpcSubSystem {
      theRpcSubSystem = RpcSubSystem(chain.db, chain, peerCommunicator)
      return theRpcSubSystem!!
    }

    fun get() : RpcSubSystem {
      assert(theRpcSubSystem != null)
      return theRpcSubSystem!!
    }
  }
}


