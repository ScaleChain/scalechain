package io.scalechain.blockchain.storage.index

import java.io.File

object DatabaseFactory {
  fun create(path : File) : KeyValueDatabase = MapDatabase(path)
}