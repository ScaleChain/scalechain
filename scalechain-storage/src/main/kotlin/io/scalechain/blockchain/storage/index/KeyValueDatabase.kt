package io.scalechain.blockchain.storage.index

import java.util

import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.MessagePartCodec
import io.scalechain.blockchain.proto.codec.primitive.{CStringPrefixed, CStringPrefixedCodec, CString}
import scodec.codecs._

trait ClosableIterator<T> : Iterator<T> with AutoCloseable {
  fun close : Unit
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
  fun seek(keyOption : Option<Array<Byte>> ) : ClosableIterator<(Array<Byte>, Array<Byte>)>
  fun get(key : Array<Byte> ) : Option<Array<Byte>>
  fun put(key : Array<Byte>, value : Array<Byte> ) : Unit
  fun del(key : Array<Byte>) : Unit
  fun close() : Unit

  private fun prefixedKey(prefix: Byte, key:Array<Byte>) = Array(prefix) ++ key
  private fun prefixedKey(prefix: Array<Byte>, key:Array<Byte>) = prefix ++ key

  fun seekObject<V <: ProtocolMessage>(rawKeyOption : Option<Array<Byte>> = None)(implicit valueCodec : MessagePartCodec<V>) : ClosableIterator<(Array<Byte>, V)> {
    class ValueMappedIterator(iterator:ClosableIterator<(Array<Byte>,Array<Byte>)>) : ClosableIterator<(Array<Byte>, V)> {
      fun next : (Array<Byte>, V) {
        val (rawKey, rawValue) = iterator.next
        (rawKey, valueCodec.parse(rawValue))
      }
      fun hasNext : Boolean = iterator.hasNext
      fun close : Unit = iterator.close
    }
    val rawIterator = seek(rawKeyOption)
    ValueMappedIterator(rawIterator)
  }

  fun seekObject<K <: ProtocolMessage,V <: ProtocolMessage>(prefix: Byte, key : K)(implicit keyCodec : MessagePartCodec<K>, valueCodec : MessagePartCodec<V>) : ClosableIterator<(K,V)> {
    seekObjectInternal( Array(prefix), Some(key))(keyCodec, valueCodec)
  }

  fun seekPrefixedObject<K <: ProtocolMessage,V <: ProtocolMessage>(prefix: Byte, keyPrefix:String)(implicit keyCodec : MessagePartCodec<K>, valueCodec : MessagePartCodec<V>) : ClosableIterator<(CStringPrefixed<K>,V)> {
    seekObjectInternal( prefixedKey(prefix, CString.serialize(keyPrefix)), None)(CStringPrefixedCodec(keyCodec), valueCodec)
  }

  fun seekPrefixedObject<K <: ProtocolMessage,V <: ProtocolMessage>(prefix: Byte)(implicit keyCodec : MessagePartCodec<K>, valueCodec : MessagePartCodec<V>) : ClosableIterator<(CStringPrefixed<K>,V)> {
    seekObjectInternal( Array(prefix), None)(CStringPrefixedCodec(keyCodec), valueCodec)
  }


  protected fun seekObjectInternal<K <: ProtocolMessage,V <: ProtocolMessage>(prefix: Array<Byte>, keyOption : Option<K>)(keyCodec : MessagePartCodec<K>, valueCodec : MessagePartCodec<V>) : ClosableIterator<(K,V)> {
    /** We should stop the iteration if the prefix of the key changes.
      * So, hasNext first gets the next key and checks if the prefix remains unchanged.
      * next will return the element we got from hasNext.
      *
      * If the prefix is changed, we stop the iteration.
      *
      * @param iterator
      */
    class PrefetchingIterator(iterator:ClosableIterator<(Array<Byte>,Array<Byte>)>) : ClosableIterator<(K, V)> {
      var elementToReturn : Option<(Array<Byte>,Array<Byte>)> = None

      fun next : (K, V) {
        assert(elementToReturn.isDefined)

        val rawKey = elementToReturn.get._1
        val rawValue = elementToReturn.get._2
        elementToReturn = None

        val readPrefix = rawKey.take(prefix.length)
        // hasNext should return false if the prefix of the next key does not match the prefix.
        assert( util.Arrays.equals(readPrefix,prefix) )
        // We need to drop the prefix byte for the rawKey.
        (keyCodec.parse(rawKey.drop(1)), valueCodec.parse(rawValue))
      }

      /** Prefetch next key, and check if the prefix matches with the one privided by the first parameter of the seekObject method.
        *
        * @return true if the next key matches the prefix.
        */
      fun hasNext : Boolean {
        // We already have a prefetched key.
        if (elementToReturn.isDefined) {
          val rawKey = elementToReturn.get._1
          val readPrefix = rawKey.take(prefix.length)
          if(util.Arrays.equals(readPrefix, prefix)) {
            true
          } else {
            false
          }
        } else {
          // We don't have a prefetched key. Prefetch one.
          if ( iterator.hasNext ) {
            val (rawKey, rawValue) = iterator.next
            assert(rawKey.length > 0)
            val readPrefix = rawKey.take(prefix.length)
            elementToReturn = Some((rawKey, rawValue))

            // Continue the iteration only if the prefix remains unchanged.
            if(util.Arrays.equals(readPrefix, prefix)) {
              true
            } else {
              false
            }
          } else {
            false
          }
        }
      }
      fun close : Unit = iterator.close
    }
    val seekKey =
      if (keyOption.isDefined)
        Some(prefixedKey(prefix, keyCodec.serialize(keyOption.get)))
      else
        Some(prefix)
    val rawIterator = seek(seekKey)
    PrefetchingIterator(rawIterator)
  }



