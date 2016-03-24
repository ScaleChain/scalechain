package io.scalechain.blockchain.storage.index

import java.util.{ArrayList, List}

import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.MessagePartCodec
import org.rocksdb.ColumnFamilyHandle

/**
  * Created by kangmo on 3/11/16.
  */
trait KeyValueDatabase {
  def get(key : Array[Byte] ) : Option[Array[Byte]]
  def put(key : Array[Byte], value : Array[Byte] ) : Unit
  def del(key : Array[Byte]) : Unit
  def close() : Unit

  // add new functions for column families of rocksdb
  def openWithColumnFamily() : Unit
  def createColumnFamily(descriptor : String) : Unit
  def getColumnFamilyIndex(descriptor : String) : Int
  def getColumnFamilyHandle(descriptor: String) : ColumnFamilyHandle
  def get(cf : ColumnFamilyHandle, key : Array[Byte]) : Option[Array[Byte]]
  def getKeys(cf : ColumnFamilyHandle) : ArrayList[String]
  def put(cf : ColumnFamilyHandle, key : Array[Byte], value : Array[Byte]) : Unit

  private def prefixedKey(prefix: Byte, key:Array[Byte]) = Array(prefix) ++ key

  def getObject[V <: ProtocolMessage](rawKey : Array[Byte])(valueCodec : MessagePartCodec[V]) : Option[V] = {
    get(rawKey).map( valueCodec.parse(_) )
  }

  def getObject[K <: ProtocolMessage,V <: ProtocolMessage](prefix : Byte, key : K)(keyCodec : MessagePartCodec[K], valueCodec : MessagePartCodec[V]) : Option[V] = {
    val rawKey = prefixedKey(prefix, keyCodec.serialize(key))
    getObject(rawKey)(valueCodec)
  }

  def getObject[K <: ProtocolMessage,V <: ProtocolMessage](key : K)(keyCodec : MessagePartCodec[K], valueCodec : MessagePartCodec[V]) : Option[V] = {
    getObject(keyCodec.serialize(key))(valueCodec)
  }

  def getObject[K <: ProtocolMessage,V <: ProtocolMessage](columnFamilyName : String, key : K)(keyCodec : MessagePartCodec[K], valueCodec : MessagePartCodec[V]) : Option[V] = {
    val index = getColumnFamilyIndex(columnFamilyName)

    if(index == -1) {
      None
    } else {
      val columnFamilyHandle = getColumnFamilyHandle(columnFamilyName)
      get(columnFamilyHandle, keyCodec.serialize(key)).map( valueCodec.parse(_) )
    }
  }

  def putObject[V <: ProtocolMessage](rawKey : Array[Byte], value : V)(valueCodec : MessagePartCodec[V]) : Unit = {
    val rawValue = valueCodec.serialize(value)

    put(rawKey, rawValue)
  }

  def putObject[K <: ProtocolMessage, V <: ProtocolMessage](prefix : Byte, key : K, value : V)(keyCodec : MessagePartCodec[K], valueCodec : MessagePartCodec[V]) : Unit = {
    val rawKey = prefixedKey(prefix, keyCodec.serialize(key))

    putObject(rawKey, value)(valueCodec)
  }

  def putObject[K <: ProtocolMessage, V <: ProtocolMessage](columnFamilyName : String, key : K, value : V)(keyCodec : MessagePartCodec[K], valueCodec : MessagePartCodec[V]) : Unit = {

    val index = getColumnFamilyIndex(columnFamilyName)

    if(index == -1) {
      createColumnFamily(columnFamilyName)
      val columnFamilyHandle = getColumnFamilyHandle(columnFamilyName)
      put(columnFamilyHandle, keyCodec.serialize(key), valueCodec.serialize(value))
    } else {
      val columnFamilyHandle = getColumnFamilyHandle(columnFamilyName)
      put(columnFamilyHandle, keyCodec.serialize(key), valueCodec.serialize(value))
    }
  }

  def delObject[K <: ProtocolMessage](prefix : Byte, key : K)(keyCodec : MessagePartCodec[K]) = {
    val rawKey = prefixedKey(prefix, keyCodec.serialize(key))
    del(rawKey)
  }

  def getKeys(columnFamilyName : String) : ArrayList[String] = {
    val index = getColumnFamilyIndex(columnFamilyName)
    val keys = new ArrayList[String]

    if(index != 0) {
      val columnFamilyHandle = getColumnFamilyHandle(columnFamilyName)
      getKeys(columnFamilyHandle)
    } else {
      keys
    }
  }
}

