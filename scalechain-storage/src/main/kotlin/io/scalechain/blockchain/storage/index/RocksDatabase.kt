package io.scalechain.blockchain.storage.index

import java.io.File

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.blockchain.storage.Storage
import org.rocksdb._
import org.rocksdb.util.SizeUnit
import org.slf4j.LoggerFactory


class KeyValueIterator(rocksIterator : RocksIterator) : ClosableIterator<(Array<Byte>,Array<Byte>)> {
  var isClosed = false
  fun next : (Array<Byte>,Array<Byte>) {
    assert( !isClosed )

    if (!rocksIterator.isValid) {
      throw GeneralException(ErrorCode.NoMoreKeys)
    }

    val rawKey = rocksIterator.key
    val rawValue = rocksIterator.value

    rocksIterator.next

    (rawKey, rawValue)
  }
  fun hasNext : Boolean {
    if (isClosed) {
      false
    } else {
      rocksIterator.isValid
    }
  }

  fun close : Unit {
    rocksIterator.dispose()
    isClosed = true
  }
}

/**
  * A KeyValueDatabase implementation using RocksDB.
  */
class RocksDatabase(path : File) : KeyValueDatabase {
  private val logger = Logger( LoggerFactory.getLogger(classOf<RocksDatabase>) )

  assert( Storage.initialized )

  fun beginTransaction() : Unit {
    // No transaction supported. do nothing.
  }
  fun commitTransaction() : Unit {
    // No transaction supported. do nothing.
  }
  fun abortTransaction() : Unit {
    // No transaction supported. do nothing.
  }

  val dbAbsolutePath = path.getAbsolutePath

  // the Options class contains a set of configurable DB options
  // that determines the behavior of a database.
  protected<storage> var options =
    Options()
      .setCreateIfMissing(true)
      .setCreateMissingColumnFamilies(true)
      //.setStatsDumpPeriodSec(3)
      .setAllowOsBuffer(true)
      .setWriteBufferSize(256 * 1024 * 1024)
      .setMaxWriteBufferNumber(4)
      .setMinWriteBufferNumberToMerge(2)
      .setMaxOpenFiles(5000)
//      .setCompressionType(CompressionType.LZ4_COMPRESSION)
      //      .setCompressionType(CompressionType.SNAPPY_COMPRESSION)
      .setMaxBackgroundCompactions(3) // how many cores to allocate to compaction?
      .setMaxBackgroundFlushes(1)
//      .setCompactionStyle(CompactionStyle.LEVEL)
//            .setMaxTotalWalSize(1024 * 1024 * 1024)

  /*
  protected<storage> var bloomFilter = BloomFilter(10);

  var tableOptions : BlockBasedTableConfig = BlockBasedTableConfig()
  tableOptions.setBlockCacheSize(64 * SizeUnit.KB)
    .setFilter(bloomFilter)
    .setCacheIndexAndFilterBlocks(true)
    */
    /*
    .setCacheNumShardBits(6)
    .setBlockSizeDeviation(5)
    .setBlockRestartInterval(10)
    .setHashIndexAllowCollision(false)
    .setBlockCacheCompressedSize(64 * SizeUnit.KB)
    .setBlockCacheCompressedNumShardBits(10)
    */

  //options.setTableFormatConfig(tableOptions);


  options.getEnv().setBackgroundThreads(3, Env.COMPACTION_POOL)
    .setBackgroundThreads(1, Env.FLUSH_POOL)

  //      .setTargetFileSizeBase(options.maxBytesForLevelBase() / 10)

  // If we have any db open with the same path, use it.
  // This is necessary to use the same database for block/transaction storage and wallet storage.


  protected<storage> var db : RocksDB = RocksDB.open(options, dbAbsolutePath)


  /** Seek a key greater than or equal to the given key.
    * Return an iterator which iterates each (key, value) pair from the seek position.
    *
    * @param rocksIterator The iterator to use for seeking a key.
    * @param keyOption if Some(key) seek a key greater than or equal to the key; Seek all keys and values otherwise.
    * @return An Iterator to iterate (key, value) pairs.
    */
  protected<storage> fun seek(rocksIterator : RocksIterator, keyOption : Option<Array<Byte>> ) : ClosableIterator<(Array<Byte>, Array<Byte>)> {
    if (keyOption.isDefined) {
      rocksIterator.seek(keyOption.get)
    } else {
      rocksIterator.seekToFirst()
    }

    KeyValueIterator(rocksIterator)
  }

  /** Seek a key greater than or equal to the given key.
    * Return an iterator which iterates each (key, value) pair from the seek position.
    *
    * @param keyOption if Some(key) seek a key greater than or equal to the key; Seek all keys and values otherwise.
    * @return An Iterator to iterate (key, value) pairs.
    */
  fun seek(keyOption : Option<Array<Byte>> ) : ClosableIterator<(Array<Byte>, Array<Byte>)> {

    val rocksIterator =  db.newIterator()

    seek(rocksIterator, keyOption)
  }


  fun get(key : Array<Byte> ) : Option<Array<Byte>> {
    val value = db.get(key)
    if ( value != null )
      Some(value)
    else None
  }

  fun put(key : Array<Byte>, value : Array<Byte> ) : Unit {
    db.put(key, value)
  }

  fun del(key : Array<Byte>) : Unit {
    db.remove(key)
  }

  fun close() : Unit {
//    logger.info("Closing RocksDB.")
    fun getHistogram(histogramType : HistogramType) {
      val histo = options.statisticsPtr.getHistogramData(histogramType)
      s"Average: ${histo.getAverage}, Median: ${histo.getMedian}, 95%: ${histo.getPercentile95}, 99%: ${histo.getPercentile99}, Standard Deviation: ${histo.getStandardDeviation}"
    }

/*
      logger.warn(s"RocksDB statistics. Path : ${dbAbsolutePath}, WRITE_STALL : ${getHistogram(HistogramType.WRITE_STALL)}")
      logger.warn(s"RocksDB statistics. Path : ${dbAbsolutePath}, DB_WRITE : ${getHistogram(HistogramType.DB_WRITE)}")
      logger.warn(s"RocksDB statistics. Path : ${dbAbsolutePath}, WAL_FILE_SYNC_MICROS : ${getHistogram(HistogramType.WAL_FILE_SYNC_MICROS)}")
      logger.warn(s"RocksDB statistics. Path : ${dbAbsolutePath}, STALL_L0_SLOWDOWN_COUNT : ${getHistogram(HistogramType.STALL_L0_SLOWDOWN_COUNT)}")
      logger.warn(s"RocksDB statistics. Path : ${dbAbsolutePath}, STALL_MEMTABLE_COMPACTION_COUNT : ${getHistogram(HistogramType.STALL_MEMTABLE_COMPACTION_COUNT)}")
      logger.warn(s"RocksDB statistics. Path : ${dbAbsolutePath}, STALL_L0_NUM_FILES_COUNT : ${getHistogram(HistogramType.STALL_L0_NUM_FILES_COUNT)}")
*/
    if (db != null) {
      assert(options != null)
      db.close
      options.close
  //      bloomFilter.close
    }

    db = null
    options = null
    /*
    bloomFilter = null
    tableOptions = null
    */
  }
}
