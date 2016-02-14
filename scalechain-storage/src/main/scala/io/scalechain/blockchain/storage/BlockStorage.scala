package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{Hash, Block, BlockHeader}

/**
  * Created by kangmo on 2/14/16.
  */
trait BlockStorage {
  def storeHeader(header : BlockHeader)
  def getHeader(hash:Hash) : Option[BlockHeader]
  def hasHeader(hash:Hash) : Boolean

  def storeBlock(block : Block)
  def getBlock(hash : Hash) : Option[Block]
  def hasBlock(hash:Hash) : Boolean
}
