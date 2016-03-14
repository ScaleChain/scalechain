package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.MessagePartCodec

/**
  * Created by kangmo on 3/11/16.
  */
trait KeyValueDatabase {
  def get(key : Array[Byte] ) : Option[Array[Byte]]
  def put(key : Array[Byte], value : Array[Byte] ) : Unit
  def del(key : Array[Byte]) : Unit
  def close() : Unit

  private def prefixedKey(prefix: Byte, key:Array[Byte]) = Array(prefix) ++ key

  def getObject[V <: ProtocolMessage](rawKey : Array[Byte])(valueCodec : MessagePartCodec[V]) : Option[V] = {
    get(rawKey).map( valueCodec.parse(_) )
  }

  def getObject[K <: ProtocolMessage,V <: ProtocolMessage](prefix : Byte, key : K)(keyCodec : MessagePartCodec[K], valueCodec : MessagePartCodec[V]) : Option[V] = {
    val rawKey = prefixedKey(prefix, keyCodec.serialize(key))
    getObject(rawKey)(valueCodec)
  }

  def putObject[V <: ProtocolMessage](rawKey : Array[Byte], value : V)(valueCodec : MessagePartCodec[V]) : Unit = {
    val rawValue = valueCodec.serialize(value)

    put(rawKey, rawValue)
  }

  def putObject[K <: ProtocolMessage, V <: ProtocolMessage](prefix : Byte, key : K, value : V)(keyCodec : MessagePartCodec[K], valueCodec : MessagePartCodec[V]) : Unit = {
    val rawKey = prefixedKey(prefix, keyCodec.serialize(key))

    putObject(rawKey, value)(valueCodec)
  }

  def delObject[K <: ProtocolMessage](prefix : Byte, key : K)(keyCodec : MessagePartCodec[K]) = {
    val rawKey = prefixedKey(prefix, keyCodec.serialize(key))
    del(rawKey)
  }
}

