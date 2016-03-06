package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.{Hash, Block, BlockHeader}


/** Store block headers.
  *
  * Why have methods for storing and searching block headers only?
  * - During the Headers-first IBD(Initial block download), we first need to be able to download block headers first.
  * - In SPV mode, we need to download and store block headers only. SPV mode support is not decided yet though.
  */
trait BlockHeaderStorage {
  /** Store a block header.
    *
    * @param header The block header to store.
    */
  def storeHeader(header : BlockHeader) : Unit

  /** Search a block header by hash value.
    *
    * @param hash The hash of the header to search.
    * @return The found block header.
    */
  def getHeader(hash:Hash) : Option[BlockHeader]

  /** See if a block header whose hash matches the given one is stored.
    *
    * @param hash the hash of the header to search.
    * @return true if the block header exists. false otherwise.
    */
  def hasHeader(hash:Hash) : Boolean
}

/** Store blocks.
  */
trait BlockStorage {
  /** Store a block.
    *
    * @param block the block to store.
    */
  def storeBlock(block : Block)

  /** Search a block by a hash of the block header.
    *
    * @param hash The hash of the block header.
    * @return The found block.
    */
  def getBlock(hash : Hash) : Option[Block]

  /** See if a block exists.
    *
    * @param hash
    * @return true if the block exists. false otherwise.
    */
  def hasBlock(hash : Hash) : Boolean
}
