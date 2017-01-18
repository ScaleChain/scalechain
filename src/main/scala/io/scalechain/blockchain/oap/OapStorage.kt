package io.scalechain.blockchain.oap

import java.io.File

import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer
import io.scalechain.blockchain.oap.transaction.OapTransactionOutput
import io.scalechain.blockchain.oap.util.Pair
import io.scalechain.blockchain.oap.wallet.AssetId
import io.scalechain.blockchain.proto.codec._
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.primitive._
import io.scalechain.blockchain.storage.index.{ClosableIterator, KeyValueDatabase, RocksDatabase}
import io.scalechain.util.ByteArray
import scodec.Codec

/**
  * Storage for OAP.
  * Hold Colored outputs, Asset Definition Pointer for asset Id and Asset Definition File for Asset Definition Pointer.
  * For use with bltcoind OapStorage uses the sepatrated rocksdb not the blockstorage of sclachain.
  * Can be integrated into the blockstorage of scalechian.
  *
  * The data stored in this storage are maintained locally in running node.
  * The Asset Definition Pointers and Asset Definition Files should be propagated to other nodes.
  *
  * Created by shannon on 16. 12. 27.
  */

object OapStorage {
  var theStorage : OapStorage = null
  def create(path : File) = {
    theStorage = new OapStorage(path)
    theStorage
  }
  def get() = {
    theStorage
  }
}

class OapStorage(path : File) {
  val COLORED_OUTPUT :          Byte = 'O'
  val ASSET_DEFINITION:        Byte = 'D'
  val ASSET_DEFINITION_POINTER: Byte = 'H'

  val db :  RocksDatabase = new RocksDatabase(path);

  def putOutput(outPoint : OutPoint, output : OapTransactionOutput) : Unit = {
    val value = OutputCacheItem.from(output);
    db.putObject(COLORED_OUTPUT, outPoint, value)(OutPointCodec, OutputCacheItemCodec)
  }

  def getOutput(outPoint : OutPoint) : Option[OapTransactionOutput] = {
    val itemOption = db.getObject(COLORED_OUTPUT, outPoint)(OutPointCodec, OutputCacheItemCodec)
    if (itemOption.isEmpty) None
    else {
      Option(new OapTransactionOutput(AssetId.from(itemOption.get.assetId), itemOption.get.quantity, itemOption.get.output))
    }
  }

  def delOutput(outPoint : OutPoint) : Unit = {
    db.delObject(COLORED_OUTPUT, outPoint)(OutPointCodec);
  }

  def getOutputs(hash : Hash) : java.util.List[Pair[OutPoint, OapTransactionOutput]] = {
    val result : java.util.List[Pair[OutPoint, OapTransactionOutput]] = new java.util.ArrayList[Pair[OutPoint, OapTransactionOutput]]()
    val outPoint = OutPoint(hash, 0)
    var f : Boolean = true
    var iterator : ClosableIterator[(OutPoint, OutputCacheItem)] = null
    try {
      iterator = db.seekObject(COLORED_OUTPUT, outPoint)(OutPointCodec, OutputCacheItemCodec)
      while (iterator.hasNext && f) {
        val (key, value) = iterator.next()
        if (key.transactionHash.equals(hash))
          result.add(new Pair[OutPoint, OapTransactionOutput](
            key, new OapTransactionOutput(AssetId.from(value.assetId), value.quantity, value.output)
          ))
        else
          f = false
      }
    } finally {
      if (iterator != null)
        iterator.close
    }
    result
  }

  def delOutputs(hash : Hash) : Int = {
    val outPoint = OutPoint(hash, 0);
    var count = 0;
    var f : Boolean = true;
    var iterator : ClosableIterator[(OutPoint, OutputCacheItem)] = null
    try {
      iterator = db.seekObject(COLORED_OUTPUT, outPoint)(OutPointCodec, OutputCacheItemCodec)
      while (iterator.hasNext && f) {
        val (key, v) = iterator.next()
        if (key.transactionHash.equals(hash)) {
          db.delObject(COLORED_OUTPUT, key)(OutPointCodec)
          count += 1
        } else {
          f = false
        }
      }
    } finally {
      if (iterator != null) iterator.close
    }
    count
  }

  def putAssetDefinition(pointer : AssetDefinitionPointer, value : String) : Unit = {
    db.putObject(ASSET_DEFINITION,
      FixedByteArrayMessage(ByteArray(pointer.getPointer())),
      StringMessage(value)
    )(FixedByteArrayMessageCodec, StringMessageCodec)
  }

  def getAssetDefinition(pointer : AssetDefinitionPointer) : Option[String] = {
    val v = db.getObject(ASSET_DEFINITION,
      FixedByteArrayMessage(ByteArray(pointer.getPointer()))
    )(FixedByteArrayMessageCodec, StringMessageCodec)
    if (v.isDefined) Some(v.get.value) else None
  }

  def delAssetDefintion(pointer : AssetDefinitionPointer) : Unit = {
    db.delObject(ASSET_DEFINITION, FixedByteArrayMessage(ByteArray(pointer.getPointer())))(FixedByteArrayMessageCodec)
  }

  def putAssetDefinitionPointer(assetId : String, pointer : AssetDefinitionPointer) = {
    db.putObject(ASSET_DEFINITION_POINTER,
      StringMessage(assetId),
      FixedByteArrayMessage(ByteArray(pointer.getPointer()))
    )(StringMessageCodec, FixedByteArrayMessageCodec)
  }

  def getAssetDefinitionPointer(assetId : String) : Option[AssetDefinitionPointer] = {
    val v = db.getObject(ASSET_DEFINITION_POINTER, StringMessage(assetId))(StringMessageCodec, FixedByteArrayMessageCodec)
    if (v.isDefined)
      Option(AssetDefinitionPointer.from(
        v.get.value.array
      ))
    else None
  }

  def delAssetDefinitionPointer(assetId : String) : Unit = {
    db.delObject(ASSET_DEFINITION_POINTER, StringMessage(assetId))(StringMessageCodec)
  }

  def close : Unit = {
    db.close
  }
}

