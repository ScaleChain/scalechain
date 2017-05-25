package io.scalechain.blockchain.oap

import java.io.File

import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer
import io.scalechain.blockchain.oap.transaction.OapTransactionOutput
import io.scalechain.blockchain.oap.wallet.AssetId
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.primitive.*
import io.scalechain.blockchain.storage.index.ClosableIterator
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.util.Bytes

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

class OapStorage(path : File) {
  val db :  RocksDatabase = RocksDatabase(path);

  fun putOutput(outPoint : OutPoint, output : OapTransactionOutput) : Unit  {
    val value = OutputCacheItem.from(output);
    db.putObject(OutPointCodec, OutputCacheItemCodec, COLORED_OUTPUT, outPoint, value)
  }

  fun getOutput(outPoint : OutPoint) : OapTransactionOutput? {
    val itemOption = db.getObject(OutPointCodec, OutputCacheItemCodec, COLORED_OUTPUT, outPoint)
    if (itemOption == null) {
      return null
    } else {
      return OapTransactionOutput(AssetId.from(itemOption.assetId), itemOption.quantity, itemOption.output)
    }
  }

  fun delOutput(outPoint : OutPoint) : Unit  {
    db.delObject(OutPointCodec, COLORED_OUTPUT, outPoint);
  }

  fun getOutputs(hash : Hash) : List<Pair<OutPoint, OapTransactionOutput>> {
    val result : MutableList<Pair<OutPoint, OapTransactionOutput>> = mutableListOf<Pair<OutPoint, OapTransactionOutput>>()
    val outPoint = OutPoint(hash, 0)
    var f : Boolean = true

    db.seekObject(OutPointCodec, OutputCacheItemCodec, COLORED_OUTPUT, outPoint).use { iterator ->
      while (iterator.hasNext() && f) {
        val (key, value) = iterator.next()
        if (key.transactionHash.equals(hash)) {
          result.add(Pair(
            key, OapTransactionOutput(AssetId.from(value.assetId), value.quantity, value.output)
          ))
        } else {
          f = false
        }
      }
    }

    return result
  }

  fun delOutputs(hash : Hash) : Int  {
    val outPoint = OutPoint(hash, 0);
    var count = 0;
    var f : Boolean = true;

    db.seekObject(OutPointCodec, OutputCacheItemCodec, COLORED_OUTPUT, outPoint).use { iterator ->
      while (iterator.hasNext() && f) {
        val (key, v) = iterator.next()
        if (key.transactionHash.equals(hash)) {
          db.delObject(OutPointCodec, COLORED_OUTPUT, key)
          count += 1
        } else {
          f = false
        }
      }
    }
    return count
  }

  fun putAssetDefinition(pointer : AssetDefinitionPointer, value : String) : Unit  {
    db.putObject(
      AssetDefinitionPointerCodec, StringMessageCodec,
      ASSET_DEFINITION,
      FixedByteArrayMessage(Bytes(pointer.pointer)),
      StringMessage(value)
    )
  }

  fun getAssetDefinition(pointer : AssetDefinitionPointer) : String?  {
    val v = db.getObject(
      AssetDefinitionPointerCodec, StringMessageCodec,
      ASSET_DEFINITION,
      FixedByteArrayMessage(Bytes(pointer.pointer))
    )
    return v?.value
  }

  fun delAssetDefinition(pointer : AssetDefinitionPointer) : Unit  {
    val pointerBytes = Bytes(pointer.pointer)

    db.delObject(AssetDefinitionPointerCodec, ASSET_DEFINITION, FixedByteArrayMessage(pointerBytes))
  }

  fun putAssetDefinitionPointer(assetId : String, pointer : AssetDefinitionPointer)  {

    db.putObject(
      StringMessageCodec, AssetDefinitionPointerCodec,
      ASSET_DEFINITION_POINTER,
      StringMessage(assetId),
      FixedByteArrayMessage(Bytes(pointer.pointer))
    )
  }

  fun getAssetDefinitionPointer(assetId : String) : AssetDefinitionPointer? {
    val v = db.getObject(StringMessageCodec, AssetDefinitionPointerCodec, ASSET_DEFINITION_POINTER, StringMessage(assetId))
    if (v != null) {
      return AssetDefinitionPointer.from(
        v.value.array
      )
    }
    else {
      return null
    }
  }

  fun delAssetDefinitionPointer(assetId : String) : Unit  {
    db.delObject(StringMessageCodec, ASSET_DEFINITION_POINTER, StringMessage(assetId))
  }

  fun close() : Unit  {
    db.close()
  }

  companion object {
    lateinit var theStorage : OapStorage

    @JvmStatic
    fun create(path : File) : OapStorage {
      theStorage = OapStorage(path)
      return theStorage
    }

    @JvmStatic
    fun get() : OapStorage {
      return theStorage
    }

    private val AssetDefinitionPointerCodec = FixedByteArrayMessageCodec

    private val COLORED_OUTPUT           = 'O'.toByte()
    private val ASSET_DEFINITION         = 'D'.toByte()
    private val ASSET_DEFINITION_POINTER = 'H'.toByte()
  }
}

