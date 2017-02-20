package io.scalechain.blockchain.storage.index

import java.io.File

object DatabaseFactory {
//  fun create(path : File) : KeyValueDatabase = RocksDatabase(path)
  fun create(path : File) : KeyValueDatabase = SpannerDatabase(instanceId = "scalechain", databaseId = "blockchain", tableName = "data")
}
