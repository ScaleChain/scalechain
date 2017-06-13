package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.GeneralException
import org.mapdb.*
import org.mapdb.DB
import java.io.File

class MapDatabaseIterator(private val mapDbIterator : MutableIterator<MutableMap.MutableEntry<ByteArray, ByteArray?>>) : ClosableIterator<Pair<ByteArray,ByteArray>> {
  private var isClosed = false
  override fun next() : Pair<ByteArray,ByteArray> {
    assert( !isClosed )

    if (!mapDbIterator.hasNext()) {
      throw GeneralException(ErrorCode.NoMoreKeys)
    }

    val (rawKey, rawValue) = mapDbIterator.next()

    return Pair(rawKey, rawValue!!)
  }

  override fun hasNext() : Boolean {
    if (isClosed) {
      return false
    } else {
      return mapDbIterator.hasNext()
    }
  }

  override fun close() : Unit {
    isClosed = true
  }
}

/**
 * Created by kangmo on 18/12/2016.
 */
class MapDatabase(path : File) : KeyValueDatabase {
  private var db : DB
  private var treeMap : BTreeMap<ByteArray, ByteArray>
  fun getDb() = db
  fun getTreeMap() = treeMap
  init {
    if (!path.exists()) path.mkdir()
    val dbFilePath = File(path, "scalechain.mapdb")

    db = DBMaker
      .fileDB(dbFilePath)
      .fileMmapEnableIfSupported()
      //.fileMmapPreclearDisable()
      //.cleanerHackEnable()
      .transactionEnable()
      .make()

    treeMap = db.treeMap("db")
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializer.BYTE_ARRAY)
                // Try this option for performance tests.
                //.valueSerializer(SerializerCompressionWrapper(Serializer.BYTE_ARRAY))
                .createOrOpen()
  }

  override fun put(key: ByteArray, value: ByteArray) {
    treeMap.put(key, value)
    db.commit()
  }

  override fun get(key: ByteArray): ByteArray? {
    return treeMap.get(key)
    db.commit()
  }

  override fun del(key: ByteArray) {
    treeMap.remove(key)
    db.commit()
  }


  override fun seek(keyOption: ByteArray?): ClosableIterator<Pair<ByteArray, ByteArray>> {
    val iterator  = if (keyOption != null) {
      treeMap.tailMap(keyOption).iterator()
    } else {
      treeMap.iterator()
    }
    return MapDatabaseIterator(iterator)
  }

  override fun transacting(): TransactingKeyValueDatabase {
    return TransactingMapDatabase(this)
  }

  override fun close() {
    assert(!treeMap.isClosed())
    assert(!db.isClosed())
    treeMap.close()
    db.close()
  }
}
