package io.scalechain.blockchain.chain
import io.scalechain.blockchain.proto
import io.scalechain.blockchain.proto.{BlockHash, Transaction, Block, BlockHeader}

import scala.collection.mutable.ListBuffer

/**
  * Builds a block with a list of transactions.
  */
class BlockBuilder {
  /** A list buffer that has transactions for this block.
    *
    */
  val transactionsBuffer = new ListBuffer[Transaction]()

  /** Add a transaction.
    *
    * @param transaction the transaction to add.
    */
  def addTransaction(transaction : Transaction) : Unit = {
    transactionsBuffer.append(transaction)
  }

  /** Check if the current status of the builder is valid.
    *
    * 1. Check the size of serialized block.
    *
    * @param block The block to use to calculate the serialized size of it.
    */
  protected[chain] def checkValidity(block : Block) : Unit = {

  }

  /** Get the block.
    *
    * @param version The version of the block
    * @param hashPrevBlock The hash of the previous block header.
    * @param timestamp The block timestamp.
    * @param target The target difficulty
    * @param nonce The nonce value.
    * @return The built block.
    */
  def getBlock(version:Int, hashPrevBlock : BlockHash, timestamp : Long, target : Long, nonce : Long) : Block = {
    val transactions = transactionsBuffer.toList
    val merkleRootHash = MerkleRootHash.calculate(transactions)
    val blockHeader = BlockHeader(version, hashPrevBlock, proto.MerkleRootHash(merkleRootHash.value), timestamp, target, nonce)
    val block = Block(
      blockHeader,
      transactions
    )
    block
  }
}
