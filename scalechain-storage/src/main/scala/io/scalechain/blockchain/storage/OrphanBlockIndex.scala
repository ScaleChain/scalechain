package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixed
import io.scalechain.blockchain.proto.codec.{HashCodec, OneByteCodec, OrphanBlockDescriptorCodec}
import io.scalechain.blockchain.proto.{Hash, OneByte, OrphanBlockDescriptor}
import io.scalechain.blockchain.storage.index.{DatabaseTablePrefixes, SharedKeyValueDatabase}
import io.scalechain.util.HexUtil
import io.scalechain.util.HexUtil._
import io.scalechain.util.Using._

/**
  * Provides index operations for orphan blocks.
  */
trait OrphanBlockIndex extends SharedKeyValueDatabase {
  import DatabaseTablePrefixes._
  private implicit val hashCodec = HashCodec
  private implicit val orphanBlockDescriptorCodec = OrphanBlockDescriptorCodec
  private implicit val oneByteCodec = OneByteCodec
  /** Put an orphan block
    *
    * @param hash The hash of the block header.
    * @param orphanBlockDescriptor The descriptor of the orphan block.
    */
  def putOrphanBlock(hash : Hash, orphanBlockDescriptor : OrphanBlockDescriptor) : Unit = {
    keyValueDB.putObject(ORPHAN_BLOCK, hash, orphanBlockDescriptor)
  }

  /** Get an orphan block by the hash of it.
    *
    * @param hash The orphan block header.
    * @return Some(block) if an orphan block was found by the hash. None otherwise.
    */
  def getOrphanBlock(hash : Hash) : Option[OrphanBlockDescriptor] = {
    keyValueDB.getObject(ORPHAN_BLOCK, hash)(HashCodec, OrphanBlockDescriptorCodec)
  }

  /** Delete a specific orphan block.
    *
    * @param hash The hash of the orphan block.
    */
  def delOrphanBlock(hash : Hash) : Unit = {
    keyValueDB.delObject(ORPHAN_BLOCK, hash)
  }

  /** Add an orphan block than depends on a parent block denoted by the hash.
    *
    * @param parentBlockHash The hash of the parent block.
    * @param orphanBlockHash The hash of the orphan block.
    */
  def addOrphanBlockByParent(parentBlockHash : Hash, orphanBlockHash : Hash) : Unit = {
    // TODO : Optimize : Reduce the length of the prefix string by using base64 encoding?
    keyValueDB.putPrefixedObject(ORPHAN_BLOCKS_BY_PARENT, hex(parentBlockHash.value), orphanBlockHash, OneByte(1) )
  }

  /** Get all orphan blocks that depend on the given parent block.
    *
    * @param parentBlockHash The hash of the parent block.
    * @return Hash of all orphan blocks that depend on the parent.
    */
  def getOrphanBlocksByParent(parentBlockHash : Hash) : List[Hash] = {
    (
      using(keyValueDB.seekPrefixedObject(ORPHAN_BLOCKS_BY_PARENT, hex(parentBlockHash.value))(HashCodec, OneByteCodec)) in {
        _.toList
      }
    ).map{ case (CStringPrefixed(_, hash : Hash), _) => hash }
  }

  /** Del all orphan blocks that depend on the given parent block.
    *
    * @param parentBlockHash The hash of the parent block.
    */
  def delOrphanBlocksByParent(parentBlockHash : Hash) : Unit = {
    getOrphanBlocksByParent(parentBlockHash) foreach { blockHash : Hash =>
      keyValueDB.delPrefixedObject(ORPHAN_BLOCKS_BY_PARENT, hex(parentBlockHash.value), blockHash)
    }
  }
}
