package io.scalechain.blockchain.storage.index

import com.google.cloud.spanner.*

// Imports the Google Cloud client library

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.StorageException
import org.slf4j.LoggerFactory
import java.util.*


class SpannerDatabaseIterator(val resultSet : ResultSet) : ClosableIterator<Pair<ByteArray,ByteArray>> {
  private var isClosed = false

  // Indicates whether the current cursor is positioned at a key/value pair which is not consumed yet.
  // This can happen when hasNext() was called but next() was not called yet.
  private var hasDataToConsume : Boolean = false

  /**
   * Return the next key/value pair.
   * Assumption : next() is called only after checking if hasNext() returns true.
   */
  override fun next() : Pair<ByteArray,ByteArray> {
    assert( !isClosed )

    if (!hasDataToConsume) { // next() was called without calling hasNext(), or we don't have any data.
      val hasNext = resultSet.next()
      if (!hasNext) { // The next key should exist.
        throw AssertionError()
      }
      hasDataToConsume = true
    }

    val rawKey = resultSet.getBytes(0).toByteArray()
    val rawValue = resultSet.getBytes(1).toByteArray()

    // Now we consumed the key/value after calling next() method.
    hasDataToConsume = false

    return Pair(rawKey, rawValue)
  }

  /**
   * Check if we have next key/value pair to get.
   */
  override fun hasNext() : Boolean {
    if (isClosed) {
      return false
    } else {
      if (hasDataToConsume) { // We have data to consume but did not consume yet. This happens when hasNext() was called twice without calling next()
        return true
      } else {
        hasDataToConsume = resultSet.next()
        return hasDataToConsume
      }
    }
  }

  override fun close() : Unit {
    resultSet.close()
    isClosed = true
  }
}


/**
 * A KeyValueDatabase implementation using RocksDB.
 */
open class SpannerDatabase(instanceId : String, databaseId : String, val tableName : String) : KeyValueDatabase {
  private val logger = LoggerFactory.getLogger(RocksDatabase::class.java)

  private lateinit var dbClient : DatabaseClient
  //private lateinit var dbAdminClient : DatabaseAdminClient

  fun getDbClient() = dbClient

  val sessionPoolOptions = SessionPoolOptions.newBuilder().setMinSessions(1).build()
  val options : SpannerOptions = SpannerOptions.newBuilder().setSessionPoolOption(sessionPoolOptions).build();
  var spanner : Spanner? = options.getService();

  init {
    val db : DatabaseId = DatabaseId.of(options.getProjectId(), instanceId, databaseId);
    // [END init_client]
    // This will return the default project id based on the environment.
    val clientProject : String = spanner!!.getOptions().getProjectId();
    if (!db.getInstanceId().getProject().equals(clientProject)) {
      StorageException(ErrorCode.InvalidProject, "Invalid project specified. Project in the database id should match"
        + "the project name set in the environment variable GCLOUD_PROJECT. Expected: "
        + clientProject);
    }
    // [START init_client]
    dbClient = spanner!!.getDatabaseClient(db);
    //dbAdminClient : DatabaseAdminClient = spanner.getDatabaseAdminClient();
  }

  /** Seek a key greater than or equal to the given key.
   * Return an iterator which iterates each (key, value) pair from the seek position.
   *
   * @param keyOption if Some(key) seek a key greater than or equal to the key; Seek all keys and values otherwise.
   * @return An Iterator to iterate (key, value) pairs.
   */
  override fun seek(keyOption : ByteArray? ) : ClosableIterator<Pair<ByteArray, ByteArray>> {
    val keySet =
      if (keyOption == null) {
        KeySet.all()
      }
      else {
        val keyRange =
          KeyRange.newBuilder()
                  .setStart(toKey(keyOption))
                  .setStartType(KeyRange.Endpoint.CLOSED)
                  .setEnd(toKey(MAX_BYTES_DATA))
                  .setEndType(KeyRange.Endpoint.CLOSED)
                  .build()
        KeySet.range( keyRange )
      }

    val resultSet : ResultSet = select( dbClient, tableName, keySet)

    return SpannerDatabaseIterator(resultSet)
  }


  override fun get(key : ByteArray ) : ByteArray? {
    val resultSet = dbClient
      .singleUse()
      .read(tableName,
        KeySet.singleKey(toKey(key)),
        Arrays.asList("value"))

    try {
      while (resultSet.next()) {
        return resultSet.getBytes(0).toByteArray()
      }
      return null
    } finally {
      resultSet.close()
    }
  }

  override fun put(key : ByteArray, value : ByteArray ) : Unit {
    val mutations = mutableListOf<Mutation>()

    mutations.add(
      Mutation.newInsertOrUpdateBuilder(tableName)
        .set("key")
        .to(com.google.cloud.ByteArray.copyFrom(key))
        .set("value")
        .to(com.google.cloud.ByteArray.copyFrom(value))
        .build());

    dbClient.write(mutations);
  }

  override fun del(key : ByteArray) : Unit {
    val mutations = mutableListOf<Mutation>()

    mutations.add(
      Mutation.delete(tableName, toKey(key))
    )

    dbClient.write(mutations);
  }

  /**
   * Create a new transacting db that supports transaction commit/abort operations.
   */
  override fun transacting() : TransactingKeyValueDatabase {
    return TransactingSpannerDatabase(this)
  }

  override fun close() : Unit {
    spanner!!.close();
    spanner = null
  }

  companion object {
    fun toKey(key : ByteArray) : Key {
      return Key.of( com.google.cloud.ByteArray.copyFrom(key) )
    }

    fun select(dbClient : DatabaseClient, tableName : String, keySet : KeySet) : ResultSet {
      return dbClient
        .singleUse()
        .read(tableName,
          keySet,
          Arrays.asList("key", "value"))
    }
    // BUGBUG : If the key has more than 64 bytes and all the first 64 bytes are FF, our cursor will not able to see it.
    // Spanner API requires to set the end key for a key range, so we had to use the following maximum key value to get all keys greater than a specific key.
    val MAX_BYTES_DATA = ByteArray(64, {255.toByte()})

  }
}