  fun getObject<V <: ProtocolMessage>(rawKey : Array<Byte>)(implicit valueCodec : MessagePartCodec<V>) : Option<V> {
    get(rawKey).map( valueCodec.parse(_) )
  }

  fun getObject<K <: ProtocolMessage,V <: ProtocolMessage>(prefix : Byte, key : K)(implicit keyCodec : MessagePartCodec<K>, valueCodec : MessagePartCodec<V>) : Option<V> {
    val rawKey = prefixedKey(prefix, keyCodec.serialize(key))
    getObject(rawKey)(valueCodec)
  }

  fun getPrefixedObject<K <: ProtocolMessage,V <: ProtocolMessage>(prefix : Byte, keyPrefix : String, key : K)(implicit keyCodec : MessagePartCodec<K>, valueCodec : MessagePartCodec<V>) : Option<V> {
    val rawKey = prefixedKey(prefix, CStringPrefixedCodec<K>(keyCodec).serialize(CStringPrefixed(keyPrefix, key)) )
    getObject(rawKey)(valueCodec)
  }



  fun putObject<V <: ProtocolMessage>(rawKey : Array<Byte>, value : V)(implicit valueCodec : MessagePartCodec<V>) : Unit {
    val rawValue = valueCodec.serialize(value)

    put(rawKey, rawValue)
  }

  fun putObject<K <: ProtocolMessage, V <: ProtocolMessage>(prefix : Byte, key : K, value : V)(implicit keyCodec : MessagePartCodec<K>, valueCodec : MessagePartCodec<V>) : Unit {
    val rawKey = prefixedKey(prefix, keyCodec.serialize(key))

    putObject(rawKey, value)(valueCodec)
  }

  fun putPrefixedObject<K <: ProtocolMessage, V <: ProtocolMessage>(prefix : Byte, keyPrefix : String, key : K, value : V)(implicit keyCodec : MessagePartCodec<K>, valueCodec : MessagePartCodec<V>) : Unit {
    val rawKey = prefixedKey(prefix, CStringPrefixedCodec<K>(keyCodec).serialize(CStringPrefixed(keyPrefix, key)) )

    putObject(rawKey, value)(valueCodec)
  }


  fun delObject<K <: ProtocolMessage>(prefix : Byte, key : K)(implicit keyCodec : MessagePartCodec<K>) : Unit {
    val rawKey = prefixedKey(prefix, keyCodec.serialize(key))
    del(rawKey)
  }

  fun delPrefixedObject<K <: ProtocolMessage>(prefix : Byte, keyPrefix : String, key : K)(implicit keyCodec : MessagePartCodec<K>) : Unit {
    delPrefixedObject( prefix, CStringPrefixed(keyPrefix, key))
  }

  fun delPrefixedObject<K <: ProtocolMessage>(prefix : Byte, key : CStringPrefixed<K>)(implicit keyCodec : MessagePartCodec<K>) : Unit {
    val rawKey = prefixedKey(prefix, CStringPrefixedCodec<K>(keyCodec).serialize(key) )
    del(rawKey)
  }
}

