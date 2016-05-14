package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.MessagePartCodec

trait ClosableIterator[T] extends Iterator[T] with AutoCloseable {
  def close : Unit
}

/**
  * Created by kangmo on 3/11/16.
  */
trait KeyValueDatabase {

  /** Seek a key greater than or equal to the given key.
    * Return an iterator which iterates each (key, value) pair from the seek position.
    *
    * @param keyOption if Some(key) seek a key greater than or equal to the key; Seek all keys and values otherwise.
    * @return An Iterator to iterate (key, value) pairs.
    */
  def seek(keyOption : Option[Array[Byte]] ) : ClosableIterator[(Array[Byte], Array[Byte])]
  def get(key : Array[Byte] ) : Option[Array[Byte]]
  def put(key : Array[Byte], value : Array[Byte] ) : Unit
  def del(key : Array[Byte]) : Unit
  def close() : Unit

  private def prefixedKey(prefix: Byte, key:Array[Byte]) = Array(prefix) ++ key

  def seekObject[V <: ProtocolMessage](rawKeyOption : Option[Array[Byte]] = None)(valueCodec : MessagePartCodec[V]) : ClosableIterator[(Array[Byte], V)] = {
    class ValueMappedIterator(iterator:ClosableIterator[(Array[Byte],Array[Byte])]) extends ClosableIterator[(Array[Byte], V)] {
      def next : (Array[Byte], V) = {
        val (rawKey, rawValue) = iterator.next
        (rawKey, valueCodec.parse(rawValue))
      }
      def hasNext : Boolean = iterator.hasNext
      def close : Unit = iterator.close
    }
    val rawIterator = seek(rawKeyOption)
    new ValueMappedIterator(rawIterator)
  }

  def seekObject[K <: ProtocolMessage,V <: ProtocolMessage](prefix: Byte, key : K)(keyCodec : MessagePartCodec[K], valueCodec : MessagePartCodec[V]) : Iterator[(K,V)] = {

    class ValueMappedIterator(iterator:ClosableIterator[(Array[Byte],Array[Byte])]) extends ClosableIterator[(K, V)] {
      def next : (K, V) = {
        val (rawKey, rawValue) = iterator.next
        (keyCodec.parse(rawKey), valueCodec.parse(rawValue))
      }
      def hasNext : Boolean = iterator.hasNext
      def close : Unit = iterator.close
    }
    val seekKey = prefixedKey(prefix, keyCodec.serialize(key))
    val rawIterator = seek(Some(seekKey))
    new ValueMappedIterator(rawIterator)
  }


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

