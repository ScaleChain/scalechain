package io.scalechain.blockchain.storage.record

import java.io.File

/**
  * Created by mijeong on 2016. 3. 30..
  */
object TransactionRecordStorage {
  val FILE_PREFIX = "wallet-transaction"
  val MAX_FILE_SIZE = 1024 * 1024
}

class TransactionRecordStorage(directoryPath : File)
  extends RecordStorage(directoryPath, BlockRecordStorage.FILE_PREFIX, BlockRecordStorage.MAX_FILE_SIZE)
