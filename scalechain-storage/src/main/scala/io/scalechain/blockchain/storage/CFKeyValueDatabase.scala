package io.scalechain.blockchain.storage

import java.util.ArrayList

import io.scalechain.blockchain.proto.{ProtocolMessage}
import io.scalechain.blockchain.proto.codec.{MessagePartCodec}
import org.rocksdb.ColumnFamilyHandle

/**
  * Created by mijeong on 2016. 3. 25..
  */
trait CFKeyValueDatabase {

  def createColumnFamily(descriptor : String) : Unit
  def getColumnFamilyIndex(descriptor : String) : Int
  def getColumnFamilyHandle(descriptor: String) : ColumnFamilyHandle
  def get(cf : ColumnFamilyHandle, key : Array[Byte]) : Option[Array[Byte]]
  def getKeys(cf : ColumnFamilyHandle) : ArrayList[String]
  def getKeysByteArray(cf: ColumnFamilyHandle): ArrayList[Array[Byte]]
  def put(cf : ColumnFamilyHandle, key : Array[Byte], value : Array[Byte]) : Unit
  def close() : Unit

  def getObject[K <: ProtocolMessage,V <: ProtocolMessage](columnFamilyName : String, key : K)(keyCodec : MessagePartCodec[K], valueCodec : MessagePartCodec[V]) : Option[V] = {
    val index = getColumnFamilyIndex(columnFamilyName)

    if(index == -1) {
      None
    } else {
      val columnFamilyHandle = getColumnFamilyHandle(columnFamilyName)
      get(columnFamilyHandle, keyCodec.serialize(key)).map( valueCodec.parse(_) )
    }
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

  def getKeys(columnFamilyName : String) : ArrayList[String] = {
    val index = getColumnFamilyIndex(columnFamilyName)
    val keys = new ArrayList[String]

    if(index > 0) {
      val columnFamilyHandle = getColumnFamilyHandle(columnFamilyName)
      getKeys(columnFamilyHandle)
    } else {
      keys
    }
  }

  def getKeysByteArray(columnFamilyName : String) : Option[ArrayList[Array[Byte]]] = {
    val columnFamilyHandle = getColumnFamilyHandle(columnFamilyName)

    if(columnFamilyHandle == null)
      None
    else {
      val keys = getKeysByteArray(columnFamilyHandle)
      if(keys.size() > 0)
        Some(keys)
      else
        None
    }
  }

}
