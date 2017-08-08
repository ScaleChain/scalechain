package io.scalechain.blockchain.storage.record

import java.io.File

class BlockRecordStorage(directoryPath : File, maxFileSize : Int)
  : RecordStorage(
            directoryPath,
            BlockRecordStorage.FILE_PREFIX,
            maxFileSize.toLong()) {
  companion object {
    val FILE_PREFIX = "blk"
  }
}