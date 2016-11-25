package io.scalechain.blockchain.storage.record

import java.io.File

object BlockRecordStorage {
  val FILE_PREFIX = "blk"
}

class BlockRecordStorage(directoryPath : File, maxFileSize : Int)
  : RecordStorage(
            directoryPath,
            BlockRecordStorage.FILE_PREFIX,
            maxFileSize)