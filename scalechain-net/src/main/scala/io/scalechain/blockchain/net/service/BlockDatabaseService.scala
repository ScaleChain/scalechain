package io.scalechain.blockchain.net.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.scalechain.blockchain.net.processor.BlockProcessor
import io.scalechain.blockchain.net.processor.BlockProcessor.{PutBlockResult, GetBestBlockHashResult, GetBlockResult}
import io.scalechain.blockchain.proto.{Transaction, Block, Hash}

import scala.concurrent._
import scala.concurrent.duration._

/**
  * Created by kangmo on 3/15/16.
  */
class BlockDatabaseService(blockProcessor : ActorRef) {
  implicit val timeout = Timeout(60 seconds)

  /** Put a block on the block database.
    *
    * @param block The block to put.
    * @return true if the block was a new one without any previous block header or block. false otherwise.
    */
  def putBlock(block : Block) : Boolean = {
    val resultFuture = ( blockProcessor ? BlockProcessor.PutBlock(block) ).mapTo[PutBlockResult]
    Await.result(resultFuture, Duration.Inf).isNewBlock
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash: Hash): Option[Block] = {
    val resultFuture = ( blockProcessor ? BlockProcessor.GetBlock(blockHash) ).mapTo[GetBlockResult]
    Await.result(resultFuture, Duration.Inf).blockOption
  }

  /** Get the header hash of the most recent block on the best block chain.
    *
    * Used by : getbestblockhash RPC.
    *
    * @return The header hash of the most recent block.
    */
  def getBestBlockHash(): Option[Hash] = {
    val resultFuture = ( blockProcessor ? BlockProcessor.GetBestBlockHash() ).mapTo[GetBestBlockHashResult]
    Await.result(resultFuture, Duration.Inf).blockHashOption
  }


  /** Get a transaction searching by the header hash.
    *
    * Used by : gettransaction RPC.
    *
    * @param txHash The header hash of the transaction to search.
    * @return The searched block.
    */
  def getTransaction(txHash : Hash): Option[Transaction] = {
    val resultFuture = ( blockProcessor ? BlockProcessor.GetTransaction(txHash) ).mapTo[BlockProcessor.GetTransactionResult]
    Await.result(resultFuture, Duration.Inf).transactionOption
  }
}