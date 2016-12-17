package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.CStringPrefixed
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.primitive.CStringPrefixedCodec
import io.scalechain.blockchain.proto.codec.primitive.Codecs
import java.io.Closeable

import java.util.Arrays


interface ClosableIterator<T> : Iterator<T>, Closeable {
  override fun close() : Unit
}

/**
  * Created by kangmo on 3/11/16.
  */
interface KeyValueDatabase {

  /** Seek a key greater than or equal to the given key.
    * Return an iterator which iterates each (key, value) pair from the seek position.
    *
    * @param keyOption if Some(key) seek a key greater than or equal to the key; Seek all keys and values otherwise.
    * @return An Iterator to iterate (key, value) pairs.
    */
  fun seek(keyOption : ByteArray? ) : ClosableIterator<Pair<ByteArray, ByteArray>>
  fun get(key : ByteArray ) : ByteArray?
  fun put(key : ByteArray, value : ByteArray ) : Unit
  fun del(key : ByteArray) : Unit
  fun close() : Unit

  /**
   * Return a new key/value database that supports transaction commit/abort.
   */
  fun transacting() : TransactingKeyValueDatabase

  private fun prefixedKey(prefix: Byte, key:ByteArray) = ByteArray(1, {prefix}) + key
  private fun prefixedKey(prefix: ByteArray, key:ByteArray) = prefix + key

  fun<V> seekObject(valueCodec : Codec<V>, rawKeyOption : ByteArray? = null) : ClosableIterator<Pair<ByteArray, V>> {
    class ValueMappedIterator(private val iterator:ClosableIterator<Pair<ByteArray,ByteArray>>) : ClosableIterator<Pair<ByteArray, V>> {
      override fun next() : Pair<ByteArray, V> {
        val (rawKey, rawValue) = iterator.next()
        return Pair(rawKey, valueCodec.decode(rawValue)!!)
      }
      override fun hasNext() : Boolean = iterator.hasNext()
      override fun close() = iterator.close()
    }
    val rawIterator = seek(rawKeyOption)
    return ValueMappedIterator(rawIterator)
  }

  fun<K,V> seekObject(keyCodec : Codec<K>, valueCodec : Codec<V>, prefix: Byte, key : K) : ClosableIterator<Pair<K,V>> {
    return seekObjectInternal(keyCodec, valueCodec, ByteArray(1, {prefix}), key)
  }

  fun<K,V> seekPrefixedObject(keyCodec : Codec<K>, valueCodec : Codec<V>, prefix: Byte, keyPrefix:String) : ClosableIterator<Pair<CStringPrefixed<K>, V>> {
    val key = prefixedKey(prefix, Codecs.CString.encode(keyPrefix))

    return seekObjectInternal( CStringPrefixedCodec(keyCodec), valueCodec, key, null)
  }

  fun<K,V> seekPrefixedObject(keyCodec : Codec<K>, valueCodec : Codec<V>, prefix: Byte) : ClosableIterator<Pair<CStringPrefixed<K>,V>> {
    val keyPrefix = ByteArray(1, {prefix})
    return seekObjectInternal( Codecs.cstringPrefixed(keyCodec), valueCodec, keyPrefix, null)
  }


