package io.scalechain.blockchain.storage.record

import java.io.File

object BlockRecordStorage {
  val FILE_PREFIX = "blk"
  val MAX_FILE_SIZE = 1024 * 1024 * 100
  //val MAX_FILE_SIZE = 1024 * 128
}

class BlockRecordStorage(directoryPath : File)
  extends RecordStorage(
            directoryPath,
            BlockRecordStorage.FILE_PREFIX,
            BlockRecordStorage.MAX_FILE_SIZE)