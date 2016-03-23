package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec}
import io.scalechain.blockchain.script.HashCalculator

/**
  * Created by kangmo on 3/23/16.
  */
trait BlockStorage extends BlockIndex {
  def putBlock(blockHash : Hash, block : Block) : Boolean
  def putBlockHeader(blockHash : Hash, blockHeader : BlockHeader)
  def getTransaction(transactionHash : Hash) : Option[Transaction]
  def getBlock(blockHash : Hash) : Option[(BlockInfo, Block)]
  def getBestBlockHash() : Option[Hash]
  def getBlockHeader(blockHash : Hash) : Option[BlockHeader]


  def putBlock(block : Block) : Boolean = {
    val blockHash = Hash( HashCalculator.blockHeaderHash(block.header) )

    putBlock(blockHash, block)
  }

  def putBlockHeader(blockHeader : BlockHeader) : Unit = {
    val blockHash = Hash(HashCalculator.blockHeaderHash(blockHeader))

    putBlockHeader(blockHash, blockHeader)
  }

  def hasBlock(blockHash : Hash) : Boolean = {
    getBlock(blockHash).isDefined
  }

  def hasBlockHeader(blockHash : Hash) : Boolean = {
    getBlockHeader(blockHash).isDefined
  }

  // Methods that are extended from BlockIndex.
  def getBlock(blockHash : BlockHash) : Option[(BlockInfo, Block)] = {
    getBlock(Hash(blockHash.value))
  }

  def getTransaction(transactionHash : TransactionHash) : Option[Transaction] = {
    getTransaction(Hash(transactionHash.value))
  }


  def close() : Unit
}