  private fun<K,V> seekObjectInternal(keyCodec : Codec<K>, valueCodec : Codec<V>, prefix: ByteArray, keyOption : K?) : ClosableIterator<Pair<K,V>> {
    /** We should stop the iteration if the prefix of the key changes.
      * So, hasNext first gets the next key and checks if the prefix remains unchanged.
      * next will return the element we got from hasNext.
      *
      * If the prefix is changed, we stop the iteration.
      *
      * @param iterator
      */
    class PrefetchingIterator(private val iterator:ClosableIterator<Pair<ByteArray,ByteArray>>) : ClosableIterator<Pair<K, V>> {
      var elementToReturn : Pair<ByteArray,ByteArray>? = null

      // BUGBUG : Optimize : Can we implement this method without toByteArray?
      override fun next() : Pair<K, V> {
        assert(elementToReturn != null)

        val rawKey = elementToReturn!!.first
        val rawValue = elementToReturn!!.second
        elementToReturn = null

        val readPrefix = rawKey.take(prefix.size).toByteArray()
        // hasNext should return false if the prefix of the next key does not match the prefix.
        assert( Arrays.equals(readPrefix,prefix) )
        // We need to drop the prefix byte for the rawKey.
        return Pair(keyCodec.decode(rawKey.drop(1).toByteArray())!!, valueCodec.decode(rawValue)!!)
      }

      /** Prefetch next key, and check if the prefix matches with the one privided by the first parameter of the seekObject method.
        *
        * @return true if the next key matches the prefix.
        */
      override fun hasNext() : Boolean {
        // We already have a prefetched key.
        if (elementToReturn != null) {
          val rawKey = elementToReturn!!.first
          val readPrefix = rawKey.take(prefix.size).toByteArray()
          if(Arrays.equals(readPrefix, prefix)) {
            return true
          } else {
            return false
          }
        } else {
          // We don't have a prefetched key. Prefetch one.
          if ( iterator.hasNext() ) {
            val (rawKey, rawValue) = iterator.next()
            assert(rawKey.size > 0)
            val readPrefix = rawKey.take(prefix.size).toByteArray()
            elementToReturn = Pair(rawKey, rawValue)

            // Continue the iteration only if the prefix remains unchanged.
            if(Arrays.equals(readPrefix, prefix)) {
              return true
            } else {
              return false
            }
          } else {
            return false
          }
        }
      }
      override fun close() = iterator.close()
    }

    val seekKey =
      if (keyOption != null)
        prefixedKey(prefix, keyCodec.encode(keyOption))
      else
        prefix

    val rawIterator = seek(seekKey)

    return PrefetchingIterator(rawIterator)
  }



  fun<V> getObject(valueCodec : Codec<V>, rawKey : ByteArray) : V? {
    val rawValue = get(rawKey)
    if (rawValue != null) {
      return valueCodec.decode(rawValue)
    } else {
      return null
    }
  }

  fun<K,V> getObject(keyCodec : Codec<K>, valueCodec : Codec<V>, prefix : Byte, key : K) : V? {
    val rawKey = prefixedKey(prefix, keyCodec.encode(key))
    return getObject(valueCodec, rawKey)
  }

  fun<K,V> getPrefixedObject(keyCodec : Codec<K>, valueCodec : Codec<V>, prefix : Byte, keyPrefix : String, key : K) : V? {
    val rawKey = prefixedKey(prefix, CStringPrefixedCodec<K>(keyCodec).encode(CStringPrefixed(keyPrefix, key)) )
    return getObject(valueCodec, rawKey)
  }

  fun<V> putObject(valueCodec : Codec<V>, rawKey : ByteArray, value : V) : Unit {
    val rawValue = valueCodec.encode(value)

    put(rawKey, rawValue)
  }

  fun<K,V> putObject(keyCodec : Codec<K>, valueCodec : Codec<V>, prefix : Byte, key : K, value : V) : Unit {
    val rawKey = prefixedKey(prefix, keyCodec.encode(key))

    putObject(valueCodec, rawKey, value)
  }

  fun<K,V> putPrefixedObject(keyCodec : Codec<K>, valueCodec : Codec<V>, prefix : Byte, keyPrefix : String, key : K, value : V) : Unit {
    val rawKey = prefixedKey(prefix, CStringPrefixedCodec<K>(keyCodec).encode(CStringPrefixed(keyPrefix, key)) )

    putObject(valueCodec, rawKey, value)
  }


  fun<K> delObject(keyCodec : Codec<K>, prefix : Byte, key : K) : Unit {
    val rawKey = prefixedKey(prefix, keyCodec.encode(key))
    del(rawKey)
  }

  fun<K> delPrefixedObject(keyCodec : Codec<K>, prefix : Byte, keyPrefix : String, key : K) : Unit {
    delPrefixedObject( keyCodec, prefix, CStringPrefixed(keyPrefix, key))
  }

  fun<K> delPrefixedObject(keyCodec : Codec<K>, prefix : Byte, key : CStringPrefixed<K>) : Unit {
    val rawKey = prefixedKey(prefix, CStringPrefixedCodec<K>(keyCodec).encode(key) )
    del(rawKey)
  }
}

