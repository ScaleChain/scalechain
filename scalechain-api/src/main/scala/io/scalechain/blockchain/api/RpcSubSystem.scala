package io.scalechain.blockchain.api

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.net.{PeerInfo, PeerCommunicator, PeerSet}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.DiskBlockStorage
import spray.json.JsObject
import HashSupported._
/**
  * Created by kangmo on 3/15/16.
  */
object RpcSubSystem {
  var theRpcSubSystem : RpcSubSystem = null

  def create(chain : Blockchain, peerCommunicator: PeerCommunicator) = {
    theRpcSubSystem = new RpcSubSystem(chain, peerCommunicator)
    theRpcSubSystem
  }

  def get = {
    assert(theRpcSubSystem != null)
    theRpcSubSystem
  }
}

class RpcSubSystem(chain : Blockchain, peerCommunicator: PeerCommunicator) {

  /** Get the hash of a block specified by the block height on the best blockchain.
    *
    * Used by : getblockhash RPC.
    *
    * @param blockHeight The height of the block.
    * @return The hash of the block header.
    */
  def getBlockHash(blockHeight : Long) : Hash = {
    chain.getBlockHash(blockHeight)
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash: Hash): Option[(BlockInfo, Block)] = {
    chain.getBlock(blockHash)
  }

  /** Get the header hash of the most recent block on the best block chain.
    *
    * Used by : getbestblockhash RPC.
    *
    * @return The header hash of the most recent block.
    */
  def getBestBlockHash(): Option[Hash] = {
    chain.getBestBlockHash()
  }


  /**
    * Return the block height of the best block.
    *
    * Used by getrawtransaction RPC to get the confirmation of the block which has a transaction.
    * @return The height of the best block.
    */
  def getBestBlockHeight() : Long = {
    chain.getBestBlockHeight()
  }

  /**
    * Get the block info of the block which has the given transaction.
    *
    * @param txHash The hash of the transaction to get the block info of the block which has the transaction.
    * @return Some(block info) if the transaction is included in a block; None otherwise.
    */
  def getTransactionBlockInfo(txHash : Hash) : Option[BlockInfo] = {
    chain.getTransactionBlockInfo(txHash)
  }

  /** Get a transaction searching by the transaction hash.
    *
    * Used by : gettransaction RPC.
    *
    * @param txHash The header hash of the transaction to search.
    * @return The searched block.
    */
  def getTransaction(txHash : Hash): Option[Transaction] = {
    chain.getTransaction(txHash)
  }


  /** List of responses for submitblock RPC.
    */
  object SubmitBlockResult extends Enumeration {
    val DUPLICATE         = new Val(nextId, "duplicate")
    val DUPLICATE_INVALID = new Val(nextId, "duplicate-invalid")
    val INCONCLUSIVE      = new Val(nextId, "inconclusive")
    val REJECTED          = new Val(nextId, "rejected")
  }

  /** Accepts a block, verifies it is a valid addition to the block chain, and broadcasts it to the network.
    *
    * Used by : submitblock RPC.
    *
    * @param block The block we are going to submit.
    * @param parameters The JsObject we got from the second parameter of submitblock RPC. A common parameter is a workid string.
    * @return Some(SubmitBlockResult) if any error happend; None otherwise.
    */
  def submitBlock(block : Block, parameters : JsObject) : Option[SubmitBlockResult.Value] = {
    // TODO : BUGBUG : parameters is not used.
    val blockHash = block.header.hash
    if (chain.hasBlock(blockHash)) {
      Some(SubmitBlockResult.DUPLICATE)
    } else {
      peerCommunicator.propagateBlock(block)
      chain.putBlock(Hash( blockHash.value) , block)
      None
    }
  }

  /** Validates a transaction and broadcasts it to the peer-to-peer network.
    *
    * Used by : sendrawtransaction RPC.
    *
    * @param transaction The serialized transaction.
    * @param allowHighFees Whether to allow the transaction to pay a high transaction fee.
    * @return
    */
  def sendRawTransaction(transaction : Transaction, allowHighFees : Boolean) = {
    // TODO : BUGBUG : allowHighFees is not used.
    chain.putTransaction(transaction.hash, transaction)

    peerCommunicator.propagateTransaction(transaction)
  }

  /** Get the list of information on each peer.
    *
    * Used by : getpeerinfo RPC.
    *
    * @return The list of peer information.
    */
  def getPeerInfos() : List[PeerInfo] = {
    peerCommunicator.getPeerInfos()
  }
}


