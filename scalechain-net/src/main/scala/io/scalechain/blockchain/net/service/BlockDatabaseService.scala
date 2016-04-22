package io.scalechain.blockchain.net.service

import akka.util.Timeout
import io.scalechain.blockchain.proto.{BlockInfo, Transaction, Block, Hash}
import io.scalechain.blockchain.storage.DiskBlockStorage

import scala.concurrent.duration._

/**
  * Created by kangmo on 3/15/16.
  */
class BlockDatabaseService() {
  implicit val timeout = Timeout(60 seconds)

  /** Put a block on the block database.
    *
    * @param block The block to put.
    * @return true if the block was a new one without any previous block header or block. false otherwise.
    */
  def putBlock(block : Block) : Boolean = {
    DiskBlockStorage.get.putBlock(block)
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  def getBlock(blockHash: Hash): Option[(BlockInfo, Block)] = {
    DiskBlockStorage.get.getBlock(blockHash)
  }

  /** Get the header hash of the most recent block on the best block chain.
    *
    * Used by : getbestblockhash RPC.
    *
    * @return The header hash of the most recent block.
    */
  def getBestBlockHash(): Option[Hash] = {
    DiskBlockStorage.get.getBestBlockHash()
  }


  /** Get a transaction searching by the transaction hash.
    *
    * Used by : gettransaction RPC.
    *
    * @param txHash The header hash of the transaction to search.
    * @return The searched block.
    */
  def getTransaction(txHash : Hash): Option[Transaction] = {
    DiskBlockStorage.get.getTransaction(txHash)
  }
}