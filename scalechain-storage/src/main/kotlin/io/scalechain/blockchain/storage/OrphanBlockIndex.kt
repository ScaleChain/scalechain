package io.scalechain.blockchain.storage

import io.scalechain.blockchain.proto.CStringPrefixed
import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixedCodec
import io.scalechain.blockchain.proto.codec.HashCodec
import io.scalechain.blockchain.proto.codec.OneByteCodec
import io.scalechain.blockchain.proto.codec.OrphanBlockDescriptorCodec
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.OneByte
import io.scalechain.blockchain.proto.OrphanBlockDescriptor
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.DB
import io.scalechain.util.HexUtil
//import io.scalechain.util.Using._

/**
  * Provides index operations for orphan blocks.
  */
interface OrphanBlockIndex {
  /** Put an orphan block
    *
    * @param hash The hash of the block header.
    * @param orphanBlockDescriptor The descriptor of the orphan block.
    */
  fun putOrphanBlock(db : KeyValueDatabase, hash : Hash, orphanBlockDescriptor : OrphanBlockDescriptor) : Unit {
    db.putObject(HashCodec, OrphanBlockDescriptorCodec, DB.ORPHAN_BLOCK, hash, orphanBlockDescriptor)
  }

  /** Get an orphan block by the hash of it.
    *
    * @param hash The orphan block header.
    * @return Some(block) if an orphan block was found by the hash. None otherwise.
    */
  fun getOrphanBlock(db : KeyValueDatabase, hash : Hash) : OrphanBlockDescriptor? {
    return db.getObject(HashCodec, OrphanBlockDescriptorCodec, DB.ORPHAN_BLOCK, hash)
  }

  /** Delete a specific orphan block.
    *
    * @param hash The hash of the orphan block.
    */
  fun delOrphanBlock(db : KeyValueDatabase, hash : Hash) : Unit {
    db.delObject(HashCodec, DB.ORPHAN_BLOCK, hash)
  }

  /** Add an orphan block than depends on a parent block denoted by the hash.
    *
    * @param parentBlockHash The hash of the parent block.
    * @param orphanBlockHash The hash of the orphan block.
    */
  fun addOrphanBlockByParent(db : KeyValueDatabase, parentBlockHash : Hash, orphanBlockHash : Hash) : Unit {
    // TODO : Optimize : Reduce the length of the prefix string by using base64 encoding?
    db.putPrefixedObject(HashCodec, OneByteCodec, DB.ORPHAN_BLOCKS_BY_PARENT, HexUtil.hex(parentBlockHash.value), orphanBlockHash, OneByte(1) )
  }

  /** Get all orphan blocks that depend on the given parent block.
    *
    * @param parentBlockHash The hash of the parent block.
    * @return Hash of all orphan blocks that depend on the parent.
    */
  fun getOrphanBlocksByParent(db : KeyValueDatabase, parentBlockHash : Hash) : List<Hash> {
    val iterator = db.seekPrefixedObject(HashCodec, OneByteCodec, DB.ORPHAN_BLOCKS_BY_PARENT, HexUtil.hex(parentBlockHash.value))
    try {
      // BUGBUG : Change the code not to use Pair, but a data class. This is code so hard to read.
      return iterator.asSequence().toList().map { prefixedObject ->
        val cstringPrefixed = prefixedObject.first
        val hash = cstringPrefixed.data
        hash
      }
    } finally {
      iterator.close()
    }
  }

  /** Del all orphan blocks that depend on the given parent block.
    *
    * @param parentBlockHash The hash of the parent block.
    */
  fun delOrphanBlocksByParent(db : KeyValueDatabase, parentBlockHash : Hash) : Unit {
    for (blockHash : Hash in getOrphanBlocksByParent(db, parentBlockHash)) {
      db.delPrefixedObject(HashCodec, DB.ORPHAN_BLOCKS_BY_PARENT, HexUtil.hex(parentBlockHash.value), blockHash)
    }
  }
}
